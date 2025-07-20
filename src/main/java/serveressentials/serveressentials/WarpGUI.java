package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarpGUI {

    // Define allowed categories (hardcoded for now)
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList("Shop", "PvP", "Spawn", "Events", "MiniGames");

    public static void openMainMenu(Player player) {
        int size = Math.max(9, ((DEFAULT_CATEGORIES.size() - 1) / 9 + 1) * 9);
        Inventory gui = Bukkit.createInventory(null, size, color("&#5f9ea0&lWarp Categories"));

        for (String category : DEFAULT_CATEGORIES) {
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color("&#add8e6» &f" + category));
                item.setItemMeta(meta);
                gui.addItem(item);
            }
        }

        player.openInventory(gui);
    }

    public static void openWarpCategory(Player player, String category) {
        if (!DEFAULT_CATEGORIES.contains(category)) {
            player.sendMessage(ChatColor.RED + "Invalid category.");
            return;
        }

        List<WarpData> warps = WarpManager.getWarpsByCategory(category);
        if (warps.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No warps in this category.");
            return;
        }

        int size = Math.min(54, Math.max(9, ((warps.size() - 1) / 9 + 1) * 9));
        Inventory gui = Bukkit.createInventory(null, size, color("&#4682b4" + capitalize(category)));

        for (WarpData warp : warps) {
            ItemStack item = new ItemStack(warp.getMaterial());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(color("&#7fffd4» &f" + warp.getName()));

                List<String> lore = new ArrayList<>();
                if (warp.getDescription() != null && !warp.getDescription().isEmpty()) {
                    lore.add(color("&7" + warp.getDescription()));
                    lore.add("");
                }
                lore.add(color("&#00ff00Click to warp"));
                meta.setLore(lore);

                item.setItemMeta(meta);
            }

            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        event.setCancelled(true);

        String title = ChatColor.stripColor(event.getView().getTitle());
        String clickedName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        // Handle category click
        if (title.equalsIgnoreCase("Warp Categories")) {
            if (DEFAULT_CATEGORIES.contains(clickedName)) {
                openWarpCategory(player, clickedName);
            } else {
                player.sendMessage(ChatColor.RED + "Invalid category.");
            }
            return;
        }

        // Handle warp click
        WarpData warp = WarpManager.getWarpData(clickedName.toLowerCase());
        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Warp not found.");
            return;
        }

        long remaining = WarpCooldowns.getRemainingCooldown(player.getUniqueId(), warp.getName());
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "You must wait " + remaining + "s before using this warp.");
            return;
        }

        player.teleport(warp.getLocation());
        player.sendMessage(ChatColor.GREEN + "Warped to " + warp.getName());
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        WarpCooldowns.setCooldown(player.getUniqueId(), warp.getName(), warp.getCooldownSeconds());
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    public static Inventory getCategoryGUI(String category) {
        List<WarpData> warps = WarpManager.getWarpsByCategory(category);
        int size = Math.min(54, Math.max(27, ((warps.size() - 1) / 9 + 1) * 9));
        Inventory gui = Bukkit.createInventory(null, size, color("&#4682b4" + capitalize(category)));

        for (WarpData warp : warps) {
            ItemStack item = new ItemStack(warp.getMaterial());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(color("&#7fffd4» &f" + warp.getName()));

                List<String> lore = new ArrayList<>();
                if (warp.getDescription() != null && !warp.getDescription().isEmpty()) {
                    lore.add(color("&7" + warp.getDescription()));
                    lore.add("");
                }
                lore.add(color("&#00ff00Click to warp"));
                meta.setLore(lore);

                item.setItemMeta(meta);
            }

            gui.addItem(item);
        }

        return gui;
    }


    // Supports legacy &-color codes and pseudo-hex like '&#RRGGBB'
    private static String color(String message) {
        Pattern pattern = Pattern.compile("(?i)&#([0-9a-f]{6})");
        Matcher matcher = pattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
