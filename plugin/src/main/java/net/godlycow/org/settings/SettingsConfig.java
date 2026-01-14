package net.godlycow.org.settings;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class SettingsConfig {
    private final Plugin plugin;
    private final List<SettingsButton> buttons = new ArrayList<>();

    private String titleKey;
    private int rows;
    private boolean fillBorder;
    private Material fillMaterial;
    private Material borderMaterial;

    private boolean paginationEnabled;
    private int itemsPerPage;
    private int previousSlot;
    private int nextSlot;
    private Material previousMaterial;
    private Material nextMaterial;
    private String previousNameKey;
    private String nextNameKey;

    public SettingsConfig(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection settingsSection = config.getConfigurationSection("settings");

        if (settingsSection == null) {
            plugin.getLogger().warning("No 'settings' section found in config.yml");
            return;
        }

        buttons.clear();
        loadGUIConfig(settingsSection.getConfigurationSection("gui"));
        loadPaginationConfig(settingsSection.getConfigurationSection("pagination"));
        loadButtons(settingsSection.getConfigurationSection("buttons"));
    }

    private void loadGUIConfig(ConfigurationSection guiSection) {
        if (guiSection == null) {
            titleKey = "commands.settings.gui.title";
            rows = 6;
            fillBorder = true;
            fillMaterial = Material.GRAY_STAINED_GLASS_PANE;
            borderMaterial = Material.BLACK_STAINED_GLASS_PANE;
            return;
        }

        titleKey = guiSection.getString("title", "commands.settings.gui.title");
        rows = Math.max(1, Math.min(6, guiSection.getInt("rows", 6)));
        fillBorder = guiSection.getBoolean("fill-border", true);
        fillMaterial = Material.matchMaterial(guiSection.getString("fill-material", "GRAY_STAINED_GLASS_PANE"));
        borderMaterial = Material.matchMaterial(guiSection.getString("border-material", "BLACK_STAINED_GLASS_PANE"));

        if (fillMaterial == null) fillMaterial = Material.GRAY_STAINED_GLASS_PANE;
        if (borderMaterial == null) borderMaterial = Material.BLACK_STAINED_GLASS_PANE;
    }

    private void loadPaginationConfig(ConfigurationSection paginationSection) {
        if (paginationSection == null) {
            paginationEnabled = true;
            itemsPerPage = 21;
            previousSlot = 48;
            nextSlot = 50;
            previousMaterial = Material.ARROW;
            nextMaterial = Material.ARROW;
            previousNameKey = "commands.settings.gui.previous";
            nextNameKey = "commands.settings.gui.next";
            return;
        }

        paginationEnabled = paginationSection.getBoolean("enabled", true);
        itemsPerPage = Math.max(1, paginationSection.getInt("items-per-page", 21));
        previousSlot = paginationSection.getInt("previous-slot", 48);
        nextSlot = paginationSection.getInt("next-slot", 50);
        previousMaterial = Material.matchMaterial(paginationSection.getString("previous-material", "ARROW"));
        nextMaterial = Material.matchMaterial(paginationSection.getString("next-material", "ARROW"));
        previousNameKey = paginationSection.getString("previous-name", "commands.settings.gui.previous");
        nextNameKey = paginationSection.getString("next-name", "commands.settings.gui.next");

        if (previousMaterial == null) previousMaterial = Material.ARROW;
        if (nextMaterial == null) nextMaterial = Material.ARROW;
    }

    private void loadButtons(ConfigurationSection buttonsSection) {
        if (buttonsSection == null) {
            plugin.getLogger().warning("No 'settings.buttons' section found in config.yml");
            return;
        }

        for (String key : buttonsSection.getKeys(false)) {
            ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(key);
            if (buttonSection == null) continue;

            try {
                String materialName = buttonSection.getString("material", "STONE");
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning("Invalid material '" + materialName + "' for button '" + key + "'");
                    continue;
                }

                SettingsButton button = new SettingsButton(
                        key,
                        buttonSection.getInt("position", buttons.size()),
                        material,
                        buttonSection.getString("name-key", "commands.settings.buttons." + key + ".name"),
                        buttonSection.getStringList("lore-keys"),
                        buttonSection.getBoolean("glow", false),
                        buttonSection.getString("command", ""),
                        buttonSection.getString("permission", null)
                );

                buttons.add(button);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load button '" + key + "'", e);
            }
        }

        buttons.sort((a, b) -> Integer.compare(a.getPosition(), b.getPosition()));
        plugin.getLogger().info("Loaded " + buttons.size() + " settings buttons");
    }

    public List<SettingsButton> getButtons() { return List.copyOf(buttons); }
    public String getTitleKey() { return titleKey; }
    public int getRows() { return rows; }
    public int getSize() { return rows * 9; }
    public boolean shouldFillBorder() { return fillBorder; }
    public Material getFillMaterial() { return fillMaterial; }
    public Material getBorderMaterial() { return borderMaterial; }
    public boolean isPaginationEnabled() { return paginationEnabled && buttons.size() > itemsPerPage; }
    public int getItemsPerPage() { return itemsPerPage; }
    public int getPreviousSlot() { return previousSlot; }
    public int getNextSlot() { return nextSlot; }
    public Material getPreviousMaterial() { return previousMaterial; }
    public Material getNextMaterial() { return nextMaterial; }
    public String getPreviousNameKey() { return previousNameKey; }
    public String getNextNameKey() { return nextNameKey; }

    public List<SettingsButton> getButtonsForPage(int page) {
        if (!paginationEnabled) return getButtons();

        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, buttons.size());

        if (start >= buttons.size()) return List.of();
        return buttons.subList(start, end);
    }

    public int getTotalPages() {
        if (!paginationEnabled) return 1;
        return (int) Math.ceil((double) buttons.size() / itemsPerPage);
    }

    public boolean hasPreviousPage(int page) {
        return page > 0;
    }

    public boolean hasNextPage(int page) {
        return page < getTotalPages() - 1;
    }
}