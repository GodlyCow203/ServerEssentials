package serveressentials.serveressentials.auction;

import serveressentials.serveressentials.ServerEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GUIManager {

    private final ServerEssentials plugin;
    private final int itemsPerPage = 36;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private final Map<Player, Map<Integer, AuctionItem>> auctionGUIMap = new HashMap<>();
    private final Map<Player, Map<Integer, AuctionItem>> playerItemsGUIMap = new HashMap<>();
    private final Map<Player, AuctionItem> pendingRemove = new HashMap<>();

    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

    public GUIManager(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    private void addBorder(Inventory inv) {
        ItemStack glass = createGuiItem(Material.BLACK_STAINED_GLASS_PANE,
                plugin.getAuctionMessagesManager().getMessage("gui.border"));
        int size = inv.getSize();
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass);
        for (int i = 9; i < size - 9; i++) {
            if (i % 9 == 0 || i % 9 == 8) inv.setItem(i, glass);
        }
    }

    /** Open Auction House GUI */
    public void openAuctionGUI(Player player, int page) {
        List<AuctionItem> items = plugin.getAuctionManager().getAuctionItems();
        Component titleMM = plugin.getAuctionMessagesManager()
                .getMessage("gui.auction.title", "%page%", String.valueOf(page));
        Inventory inv = Bukkit.createInventory(null, 54, legacy.serialize(plugin.getAuctionMessagesManager().getMessage("gui.auction.title", "%page%", String.valueOf(page))));

        addBorder(inv);
        Map<Integer, AuctionItem> slotMap = new HashMap<>();

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem auctionItem = items.get(i);
            ItemStack item = auctionItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(legacy.serialize(plugin.getAuctionMessagesManager()
                    .getMessage("gui.auction.item-name", "%price%", String.valueOf(auctionItem.getPrice()))));

            List<Component> loreMM = List.of(
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.auction.item-lore.seller", "%seller%", Bukkit.getOfflinePlayer(auctionItem.getSeller()).getName()),
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.auction.item-lore.expires", "%expires%", sdf.format(new Date(auctionItem.getExpiration()))),
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.auction.item-lore.click")
            );

            List<String> lore = new ArrayList<>();
            for (Component c : loreMM) lore.add(legacy.serialize(c));

            meta.setLore(lore);
            item.setItemMeta(meta);

            int slot = i - start + 10;
            if (slot >= 45) slot += 1;
            inv.setItem(slot, item);
            slotMap.put(slot, auctionItem);
        }

        auctionGUIMap.put(player, slotMap);

        inv.setItem(45, createGuiItem(Material.ARROW, plugin.getAuctionMessagesManager().getMessage("gui.navigation.prev")));
        inv.setItem(0, createGuiItem(Material.PAPER, plugin.getAuctionMessagesManager().getMessage("gui.navigation.refresh")));
        inv.setItem(4, createPlayerBalanceItem(player));
        inv.setItem(8, createGuiItem(Material.CHEST, plugin.getAuctionMessagesManager().getMessage("gui.navigation.my-items")));
        inv.setItem(49, createGuiItem(Material.BARRIER, plugin.getAuctionMessagesManager().getMessage("gui.navigation.close")));
        inv.setItem(53, createGuiItem(Material.ARROW, plugin.getAuctionMessagesManager().getMessage("gui.navigation.next")));

        player.openInventory(inv);
    }

    private ItemStack createPlayerBalanceItem(Player player) {
        double balance = plugin.getVaultEconomy().getBalance(player);
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName(legacy.serialize(plugin.getAuctionMessagesManager()
                .getMessage("gui.balance", "%balance%", String.valueOf(balance))));
        skull.setItemMeta(meta);
        return skull;
    }

    /** Open Player's items GUI */
    public void openPlayerItemsGUI(Player player, int page) {
        List<AuctionItem> items = plugin.getAuctionManager().getPlayerItems(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54,
                legacy.serialize(plugin.getAuctionMessagesManager()
                        .getMessage("gui.my-items.title", "%page%", String.valueOf(page))));

        addBorder(inv);
        Map<Integer, AuctionItem> slotMap = new HashMap<>();

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem auctionItem = items.get(i);
            ItemStack item = auctionItem.getItem().clone();
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(legacy.serialize(plugin.getAuctionMessagesManager()
                    .getMessage("gui.my-items.item-name", "%price%", String.valueOf(auctionItem.getPrice()))));

            List<Component> loreMM = List.of(
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.my-items.item-lore.price", "%price%", String.valueOf(auctionItem.getPrice())),
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.my-items.item-lore.expires", "%expires%", sdf.format(new Date(auctionItem.getExpiration()))),
                    plugin.getAuctionMessagesManager()
                            .getMessage("gui.my-items.item-lore.click")
            );

            List<String> lore = new ArrayList<>();
            for (Component c : loreMM) lore.add(legacy.serialize(c));

            meta.setLore(lore);
            item.setItemMeta(meta);

            int slot = i - start + 10;
            if (slot >= 45) slot += 1;
            inv.setItem(slot, item);
            slotMap.put(slot, auctionItem);
        }

        playerItemsGUIMap.put(player, slotMap);

        inv.setItem(45, createGuiItem(Material.ARROW, plugin.getAuctionMessagesManager().getMessage("gui.navigation.prev")));
        inv.setItem(53, createGuiItem(Material.ARROW, plugin.getAuctionMessagesManager().getMessage("gui.navigation.next")));
        inv.setItem(49, createGuiItem(Material.BARRIER, plugin.getAuctionMessagesManager().getMessage("gui.navigation.close")));

        player.openInventory(inv);
    }

    /** Open confirmation GUI to remove item */
    public void openRemoveConfirmGUI(Player player, AuctionItem item) {
        Inventory inv = Bukkit.createInventory(null, 27,
                legacy.serialize(plugin.getAuctionMessagesManager().getMessage("gui.remove.title")));

        inv.setItem(11, createGuiItem(Material.GREEN_WOOL, plugin.getAuctionMessagesManager().getMessage("gui.remove.confirm")));
        inv.setItem(15, createGuiItem(Material.RED_WOOL, plugin.getAuctionMessagesManager().getMessage("gui.remove.cancel")));

        pendingRemove.put(player, item);
        player.openInventory(inv);
    }

    public AuctionItem getItemToRemove(Player player) {
        return pendingRemove.remove(player);
    }

    private ItemStack createGuiItem(Material mat, Component name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(legacy.serialize(name));
        item.setItemMeta(meta);
        return item;
    }

    public AuctionItem getAuctionItem(Player player, int slot) {
        return auctionGUIMap.getOrDefault(player, Map.of()).get(slot);
    }

    public AuctionItem getPlayerItem(Player player, int slot) {
        return playerItemsGUIMap.getOrDefault(player, Map.of()).get(slot);
    }
}
