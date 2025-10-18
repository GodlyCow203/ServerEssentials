package serveressentials.serveressentials.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopGUIManager {

    private static final MiniMessage mini = MiniMessage.miniMessage();
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    private static Economy economy;
    private static FileConfiguration messages;
    private static MainShopConfig mainConfig;
    public static final Map<String, ShopSectionConfig> sectionConfigs = new HashMap<>();
    public static final Map<UUID, String> openSection = new HashMap<>();
    public static final Map<UUID, Integer> currentPage = new HashMap<>();

    /** Initialize ShopGUI with Vault economy and data folder */
    public static void init(Economy eco, File dataFolder) {
        economy = eco;

        File messagesFile = new File(dataFolder, "messages/shop.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            Bukkit.getPluginManager().getPlugin("ServerEssentials").saveResource("messages/shop.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /** Load main config and all section configs */
    public static void loadShopConfigs(File folder) {
        File mainFile = new File(folder, "main.yml");
        mainConfig = ShopConfigLoader.loadMainConfig(mainFile);
        sectionConfigs.clear();

        File[] sectionFiles = folder.listFiles(f -> f.getName().endsWith(".yml") && !f.getName().equals("config.yml"));
        if (sectionFiles != null) {
            for (File file : sectionFiles) {
                ShopSectionConfig section = ShopConfigLoader.loadSectionConfig(file);
                sectionConfigs.put(file.getName(), section);
            }
        }

    }

    public static void reload(File dataFolder) {
        File messagesFile = new File(dataFolder, "messages/shop.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            Bukkit.getPluginManager().getPlugin("ServerEssentials").saveResource("messages/shop.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        File shopFolder = new File(dataFolder, "shop");
        loadShopConfigs(shopFolder);

        Bukkit.getLogger().info("[ServerEssentials] Shop GUI configs reloaded.");
    }

    public static void refreshOpenInventories() {
        for (UUID uuid : new HashSet<>(openSection.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.getOpenInventory() != null) {
                String fileName = openSection.get(uuid);
                int page = currentPage.getOrDefault(uuid, 1);
                if (fileName == null) {
                    openMainGUI(player);
                } else {
                    openSectionGUI(player, fileName, page);
                }
            }
        }
    }


    /** Open main shop GUI */
    public static void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, mainConfig.size, color(mainConfig.title));

        mainConfig.layout.forEach((slot, deco) ->
                inv.setItem(slot, createItem(deco.material, deco.name, null))
        );

        mainConfig.sectionButtons.forEach((slot, button) ->
                inv.setItem(slot, createItem(button.material, button.name, button.lore))
        );

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        meta.setDisplayName(color("#FF0000Close"));
        close.setItemMeta(meta);
        inv.setItem(49, close);

        player.openInventory(inv);
        openSection.remove(player.getUniqueId());
    }

    /** Open section GUI (with page support) */
    public static void openSectionGUI(Player player, String fileName, int page) {
        ShopSectionConfig section = sectionConfigs.get(fileName);
        if (section == null) {
            player.sendMessage(msg("errors.no-section", Map.of("file", fileName)));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, section.size, color(section.title));

        // Add layout items
        section.layout.forEach((slot, item) ->
                inv.setItem(slot, createItem(item.material, item.name, item.lore))
        );

        // Add page items
        section.items.values().stream()
                .filter(item -> item.page == page)
                .forEach(item -> inv.setItem(item.slot, createItem(item.material, item.name, item.lore, item.amount)));

        // Player head showing balance
        if (section.playerHeadSlot >= 0) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            sm.setOwningPlayer(player);
            sm.setDisplayName(color("#1BFF00Your Balance"));
            sm.setLore(List.of(color("#3C9C36Balance: #1BFF00" + String.format("%.2f", economy.getBalance(player)) + " $")));
            skull.setItemMeta(sm);
            inv.setItem(section.playerHeadSlot, skull);
        }

        // Close button
        if (section.closeButtonSlot >= 0) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta metaClose = barrier.getItemMeta();
            metaClose.setDisplayName(color("#FF0000Back"));
            barrier.setItemMeta(metaClose);
            inv.setItem(section.closeButtonSlot, barrier);
        }

        // Navigation arrows
        if (page > 1) inv.setItem(45, createItem(Material.ARROW, color("#AAAAAAPrevious Page"), null));
        if (page < section.pages) inv.setItem(53, createItem(Material.ARROW, color("#AAAAAANext Page"), null));

        player.openInventory(inv);
        openSection.put(player.getUniqueId(), fileName);
        currentPage.put(player.getUniqueId(), page);
    }

    public static void openSectionGUI(Player player, String fileName) {
        openSectionGUI(player, fileName, 1);
    }

    /** Handle clicks in shop GUIs */
    public static void handleClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        UUID uuid = player.getUniqueId();
        String sectionFile = openSection.get(uuid);

        String title = LegacyComponentSerializer.legacySection().serialize(e.getView().title());
        boolean isMainShop = title.equals(color(mainConfig.title)); // exact match
        boolean isSection = sectionFile != null && sectionConfigs.containsKey(sectionFile);

        // Only process clicks in our shop GUIs
        if (!isMainShop && !isSection) return;

        // Cancel clicks only in the shop GUI (top inventory)
        if (e.getClickedInventory() == e.getView().getTopInventory()) {
            e.setCancelled(true);
        } else {
            return; // ignore clicks in the player inventory
        }

        // ----- Main shop GUI clicks -----
        if (isMainShop) {
            int slot = e.getSlot();
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            MainShopConfig.ShopSectionButton button = mainConfig.sectionButtons.get(slot);
            if (button != null) openSectionGUI(player, button.file);
            return;
        }

        // ----- Section GUI clicks -----
        ShopSectionConfig section = sectionConfigs.get(sectionFile);
        int page = currentPage.getOrDefault(uuid, 1);
        int slot = e.getSlot();

        // Page navigation
        if (slot == 45 && page > 1) { openSectionGUI(player, sectionFile, page - 1); return; }
        if (slot == 53 && page < section.pages) { openSectionGUI(player, sectionFile, page + 1); return; }

        // Close button
        if (e.getCurrentItem().getType() == Material.BARRIER) { openMainGUI(player); return; }

        // Buy/Sell items
        for (ShopSectionConfig.ShopItem item : section.items.values()) {
            if (item.page == page && item.slot == slot && item.clickable) {
                boolean left = e.getClick() == ClickType.LEFT;
                boolean right = e.getClick() == ClickType.RIGHT;

                if (left && item.buyPrice > 0) {
                    if (economy.withdrawPlayer(player, item.buyPrice).transactionSuccess()) {
                        player.getInventory().addItem(new ItemStack(item.material, item.amount));
                        player.sendMessage(msg("shop.buy", Map.of(
                                "item", item.name,
                                "price", String.format("%.2f", item.buyPrice)
                        )));
                        openSectionGUI(player, sectionFile, page);
                    } else {
                        player.sendMessage(msg("errors.cannot-afford", Map.of("item", item.name)));
                    }
                } else if (right && item.sellPrice > 0) {
                    if (player.getInventory().containsAtLeast(new ItemStack(item.material), item.amount)) {
                        removeItems(player, item.material, item.amount);
                        economy.depositPlayer(player, item.sellPrice);
                        player.sendMessage(msg("shop.sell", Map.of(
                                "item", item.name,
                                "price", String.format("%.2f", item.sellPrice)
                        )));
                        openSectionGUI(player, sectionFile, page);
                    } else {
                        player.sendMessage(msg("errors.no-items", Map.of("item", item.name)));
                    }
                }
                break;
            }
        }
    }


    private static void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == material) {
                int stackAmount = stack.getAmount();
                if (stackAmount <= remaining) {
                    player.getInventory().clear(i);
                    remaining -= stackAmount;
                } else {
                    stack.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
        player.updateInventory();
    }

    private static Component msg(String path, Map<String, String> placeholders) {
        String raw = messages.getString(path, path);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String value = e.getValue();
                if (value != null) {
                    // Remove any legacy & color codes
                    value = value.replaceAll("&[0-9a-fA-F]", "");
                }
                raw = raw.replace("%" + e.getKey() + "%", value);
            }
        }
        return mini.deserialize(raw);
    }


    private static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore, 1);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(color(name));
            if (lore != null) {
                List<String> formatted = new ArrayList<>();
                for (String line : lore) formatted.add(color(line));
                meta.setLore(formatted);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String color(String text) {
        if (text == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group().substring(1);
            StringBuilder repl = new StringBuilder("§x");
            for (char c : hex.toCharArray()) repl.append('§').append(c);
            matcher.appendReplacement(buffer, repl.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace("&", "§");
    }
}
