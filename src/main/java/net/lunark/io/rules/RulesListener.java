package net.lunark.io.rules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lunark.io.commands.config.RulesConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RulesListener implements Listener {
    private final PlayerLanguageManager langManager;
    private final RulesStorage storage;
    private final RulesConfig config;
    private final RulesGUI gui;
    private final Plugin plugin;
    private final ConcurrentHashMap<UUID, Boolean> pendingAcceptance = new ConcurrentHashMap<>();

    public RulesListener(PlayerLanguageManager langManager, RulesStorage storage, RulesConfig config, Plugin plugin) {
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;
        this.gui = new RulesGUI(langManager, storage, config, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.mustAccept()) return;

        Player player = event.getPlayer();
        storage.hasAcceptedRules(player.getUniqueId()).thenAccept(accepted -> {
            if (!accepted) {
                pendingAcceptance.put(player.getUniqueId(), true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        gui.showRules(player);
                    }
                }, 20L);
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Check if this is our rules GUI
        String title = org.bukkit.ChatColor.stripColor(event.getView().getTitle());
        Component expectedTitleComponent = langManager.getMessageFor(player, "rules.gui.title", config.title());
        String expectedTitle = org.bukkit.ChatColor.stripColor(
                LegacyComponentSerializer.legacySection().serialize(expectedTitleComponent)
        );

        if (!title.equals(expectedTitle)) return;

        // If player hasn't accepted yet and force acceptance is enabled, reopen
        if (config.forceAcceptance() && pendingAcceptance.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !storage.hasAcceptedRules(player.getUniqueId()).join()) {
                    player.sendMessage(langManager.getMessageFor(player, "rules.close.cannot-close",
                            "<red>You must accept the rules before playing!"));
                    gui.showRules(player);
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingAcceptance.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is our rules GUI
        String title = org.bukkit.ChatColor.stripColor(event.getView().getTitle());
        Component expectedTitleComponent = langManager.getMessageFor(player, "rules.gui.title", config.title());
        String expectedTitle = org.bukkit.ChatColor.stripColor(
                LegacyComponentSerializer.legacySection().serialize(expectedTitleComponent)
        );

        if (!title.equals(expectedTitle)) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        switch (item.getType()) {
            case GREEN_WOOL:
                pendingAcceptance.remove(player.getUniqueId());
                gui.handleAccept(player);
                break;
            case RED_WOOL:
                gui.handleDecline(player);
                break;
            default:
                break;
        }
    }
}