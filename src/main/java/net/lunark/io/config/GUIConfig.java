package net.lunark.io.config;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.lunark.io.pw.PlayerWarp;
import net.lunark.io.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class GUIConfig {

    private FileConfiguration config;  // Removed final

    public GUIConfig(FileConfiguration config) {
        this.config = config;
        loadDefaults();
    }

    private void loadDefaults() {
        if (!config.contains("gui.editwarp.title"))
            config.set("gui.editwarp.title", "&eEdit Warp: %warpname%");

        String basePath = "gui.editwarp.items.";

        if (!config.contains(basePath + "name.material")) config.set(basePath + "name.material", "NAME_TAG");
        if (!config.contains(basePath + "name.displayname")) config.set(basePath + "name.displayname", "&bChange Name");
        if (!config.contains(basePath + "name.lore")) config.set(basePath + "name.lore", List.of("&7Current: &f%current%"));

        if (!config.contains(basePath + "description.material")) config.set(basePath + "description.material", "PAPER");
        if (!config.contains(basePath + "description.displayname")) config.set(basePath + "description.displayname", "&bChange Description");
        if (!config.contains(basePath + "description.lore")) config.set(basePath + "description.lore", List.of("&7Current: &f%current%"));

        if (!config.contains(basePath + "category.material")) config.set(basePath + "category.material", "BOOK");
        if (!config.contains(basePath + "category.displayname")) config.set(basePath + "category.displayname", "&bChange Category");
        if (!config.contains(basePath + "category.lore")) config.set(basePath + "category.lore", List.of("&7Current: &f%current%"));

        if (!config.contains(basePath + "icon.material")) config.set(basePath + "icon.material", "PLAYER_HEAD");
        if (!config.contains(basePath + "icon.displayname")) config.set(basePath + "icon.displayname", "&bChange Icon");
        if (!config.contains(basePath + "icon.lore")) config.set(basePath + "icon.lore", List.of("&7Click with any item in hand"));

        if (!config.contains(basePath + "location.material")) config.set(basePath + "location.material", "COMPASS");
        if (!config.contains(basePath + "location.displayname")) config.set(basePath + "location.displayname", "&bSet New Location");
        if (!config.contains(basePath + "location.lore")) config.set(basePath + "location.lore", List.of("&7Current location will be updated"));

        if (!config.contains(basePath + "cooldown.material")) config.set(basePath + "cooldown.material", "CLOCK");
        if (!config.contains(basePath + "cooldown.displayname")) config.set(basePath + "cooldown.displayname", "&bSet Cooldown");
        if (!config.contains(basePath + "cooldown.lore")) config.set(basePath + "cooldown.lore", List.of("&7Current: &f%current%s"));

        if (!config.contains("gui.category.title"))
            config.set("gui.category.title", "§9Choose a Category");

        if (!config.contains("gui.category.item.material"))
            config.set("gui.category.item.material", "BOOK");

        if (!config.contains("gui.category.item.displayname"))
            config.set("gui.category.item.displayname", "§b%category%");

        if (!config.contains("categories"))
            config.set("categories", List.of("Default", "Admin", "Fun"));

        if (!config.contains("gui.warplist.title"))
            config.set("gui.warplist.title", "&aWarps: %category%");

        String base = "gui.warplist.item.";
        if (!config.contains(base + "material")) config.set(base + "material", "ENDER_PEARL");
        if (!config.contains(base + "displayname")) config.set(base + "displayname", "&f%warpname%");
        if (!config.contains(base + "lore")) config.set(base + "lore", List.of(
                "&7Owner: &f%owner%",
                "&7%description%"
        ));
    }

    public String getWarpListTitle(String category) {
        return ChatUtil.color(config.getString("gui.warplist.title", "&aWarps: %category%").replace("%category%", category));
    }

    public String getCategoryGUITitle() {
        return ChatUtil.color(config.getString("gui.category.title", "§9Choose a Category"));
    }

    public List<String> getCategories() {
        return config.getStringList("categories");
    }

    public ItemStack getCategoryItem(String category) {
        Material material = Material.getMaterial(config.getString("gui.category.item.material", "BOOK"));
        if (material == null) material = Material.BOOK;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString("gui.category.item.displayname", "§b%category%");
        displayName = displayName.replace("%category%", category);

        meta.setDisplayName(ChatUtil.color(displayName));
        item.setItemMeta(meta);

        return item;
    }

    public String getWarpsGUITitlePrefix() {
        return ChatUtil.color(config.getString("gui.warplist.title-prefix", "§aWarps: "));
    }

    public void reload(FileConfiguration newConfig) {
        this.config = newConfig;
        loadDefaults();
    }

    public String getMessage(String key, String defaultMsg) {
        return ChatUtil.color(config.getString("messages." + key, defaultMsg));
    }

    public String getEditWarpGUITitlePrefix() {
        return ChatUtil.color(config.getString("gui.editwarp.title-prefix", "§eEdit Warp: "));
    }

    public ItemStack getWarpListItem(PlayerWarp warp) {
        String base = "gui.warplist.item.";
        Material material = Material.getMaterial(config.getString(base + "material", "ENDER_PEARL"));
        if (material == null) material = Material.ENDER_PEARL;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String displayName = config.getString(base + "displayname", "&f%warpname%");
        displayName = displayName.replace("%warpname%", warp.getName());
        meta.setDisplayName(ChatUtil.color(displayName));

        List<String> loreRaw = config.getStringList(base + "lore");
        List<String> lore = new ArrayList<>();

        String ownerName = Bukkit.getOfflinePlayer(warp.getOwner()).getName();
        String description = (warp.getDescription() != null && !warp.getDescription().isEmpty()) ? warp.getDescription() : "";

        for (String line : loreRaw) {
            line = line.replace("%owner%", ownerName != null ? ownerName : "Unknown")
                    .replace("%description%", description);
            lore.add(ChatUtil.color(line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public String getEditWarpTitle(String warpName) {
        return ChatUtil.color(config.getString("gui.editwarp.title", "&eEdit Warp: %warpname%").replace("%warpname%", warpName));
    }

    public ItemStack getEditWarpItem(String key, String currentValue) {
        String base = "gui.editwarp.items." + key + ".";
        Material material = Material.getMaterial(config.getString(base + "material", "PAPER"));
        if (material == null) material = Material.PAPER;

        String displayName = config.getString(base + "displayname", "Item");
        List<String> loreRaw = config.getStringList(base + "lore");

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        List<String> lore = new ArrayList<>();
        for (String line : loreRaw) {
            lore.add(ChatUtil.color(line.replace("%current%", currentValue == null ? "" : currentValue)));
        }

        meta.setDisplayName(ChatUtil.color(displayName));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}