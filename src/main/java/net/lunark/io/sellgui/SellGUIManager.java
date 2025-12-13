package net.lunark.io.sellgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.lunark.io.commands.config.SellConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;
import java.util.*;

public class SellGUIManager implements Listener {
    private static final MiniMessage mini = MiniMessage.miniMessage();
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final SellStorage storage;
    private final SellConfig config;
    private final ServerEssentialsEconomy economy;
    private final Map<UUID, Double> pendingValues = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");
    private final Set<Inventory> sellInventories = new HashSet<>();

    public SellGUIManager(Plugin plugin, PlayerLanguageManager langManager, SellStorage storage,
                          SellConfig config, ServerEssentialsEconomy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.economy = economy;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openSellGUI(Player player) {
        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.disabled",
                    "<red>The sell GUI is currently disabled."));
            return;
        }

        Component title = langManager.getMessageFor(player, "economy.sellgui.title",
                "<gold>💰 Sell Items");
        Inventory inv = Bukkit.createInventory(null, config.guiSize, title);
        createBorder(inv);
        createInfoPanel(player, inv);

        player.openInventory(inv);
        pendingValues.put(player.getUniqueId(), 0.0);
        sellInventories.add(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1f);
    }

    private void createBorder(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                ItemStack pane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
                ItemMeta meta = pane.getItemMeta();
                if (meta != null) {
                    meta.displayName(mini.deserialize(" "));
                    meta.getPersistentDataContainer().set(
                            new org.bukkit.NamespacedKey(plugin, "sellgui_display"),
                            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1
                    );
                    pane.setItemMeta(meta);
                }
                inv.setItem(i, pane);
            }
        }
    }

    private void createInfoPanel(Player player, Inventory inv) {
        ItemStack emerald = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = emerald.getItemMeta();
        if (meta != null) {
            meta.displayName(langManager.getMessageFor(player, "economy.sellgui.info.title",
                    "<green><bold>✓ SELL ZONE"));

            List<Component> lore = new ArrayList<>();
            lore.add(langManager.getMessageFor(player, "economy.sellgui.info.subtitle",
                    "<gray>Place items here to sell"));
            lore.add(langManager.getMessageFor(player, "economy.sellgui.info.instruction",
                    "<gray>Close inventory to process"));
            lore.add(Component.empty());
            lore.add(langManager.getMessageFor(player, "economy.sellgui.info.value-prefix",
                    "<gold>Total Value: <yellow>$0.00"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "sellgui_display"),
                    org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1
            );
            emerald.setItemMeta(meta);
        }
        inv.setItem(inv.getSize() - 5, emerald);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!sellInventories.contains(event.getInventory())) {
            return;
        }

        if (event.getCurrentItem() != null && isDisplayItem(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick().isShiftClick() && event.getClickedInventory() == event.getInventory()) {
            int slot = event.getSlot();
            if (slot < 9 || slot >= event.getInventory().getSize() - 9 || slot % 9 == 0 || slot % 9 == 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!sellInventories.contains(event.getInventory())) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < 9 || slot >= event.getInventory().getSize() - 9 || slot % 9 == 0 || slot % 9 == 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isDisplayItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "sellgui_display"),
                org.bukkit.persistence.PersistentDataType.BYTE
        );
    }

    public boolean isSellable(Material material) {
        return config.isSellable(material);
    }

    public void updateValueDisplay(Player player, Inventory inv, double totalValue) {
        int centerSlot = inv.getSize() - 5;
        ItemStack item = inv.getItem(centerSlot);
        if (item != null && item.getType() == Material.EMERALD_BLOCK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<Component> lore = meta.lore();
                if (lore.size() >= 4) {
                    String valuePrefix = langManager.getMessageFor(player, "economy.sellgui.info.value-prefix",
                                    "<gold>Total Value: <yellow>")
                            .toString()
                            .replaceAll("\\$0.00$", "");

                    lore.set(3, mini.deserialize(valuePrefix + formatter.format(totalValue)));
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
            }
        }
    }

    public void processSellAndReturnItems(Player player, Inventory inv) {
        UUID uuid = player.getUniqueId();
        Map<Integer, ItemStack> itemsToReturn = new HashMap<>();
        double totalValue = 0.0;
        int itemsSold = 0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) continue;
            if (item.getType() == Material.CYAN_STAINED_GLASS_PANE || item.getType() == Material.EMERALD_BLOCK) continue;

            Material mat = item.getType();
            double pricePerItem = config.getSellPrice(mat);

            if (pricePerItem > 0) {
                int amount = item.getAmount();
                double itemValue = pricePerItem * amount;
                totalValue += itemValue;
                itemsSold += amount;
                storage.logSale(uuid, player.getName(), mat, amount, pricePerItem, itemValue);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.5f);
            } else {
                itemsToReturn.put(i, item.clone());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.4f, 0.8f);
            }
        }

        if (totalValue > 0) {
            economy.depositPlayer(player, totalValue);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1f);

            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.sold-success",
                    "<green><bold>✓ SOLD</bold> <white>{amount}x items</white> for <gold>{symbol}{price}",
                    ComponentPlaceholder.of("{amount}", String.valueOf(itemsSold)),
                    ComponentPlaceholder.of("{symbol}", config.currencySymbol),
                    ComponentPlaceholder.of("{price}", formatter.format(totalValue))));
        }

        if (!itemsToReturn.isEmpty()) {
            Map<Integer, ItemStack> leftOver = player.getInventory().addItem(
                    itemsToReturn.values().toArray(new ItemStack[0])
            );

            if (!leftOver.isEmpty()) {
                leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.inventory-full",
                        "<yellow>⚠ Some items were dropped on the ground (inventory full)"));
            }

            if (totalValue == 0) {
                player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.returned-items",
                        "<gray>Returned <white>{amount}x unsellable items</white> to your inventory",
                        ComponentPlaceholder.of("{amount}", String.valueOf(itemsToReturn.size()))));
            }
        }

        pendingValues.remove(uuid);
        sellInventories.remove(inv);

        if (totalValue == 0 && itemsToReturn.isEmpty()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.no-items",
                    "<gray>No items were sold"));
        }
    }
}