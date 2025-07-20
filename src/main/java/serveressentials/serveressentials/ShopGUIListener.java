package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import serveressentials.serveressentials.EconomyManager;
import serveressentials.serveressentials.ShopItem;
import serveressentials.serveressentials.ShopManager;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopGUIListener implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static void openSectionSelector(Player player, int page) {
        Inventory sectionGUI = Bukkit.createInventory(null, 27, translateHexColor("<#4B0082>Shop Sections (Page " + page + ")"));

        String[] sectionNames = {
                "Misc", "Tools", "Combat", "Nether", "Farming", "Redstone", "Brewing",
                "Wood", "Mob Drops", "Decorations", "Ores", "Food", "End", "Colored Blocks",
                "Spawners [Disabled by the Dev]"
        };

        String[] sectionDescriptions = {
                "Various useful items", "Pickaxes, shovels, and more", "Weapons and armor",
                "Nether-related blocks and items", "Seeds, crops, and farming tools", "Redstone components and circuits",
                "Potions and brewing items", "Logs and wood-related blocks", "Drops from mobs",
                "Decorative blocks and furniture", "Ores and minerals", "Food and consumables",
                "End dimension blocks", "Colored wool and blocks",
                "Mob spawners with preset mobs"
        };

        Material[] icons = {
                Material.BOOK, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.NETHERRACK,
                Material.WHEAT, Material.REDSTONE, Material.BREWING_STAND, Material.OAK_LOG,
                Material.ROTTEN_FLESH, Material.FLOWER_POT, Material.DIAMOND,
                Material.COOKED_BEEF, Material.END_STONE, Material.LIME_WOOL,
                Material.BARRIER
        };

        int itemsPerPage = 7;
        int start = (page - 1) * itemsPerPage;
        int[] slots = {10, 11, 12, 13, 14, 15, 16};

        for (int i = 0; i < itemsPerPage; i++) {
            if (start + i >= sectionNames.length) break;

            ItemStack item = new ItemStack(icons[start + i]);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(translateHexColor("<#FFD700>" + sectionNames[start + i]));
                meta.setLore(Arrays.asList(
                        translateHexColor("<#FFFFFF>" + sectionDescriptions[start + i]),
                        "",
                        translateHexColor("<#00FF00>Click to open")
                ));
                item.setItemMeta(meta);
            }
            sectionGUI.setItem(slots[i], item);
        }

        if (page > 1) {
            sectionGUI.setItem(18, createNavItem(Material.ARROW, translateHexColor("<#00FF00>Previous Page")));
        }

        if (start + itemsPerPage < sectionNames.length) {
            sectionGUI.setItem(26, createNavItem(Material.ARROW, translateHexColor("<#00FF00>Next Page")));
        }

        sectionGUI.setItem(22, createNavItem(Material.BARRIER, translateHexColor("<#FF0000>Close")));
        player.openInventory(sectionGUI);
    }

    public static void openShopGUI(Player player, String section, int page) {
        List<ShopItem> items = ShopManager.getItems(section);
        Inventory gui = Bukkit.createInventory(null, 54, translateHexColor("<#228B22>Shop: " + section + " (Page " + page + ")"));

        for (ShopItem item : items) {
            if (item.getPage() == page) {
                ItemStack display = item.getItem().clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(translateHexColor("<#FFD700>" + capitalizeWords(display.getType().name().replace("_", " "))));
                    meta.setLore(Arrays.asList(
                            translateHexColor("<#FFD700>Price:"),
                            translateHexColor("<#00FF00>  Buy: $" + item.getBuyPrice()),
                            translateHexColor("<#FF0000>  Sell: $" + item.getSellPrice()),
                            "",
                            translateHexColor("<#FFD700>Usage:"),
                            translateHexColor("<#00FFFF>  Left-click to buy"),
                            translateHexColor("<#00FFFF>  Right-click to sell")
                    ));
                    display.setItemMeta(meta);
                }
                gui.setItem(item.getSlot(), display);
            }
        }

        gui.setItem(45, createNavItem(Material.ARROW, translateHexColor("<#00FF00>Previous Page")));
        gui.setItem(49, createNavItem(Material.BARRIER, translateHexColor("<#FF0000>Close")));
        gui.setItem(53, createNavItem(Material.ARROW, translateHexColor("<#00FF00>Next Page")));

        player.openInventory(gui);
    }

    private static ItemStack createNavItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String capitalizeWords(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private static String translateHexColor(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = toLegacyHex(hex);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String toLegacyHex(String hex) {
        char[] chars = hex.toCharArray();
        return "§x" +
                "§" + chars[0] +
                "§" + chars[1] +
                "§" + chars[2] +
                "§" + chars[3] +
                "§" + chars[4] +
                "§" + chars[5];
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInventory = event.getView().getTopInventory();
        String title = event.getView().getTitle();

        boolean isShopSections = title.equals(translateHexColor("<#4B0082>Shop Sections (Page 1)")) ||
                title.startsWith(translateHexColor("<#4B0082>Shop Sections (Page "));
        boolean isShopGUI = title.startsWith(translateHexColor("<#228B22>Shop: "));

        if (!isShopSections && !isShopGUI) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (isShopSections) {
            int currentPage = extractPageNumber(title);

            switch (clicked.getType()) {
                case BARRIER -> {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                }
                case ARROW -> {
                    if (itemName.equalsIgnoreCase("Previous Page")) openSectionSelector(player, currentPage - 1);
                    else if (itemName.equalsIgnoreCase("Next Page")) openSectionSelector(player, currentPage + 1);
                }
                default -> {
                    if (itemName.equalsIgnoreCase("Spawners [Disabled by the Dev]")) {
                        player.sendMessage(translateHexColor("<#FF5555>This section is currently disabled by the developer."));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    openShopGUI(player, itemName, 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
                }

            }
            return;
        }

        if (isShopGUI) {
            String[] parts = title.replace(translateHexColor("<#228B22>Shop: "), "").split(" \\(Page ");
            if (parts.length != 2) return;

            String section = parts[0];
            int page;
            try {
                page = Integer.parseInt(parts[1].replace(")", ""));
            } catch (NumberFormatException e) {
                return;
            }

            if (clicked.getType() == Material.BARRIER && itemName.equalsIgnoreCase("Close")) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return;
            }

            if (clicked.getType() == Material.ARROW) {
                if (itemName.equalsIgnoreCase("Next Page")) openShopGUI(player, section, page + 1);
                else if (itemName.equalsIgnoreCase("Previous Page") && page > 1) openShopGUI(player, section, page - 1);
                return;
            }

            for (ShopItem shopItem : ShopManager.getItems(section)) {
                if (shopItem.getPage() != page || shopItem.getSlot() != event.getSlot()) continue;
                if (shopItem.getItem().getType() != clicked.getType()) continue;

                if (event.getClick().isLeftClick()) {
                    double price = shopItem.getBuyPrice();
                    if (EconomyManager.getBalance(player) < price) {
                        player.sendMessage(translateHexColor("<#FF0000>You don't have enough money to buy this!"));
                        return;
                    }

                    EconomyManager.takeBalance(player, price);
                    player.getInventory().addItem(shopItem.getItem().clone());
                    player.sendMessage(translateHexColor("<#00FF00>You bought " +
                            "<#FFD700>" + shopItem.getItem().getType().name().replace("_", " ") +
                            "<#00FF00> for $" + price));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                } else if (event.getClick().isRightClick()) {
                    ItemStack sellingItem = shopItem.getItem().clone();
                    sellingItem.setAmount(1);

                    if (!player.getInventory().containsAtLeast(sellingItem, 1)) {
                        player.sendMessage(translateHexColor("<#FF0000>You don't have this item to sell!"));
                        return;
                    }

                    player.getInventory().removeItem(sellingItem);
                    EconomyManager.addBalance(player, shopItem.getSellPrice());
                    player.sendMessage(translateHexColor("<#00FF00>You sold " +
                            "<#FFD700>" + sellingItem.getType().name().replace("_", " ") +
                            "<#00FF00> for $" + shopItem.getSellPrice()));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                return;
            }
        }
    }

    private int extractPageNumber(String title) {
        int page = 1;
        if (title.contains("(Page ")) {
            try {
                page = Integer.parseInt(title.split("\\(Page ")[1].replace(")", ""));
            } catch (NumberFormatException ignored) {}
        }
        return page;
    }
}
