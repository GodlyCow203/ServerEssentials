package net.lunark.io.auction;

import net.lunark.io.commands.config.AuctionConfig;
import net.lunark.io.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.*;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class AuctionGUI {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final int ITEMS_PER_PAGE = 36;
    private static final int GUI_SIZE = 54;

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final AuctionStorage storage;
    private final AuctionConfig config;
    private final Map<Player, Map<Integer, AuctionItem>> auctionSlotMap = new HashMap<>();
    private final Map<Player, Map<Integer, AuctionItem>> playerItemsSlotMap = new HashMap<>();
    private final Map<Player, AuctionItem> pendingRemoval = new HashMap<>();

    public AuctionGUI(Plugin plugin, PlayerLanguageManager langManager, AuctionStorage storage, AuctionConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    public void openAuctionHouse(Player player, int page) {
        storage.getAllItems().thenAccept(items -> {
            if (items.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "auction.gui.empty",
                        "<yellow>No items are currently listed for auction."));
                return;
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
            page = Math.max(1, Math.min(page, totalPages));

            int finalPage = page;
            Bukkit.getScheduler().runTask(plugin, () -> createAndOpenAuctionGUI(player, items, finalPage));
        });
    }

    private void createAndOpenAuctionGUI(Player player, List<AuctionItem> items, int page) {
        Component title = langManager.getMessageFor(player, "auction.gui.title",
                "<green>Auction House - Page {page}",
                ComponentPlaceholder.of("{page}", finalPage));

        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, MINI_MESSAGE.serialize(title));
        addBorder(inv);

        Map<Integer, AuctionItem> slotMap = new HashMap<>();
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem auctionItem = items.get(i);
            ItemStack displayItem = createAuctionDisplayItem(player, auctionItem);
            int slot = calculateSlot(i - start);
            inv.setItem(slot, displayItem);
            slotMap.put(slot, auctionItem);
        }

        auctionSlotMap.put(player, slotMap);
        addNavigationItems(inv, player, page, items.size(), "auction");
        player.openInventory(inv);
    }

    public void openPlayerItems(Player player, int page) {
        storage.getItemsBySeller(player.getUniqueId()).thenAccept(items -> {
            if (items.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "auction.gui.my-items.empty",
                        "<yellow>You don't have any items listed for auction."));
                return;
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
            page = Math.max(1, Math.min(page, totalPages));

            Bukkit.getScheduler().runTask(plugin, () -> createAndOpenPlayerItemsGUI(player, items, page));
        });
    }

    private void createAndOpenPlayerItemsGUI(Player player, List<AuctionItem> items, int page) {
        Component title = langManager.getMessageFor(player, "auction.gui.my-items.title",
                "<gold>Your Auction Items - Page {page}",
                ComponentPlaceholder.of("{page}", page));

        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, MINI_MESSAGE.serialize(title));
        addBorder(inv);

        Map<Integer, AuctionItem> slotMap = new HashMap<>();
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        for (int i = start; i < end; i++) {
            AuctionItem auctionItem = items.get(i);
            ItemStack displayItem = createPlayerItemDisplay(player, auctionItem);
            int slot = calculateSlot(i - start);
            inv.setItem(slot, displayItem);
            slotMap.put(slot, auctionItem);
        }

        playerItemsSlotMap.put(player, slotMap);
        addNavigationItems(inv, player, page, items.size(), "my-items");
        inv.setItem(4, createBalanceItem(player));
        player.openInventory(inv);
    }

    public void openRemoveConfirmation(Player player, AuctionItem item) {
        Component title = langManager.getMessageFor(player, "auction.gui.remove.title",
                "<red>Confirm Removal");

        Inventory inv = Bukkit.createInventory(null, 27, MINI_MESSAGE.serialize(title));

        ItemStack confirm = createGuiItem(Material.GREEN_WOOL,
                langManager.getMessageFor(player, "auction.gui.remove.confirm",
                        "<green>✓ Confirm"));

        ItemStack cancel = createGuiItem(Material.RED_WOOL,
                langManager.getMessageFor(player, "auction.gui.remove.cancel",
                        "<red>✗ Cancel"));

        inv.setItem(11, confirm);
        inv.setItem(15, cancel);

        pendingRemoval.put(player, item);
        player.openInventory(inv);
    }

    public Optional<AuctionItem> getPendingRemoval(Player player) {
        return Optional.ofNullable(pendingRemoval.remove(player));
    }

    public Optional<AuctionItem> getAuctionItem(Player player, int slot) {
        return Optional.ofNullable(auctionSlotMap.getOrDefault(player, Map.of()).get(slot));
    }

    public Optional<AuctionItem> getPlayerItem(Player player, int slot) {
        return Optional.ofNullable(playerItemsSlotMap.getOrDefault(player, Map.of()).get(slot));
    }

    public void clearPlayerData(Player player) {
        auctionSlotMap.remove(player);
        playerItemsSlotMap.remove(player);
        pendingRemoval.remove(player);
    }

    private ItemStack createAuctionDisplayItem(Player player, AuctionItem auctionItem) {
        ItemStack item = auctionItem.getItem().clone();
        ItemMeta meta = item.getItemMeta();

        Component name = langManager.getMessageFor(player, "auction.gui.item.name",
                "<white>{item} <gray>- <green>${price}",
                ComponentPlaceholder.of("{item}", auctionItem.getItem().getType().name()),
                ComponentPlaceholder.of("{price}", String.format("%.2f", auctionItem.getPrice())));

        meta.displayName(MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(name)));

        List<Component> lore = new ArrayList<>();
        lore.add(langManager.getMessageFor(player, "auction.gui.item.seller",
                "<gray>Seller: <yellow>{seller}",
                ComponentPlaceholder.of("{seller}", Bukkit.getOfflinePlayer(auctionItem.getSeller()).getName())));

        lore.add(langManager.getMessageFor(player, "auction.gui.item.expires",
                "<gray>Expires: <yellow>{date}",
                ComponentPlaceholder.of("{date}", DATE_FORMAT.format(new Date(auctionItem.getExpiration()))));

        lore.add(Component.empty());
        lore.add(langManager.getMessageFor(player, "auction.gui.item.click-to-buy",
                "<green>Click to purchase!"));

        meta.lore(lore.stream().map(c -> MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(c))).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerItemDisplay(Player player, AuctionItem auctionItem) {
        ItemStack item = auctionItem.getItem().clone();
        ItemMeta meta = item.getItemMeta();

        Component name = langManager.getMessageFor(player, "auction.gui.my-item.name",
                "<white>{item} <gray>- <green>${price}",
                ComponentPlaceholder.of("{item}", auctionItem.getItem().getType().name()),
                ComponentPlaceholder.of("{price}", String.format("%.2f", auctionItem.getPrice())));

        meta.displayName(MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(name)));

        List<Component> lore = new ArrayList<>();
        lore.add(langManager.getMessageFor(player, "auction.gui.my-item.price",
                "<gray>Price: <green>${price}",
                ComponentPlaceholder.of("{price}", String.format("%.2f", auctionItem.getPrice()))));

        lore.add(langManager.getMessageFor(player, "auction.gui.my-item.expires",
                "<gray>Expires: <yellow>{date}",
                ComponentPlaceholder.of("{date}", DATE_FORMAT.format(new Date(auctionItem.getExpiration())))));

        lore.add(Component.empty());
        lore.add(langManager.getMessageFor(player, "auction.gui.my-item.click-to-remove",
                "<red>Click to remove!"));

        meta.lore(lore.stream().map(c -> MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(c))).toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBalanceItem(Player player) {
        // Note: Economy calls should be done async before this
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = skull.getItemMeta();

        Component name = langManager.getMessageFor(player, "auction.gui.balance",
                "<gold>Your Balance: <green>${balance}",
                ComponentPlaceholder.of("{balance}", "0.00")); // Balance should be passed in

        meta.displayName(MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(name)));
        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack createGuiItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MINI_MESSAGE.deserialize(MINI_MESSAGE.serialize(name)));
        item.setItemMeta(meta);
        return item;
    }

    private void addBorder(Inventory inv) {
        ItemStack border = createGuiItem(Material.BLACK_STAINED_GLASS_PANE,
                langManager.getMessageFor(null, "auction.gui.border", "<black>◆"));

        int size = inv.getSize();
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = size - 9; i < size; i++) inv.setItem(i, border);
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
    }

    private void addNavigationItems(Inventory inv, Player player, int page, int totalItems, String type) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));

        if (page > 1) {
            inv.setItem(45, createGuiItem(Material.ARROW,
                    langManager.getMessageFor(player, "auction.gui.prev-page",
                            "<gray>← Previous Page")));
        }

        inv.setItem(49, createGuiItem(Material.BARRIER,
                langManager.getMessageFor(player, "auction.gui.close",
                        "<red>Close")));

        if (page < totalPages) {
            inv.setItem(53, createGuiItem(Material.ARROW,
                    langManager.getMessageFor(player, "auction.gui.next-page",
                            "<gray>Next Page →")));
        }

        if ("auction".equals(type)) {
            inv.setItem(0, createGuiItem(Material.PAPER,
                    langManager.getMessageFor(player, "auction.gui.refresh",
                            "<yellow>🔄 Refresh")));

            inv.setItem(4, createBalanceItem(player));

            inv.setItem(8, createGuiItem(Material.CHEST,
                    langManager.getMessageFor(player, "auction.gui.my-items",
                            "<gold>📦 Your Items")));
        }
    }

    private int calculateSlot(int index) {
        int row = index / 7;
        int col = index % 7;
        return 10 + (row * 9) + col;
    }
}