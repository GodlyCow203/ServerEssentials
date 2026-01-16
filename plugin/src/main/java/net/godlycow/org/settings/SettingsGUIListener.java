package net.godlycow.org.settings;

import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SettingsGUIListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final SettingsConfig config;
    private final NamespacedKey buttonIdKey;
    private final NamespacedKey guiTypeKey;

    public SettingsGUIListener(Plugin plugin, PlayerLanguageManager langManager, SettingsConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.buttonIdKey = new NamespacedKey(plugin, "settings-button-id");
        this.guiTypeKey = new NamespacedKey(plugin, "settings-gui-type");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof SettingsGUIHolder guiHolder)) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (pdc.has(guiTypeKey, PersistentDataType.STRING)) {
            String guiType = pdc.get(guiTypeKey, PersistentDataType.STRING);

            if ("settings".equals(guiType) && pdc.has(buttonIdKey, PersistentDataType.STRING)) {
                handleButtonClick(player, pdc.get(buttonIdKey, PersistentDataType.STRING));
            } else if ("settings-navigation".equals(guiType)) {
                handleNavigationClick(player, pdc, guiHolder.getPage(), event.getSlot());
            } else if ("settings-close".equals(guiType)) {
                player.closeInventory();
            }
        }
    }

    private void handleButtonClick(Player player, String buttonId) {
        SettingsButton button = config.getButtons().stream()
                .filter(b -> b.getId().equals(buttonId))
                .findFirst()
                .orElse(null);

        if (button == null) {
            plugin.getLogger().warning("Unknown settings button: " + buttonId);
            return;
        }

        if (button.hasPermission() && !player.hasPermission(button.getPermission())) {
            Component message = langManager.getMessageFor(player, "commands.settings.no-permission",
                    "<red>You don't have permission to use this setting!",
                    ComponentPlaceholder.of("{permission}", button.getPermission()));
            player.sendMessage(message);
            return;
        }

        try {
            String command = button.getCommand().replace("{player}", player.getName());

            boolean success = player.performCommand(command);

            if (success) {
                Component message = langManager.getMessageFor(player, "commands.settings.command-success",
                        "<green>Setting updated successfully!");
                player.sendMessage(message);
            } else {
                Component message = langManager.getMessageFor(player, "commands.settings.command-failed",
                        "<red>Failed to execute command: <yellow>{command}</yellow>",
                        ComponentPlaceholder.of("{command}", command));
                player.sendMessage(message);
            }

            player.closeInventory();

        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    "Error executing settings command for player " + player.getName(), e);
            Component message = langManager.getMessageFor(player, "commands.settings.command-error",
                    "<red>An error occurred while executing the command.");
            player.sendMessage(message);
        }
    }

    private void handleNavigationClick(Player player, PersistentDataContainer pdc, int currentPage, int clickedSlot) {
        String navType = pdc.get(new NamespacedKey(plugin, "navigation-type"), PersistentDataType.STRING);

        if ("previous".equals(navType) && config.hasPreviousPage(currentPage)) {
            new SettingsGUI(plugin, langManager, config).open(player, currentPage - 1);
        } else if ("next".equals(navType) && config.hasNextPage(currentPage)) {
            new SettingsGUI(plugin, langManager, config).open(player, currentPage + 1);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

    }
}