package net.godlycow.org.settings;

import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SettingsGUI {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final SettingsConfig config;
    private final NamespacedKey buttonIdKey;
    private final NamespacedKey guiTypeKey;
    private final Set<Integer> availableSlots = new HashSet<>();

    public SettingsGUI(Plugin plugin, PlayerLanguageManager langManager, SettingsConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.buttonIdKey = new NamespacedKey(plugin, "settings-button-id");
        this.guiTypeKey = new NamespacedKey(plugin, "settings-gui-type");
        calculateAvailableSlots();
    }

    private void calculateAvailableSlots() {
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                availableSlots.add(row * 9 + col);
            }
        }
    }

    public void open(Player player, int page) {
        String title = langManager.getString(String.valueOf(player), config.getTitleKey(), "<gold>Settings");
        Inventory gui = Bukkit.createInventory(
                new SettingsGUIHolder(page),
                config.getSize(),
                title
        );

        if (config.shouldFillBorder()) {
            addBorderItems(gui, player);
        }

        List<SettingsButton> pageButtons = config.getButtonsForPage(page);
        for (int i = 0; i < pageButtons.size() && i < config.getItemsPerPage(); i++) {
            SettingsButton button = pageButtons.get(i);
            if (!button.hasPermission() || player.hasPermission(button.getPermission())) {
                int slot = getSlotForPosition(i);
                gui.setItem(slot, createButtonItem(button, player));
            }
        }

        if (config.isPaginationEnabled()) {
            addNavigationItems(gui, player, page);
        }

        addCloseButton(gui, player);

        player.openInventory(gui);
    }

    private void addBorderItems(Inventory gui, Player player) {
        ItemStack filler = createFillerItem(player);
        ItemStack border = createBorderItem(player);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        int rows = config.getRows();
        for (int slot = 0; slot < rows * 9; slot++) {
            boolean isBorder = (slot < 9) || (slot >= (rows - 1) * 9) || (slot % 9 == 0) || (slot % 9 == 8);
            if (isBorder && gui.getItem(slot) != null) {
                gui.setItem(slot, border);
            }
        }
    }

    private ItemStack createFillerItem(Player player) {
        ItemStack item = new ItemStack(config.getFillMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBorderItem(Player player) {
        ItemStack item = new ItemStack(config.getBorderMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createButtonItem(SettingsButton button, Player player) {
        ItemStack item = new ItemStack(button.getMaterial());
        ItemMeta meta = item.getItemMeta();

        Component name = langManager.getMessageFor(player, button.getNameKey(), "<gold>" + button.getId());
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        for (String loreKey : button.getLoreKeys()) {
            Component loreLine = langManager.getMessageFor(player, loreKey, "");
            if (!loreLine.equals(Component.empty())) {
                lore.add(loreLine);
            }
        }
        meta.lore(lore.isEmpty() ? null : lore);

        if (button.hasGlow()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(buttonIdKey, PersistentDataType.STRING, button.getId());
        pdc.set(guiTypeKey, PersistentDataType.STRING, "settings");

        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationItems(Inventory gui, Player player, int page) {
        if (config.hasPreviousPage(page)) {
            ItemStack prev = new ItemStack(config.getPreviousMaterial());
            ItemMeta meta = prev.getItemMeta();
            Component name = langManager.getMessageFor(player, config.getPreviousNameKey(), "<gold>Previous Page");
            meta.displayName(name);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(guiTypeKey, PersistentDataType.STRING, "settings-navigation");
            pdc.set(new NamespacedKey(plugin, "navigation-type"), PersistentDataType.STRING, "previous");
            prev.setItemMeta(meta);
            gui.setItem(config.getPreviousSlot(), prev);
        }

        if (config.hasNextPage(page)) {
            ItemStack next = new ItemStack(config.getNextMaterial());
            ItemMeta meta = next.getItemMeta();
            Component name = langManager.getMessageFor(player, config.getNextNameKey(), "<gold>Next Page");
            meta.displayName(next.displayName());
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(guiTypeKey, PersistentDataType.STRING, "settings-navigation");
            pdc.set(new NamespacedKey(plugin, "navigation-type"), PersistentDataType.STRING, "next");
            next.setItemMeta(meta);
            gui.setItem(config.getNextSlot(), next);
        }

        ItemStack indicator = new ItemStack(Material.PAPER);
        ItemMeta meta = indicator.getItemMeta();
        Component name = langManager.getMessageFor(player, "commands.settings.gui.page-indicator",
                "<yellow>Page <white>{page}</white>/<white>{total}</white>",
                ComponentPlaceholder.of("{page}", page + 1),
                ComponentPlaceholder.of("{total}", config.getTotalPages()));
        meta.displayName(name);
        indicator.setItemMeta(meta);
        gui.setItem(49, indicator);
    }

    private void addCloseButton(Inventory gui, Player player) {
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta meta = close.getItemMeta();
        Component name = langManager.getMessageFor(player, "commands.settings.gui.close", "<red>Close");
        meta.displayName(name);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(guiTypeKey, PersistentDataType.STRING, "settings-close");
        close.setItemMeta(meta);
        gui.setItem(53, close);
    }

    private int getSlotForPosition(int position) {
        int[] slots = availableSlots.stream().sorted().mapToInt(Integer::intValue).toArray();
        return position < slots.length ? slots[position] : 10;
    }
}