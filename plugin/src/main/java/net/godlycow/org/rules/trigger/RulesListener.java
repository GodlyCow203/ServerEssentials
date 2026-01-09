package net.godlycow.org.rules.trigger;

import net.godlycow.org.rules.gui.RulesGUI;
import net.godlycow.org.rules.storage.RulesStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.godlycow.org.commands.config.RulesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    private static final ConcurrentHashMap<UUID, Boolean> pendingAcceptance = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> lastReopenTime = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> spamCount = new ConcurrentHashMap<>();

    private static final int PREV_PAGE_SLOT = 46;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int INFO_SLOT = 49;

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
                }, 40L);
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();
        Component expectedTitle = langManager.getMessageFor(player, "commands.rules.gui.title", config.title());
        String expectedLegacy = LegacyComponentSerializer.legacySection().serialize(expectedTitle);

        if (!title.equals(expectedLegacy)) return;

        Long lastOpen = lastReopenTime.get(player.getUniqueId());
        int spamAttempts = spamCount.getOrDefault(player.getUniqueId(), 0);

        long baseCooldown = 2000;
        long maxCooldown = 10000;
        long cooldown = Math.min(baseCooldown * (long)Math.pow(2, spamAttempts), maxCooldown);

        if (lastOpen != null && System.currentTimeMillis() - lastOpen < cooldown) {
            return;
        }

        if (lastOpen != null && System.currentTimeMillis() - lastOpen < 1000) {
            spamCount.put(player.getUniqueId(), spamAttempts + 1);
        }

        if (config.forceAcceptance() && pendingAcceptance.containsKey(player.getUniqueId())) {
            storage.hasAcceptedRules(player.getUniqueId()).thenAccept(accepted -> {
                if (!accepted && player.isOnline()) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.rules.close.cannot-close",
                            "<red>⚠ You must accept the rules before playing!"));

                    long reopenDelay = Math.max(40L, cooldown / 50);
                    lastReopenTime.put(player.getUniqueId(), System.currentTimeMillis());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && pendingAcceptance.containsKey(player.getUniqueId())) {
                            gui.showRules(player);
                        }
                    }, reopenDelay);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        pendingAcceptance.remove(playerId);
        lastReopenTime.remove(playerId);
        spamCount.remove(playerId);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        Component expectedTitle = langManager.getMessageFor(player, "commands.rules.gui.title", config.title());
        String expectedLegacy = LegacyComponentSerializer.legacySection().serialize(expectedTitle);

        if (!title.equals(expectedLegacy)) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getSlot();
        Material itemType = item.getType();

        if (slot == config.acceptButtonSlot() && itemType == Material.matchMaterial(config.acceptButtonMaterial())) {
            gui.handleAccept(player);
        } else if (slot == config.declineButtonSlot() && itemType == Material.matchMaterial(config.declineButtonMaterial())) {
            gui.handleDecline(player);
        }

        else if (config.enablePagination()) {
            if (slot == PREV_PAGE_SLOT && itemType == Material.ARROW) {
                gui.handlePageChange(player, false);
            } else if (slot == NEXT_PAGE_SLOT && itemType == Material.ARROW) {
                gui.handlePageChange(player, true);
            }
        }
    }

    public static void removePending(UUID playerId) {
        pendingAcceptance.remove(playerId);
        spamCount.remove(playerId);
    }
}