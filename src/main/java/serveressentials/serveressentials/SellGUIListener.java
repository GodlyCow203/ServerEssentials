package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellGUIListener implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private static String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    private static String colorize(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + color).toString());
        }

        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static void openSellGUI(Player player) {
        Inventory sellInv = Bukkit.createInventory(player, 54, ChatColor.RED + "Sell Items Here");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                sellInv.setItem(i, glass);
            }
        }

        ItemStack sellButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta sellMeta = sellButton.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName(colorize("<#FFA500>Click to Sell All Items"));
            sellButton.setItemMeta(sellMeta);
        }
        sellInv.setItem(48, sellButton);

        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(colorize("<#FF5555>Cancel"));
            cancelButton.setItemMeta(cancelMeta);
        }
        sellInv.setItem(49, cancelButton);

        sellInv.setItem(50, createTotalDisplay(0));
        player.openInventory(sellInv);
        updateTotalValue(sellInv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(ChatColor.RED + "Sell Items Here")) return;

        int slot = event.getRawSlot();
        if (slot >= 54) return;

        if (slot == 48 || slot == 49 || slot == 50 || slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8) {
            event.setCancelled(true);
        }

        if (slot == 48) {
            event.setCancelled(true);
            double total = sellItems(event.getInventory(), player);
            if (total > 0) {
                player.sendMessage(getPrefix() + colorize("<#00FF00>Sold items for $" + String.format("%.2f", total)));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
            } else {
                player.sendMessage(getPrefix() + colorize("<#FFFF55>No sellable items found."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            }
            player.closeInventory();
            return;
        }

        if (slot == 49) {
            event.setCancelled(true);
            returnUnsoldItems(event.getInventory(), player);
            player.sendMessage(getPrefix() + colorize("<#FFAA00>Sale cancelled. Items returned."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 0.6F);
            player.closeInventory();
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7F, 1.2F);
        Bukkit.getScheduler().runTaskLater(ServerEssentials.getInstance(), () ->
                updateTotalValue(event.getView().getTopInventory()), 1L);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.RED + "Sell Items Here")) return;

        for (int slot : event.getRawSlots()) {
            if (slot == 48 || slot == 49 || slot == 50 || slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTaskLater(ServerEssentials.getInstance(), () ->
                updateTotalValue(event.getInventory()), 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.RED + "Sell Items Here")) return;
        Player player = (Player) event.getPlayer();
        returnUnsoldItems(event.getInventory(), player);
    }

    private double sellItems(Inventory inv, Player player) {
        double total = 0;

        for (int i = 0; i < 54; i++) {
            if (i == 48 || i == 49 || i == 50 || i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) continue;

            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                double price = ShopManager.getSellPrice(item.getType());
                if (price > 0) {
                    total += price * item.getAmount();
                } else {
                    player.getInventory().addItem(item.clone());
                }
                inv.setItem(i, null);
            }
        }

        if (total > 0) {
            EconomyManager.addBalance(player.getUniqueId(), total);
        }

        return total;
    }

    private void returnUnsoldItems(Inventory inv, Player player) {
        for (int i = 0; i < 54; i++) {
            if (i == 48 || i == 49 || i == 50 || i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) continue;

            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item.clone());
                inv.setItem(i, null);
            }
        }
    }

    private static void updateTotalValue(Inventory inv) {
        double total = 0;

        for (int i = 0; i < 54; i++) {
            if (i == 48 || i == 49 || i == 50 || i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) continue;

            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                double price = ShopManager.getSellPrice(item.getType());
                if (price > 0) {
                    total += price * item.getAmount();
                }
            }
        }

        inv.setItem(50, createTotalDisplay(total));
    }

    private static ItemStack createTotalDisplay(double value) {
        ItemStack totalDisplay = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta meta = totalDisplay.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize("<#00FF00>Total Value: $" + String.format("%.2f", value)));
            totalDisplay.setItemMeta(meta);
        }
        return totalDisplay;
    }
}
