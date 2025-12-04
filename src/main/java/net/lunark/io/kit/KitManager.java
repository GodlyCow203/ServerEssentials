package net.lunark.io.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

public class KitManager {

    private static final Map<String, Kit> kits = new LinkedHashMap<>();

    public static void loadKits(FileConfiguration config) {
        kits.clear();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        for (String rawKitName : section.getKeys(false)) {
            String kitName = rawKitName.toLowerCase();
            ConfigurationSection kitSection = section.getConfigurationSection(rawKitName);
            if (kitSection == null) continue;

            try {
                String permission = kitSection.getString("permission", "");
                String displayName = kitSection.getString("display.name", rawKitName);
                String materialName = kitSection.getString("display.material", "SHULKER_BOX");
                Material material = Material.matchMaterial(materialName.toUpperCase());
                if (material == null) material = Material.SHULKER_BOX;

                List<String> lore = kitSection.getStringList("display.lore");
                int slot = kitSection.getInt("display.slot", -1);
                int cooldown = kitSection.getInt("cooldown", 0);

                List<ItemStack> items = new ArrayList<>();
                ConfigurationSection itemsSec = kitSection.getConfigurationSection("items");
                if (itemsSec != null) {
                    for (String key : itemsSec.getKeys(false)) {
                        ItemStack item = parseItemFromConfig(itemsSec.getConfigurationSection(key));
                        if (item != null) items.add(item);
                    }
                }

                Kit kit = new Kit(kitName, permission, displayName, material, lore, slot, items, cooldown);
                kits.put(kitName, kit);

            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to load kit '" + rawKitName + "'", e);
            }
        }

        Bukkit.getLogger().info("Loaded " + kits.size() + " kits.");
    }

    public static Map<String, Kit> getKits() {
        return Collections.unmodifiableMap(kits);
    }

    public static Kit getKit(String name) {
        return name != null ? kits.get(name.toLowerCase()) : null;
    }

    private static ItemStack parseItemFromConfig(ConfigurationSection section) {
        if (section == null) return null;

        try {
            Material material = Material.matchMaterial(section.getString("type", "STONE").toUpperCase());
            if (material == null) return null;

            int amount = section.getInt("amount", 1);
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                String displayName = section.getString("name");
                if (displayName != null) {
                    meta.setDisplayName(displayName);
                }

                List<String> lore = section.getStringList("lore");
                if (!lore.isEmpty()) {
                    meta.setLore(lore);
                }

                ConfigurationSection enchants = section.getConfigurationSection("enchantments");
                if (enchants != null) {
                    for (String enchantName : enchants.getKeys(false)) {
                        Enchantment enchant = Enchantment.getByName(enchantName.toUpperCase());
                        int level = enchants.getInt(enchantName, 1);
                        if (enchant != null) {
                            meta.addEnchant(enchant, level, true);
                        }
                    }
                }

                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error parsing kit item: " + e.getMessage());
            return null;
        }
    }
}