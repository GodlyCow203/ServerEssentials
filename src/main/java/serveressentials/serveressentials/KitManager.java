package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitManager {

    private static final Map<String, Kit> kits = new LinkedHashMap<>();

    public static void loadKits(FileConfiguration config) {
        kits.clear();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        for (String rawKitName : section.getKeys(false)) {
            String kitName = rawKitName.toLowerCase(); // Normalize key

            ConfigurationSection kitSection = section.getConfigurationSection(rawKitName);
            if (kitSection == null) continue;

            String permission = kitSection.getString("permission", null);
            String displayName = ColorUtils.color(kitSection.getString("display.name", rawKitName));
            String materialName = kitSection.getString("display.material", "SHULKER_BOX");
            Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material == null) material = Material.SHULKER_BOX;

            List<String> lore = new ArrayList<>();
            for (String line : kitSection.getStringList("display.lore")) {
                lore.add(ColorUtils.color(line));
            }

            int slot = kitSection.contains("display.slot") ? kitSection.getInt("display.slot") : 0;

            List<ItemStack> items = new ArrayList<>();
            for (String itemString : kitSection.getStringList("items")) {
                ItemStack parsed = parseItemString(itemString);
                if (parsed != null) {
                    items.add(parsed);
                } else {
                    Bukkit.getLogger().warning("[ServerEssentials] Failed to parse item for kit '" + kitName + "': " + itemString);
                }
            }

            // Support for preview slot-based items (if needed in future)
            if (kitSection.isConfigurationSection("preview")) {
                ConfigurationSection previewSection = kitSection.getConfigurationSection("preview");
                for (String key : previewSection.getKeys(false)) {
                    try {
                        int previewSlot = Integer.parseInt(key);
                        ItemStack parsed = parseItemString(previewSection.getString(key));
                        if (parsed != null) {
                            while (items.size() <= previewSlot) items.add(null);
                            items.set(previewSlot, parsed);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            Kit kit = new Kit(kitName, permission, displayName, material, lore, slot, items);
            kits.put(kitName, kit);

            Bukkit.getLogger().info("[ServerEssentials] Loaded kit: " + rawKitName + " (slot " + slot + ")");
        }
    }

    public static Map<String, Kit> getKits() {
        return kits;
    }

    public static Kit getKit(String name) {
        if (name == null) return null;
        return kits.get(name.toLowerCase());
    }

    public static ItemStack parseItemString(String input) {
        try {
            String[] parts = input.trim().split("[\\s:]+");
            if (parts.length == 0) return null;

            Material material = Material.matchMaterial(parts[0].toUpperCase());
            if (material == null) return null;

            int amount = 1;
            int index = 1;

            if (parts.length > 1 && !parts[1].equalsIgnoreCase("ENCHANT")) {
                try {
                    amount = Integer.parseInt(parts[1]);
                    index++;
                } catch (NumberFormatException ignored) {
                }
            }

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            while (index < parts.length) {
                if (parts[index].equalsIgnoreCase("ENCHANT") && index + 2 < parts.length) {
                    Enchantment enchant = Enchantment.getByName(parts[index + 1].toUpperCase());
                    int level = Integer.parseInt(parts[index + 2]);
                    if (enchant != null && meta != null) {
                        meta.addEnchant(enchant, level, true);
                    }
                    index += 3;
                } else {
                    index++;
                }
            }

            if (meta != null) item.setItemMeta(meta);
            return item;

        } catch (Exception e) {
            Bukkit.getLogger().warning("[ServerEssentials] Error parsing item string: " + input);
            e.printStackTrace();
            return null;
        }
    }
}
