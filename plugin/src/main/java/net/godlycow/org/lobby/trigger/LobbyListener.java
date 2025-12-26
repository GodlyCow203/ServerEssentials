package net.godlycow.org.lobby.trigger;

import net.godlycow.org.commands.config.LobbyConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.lobby.helper.AnimationHelper;
import net.godlycow.org.lobby.storage.LobbyStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class LobbyListener implements Listener {
    private final PlayerLanguageManager langManager;
    private final LobbyStorage storage;
    private final LobbyConfig config;

    public LobbyListener(PlayerLanguageManager langManager, LobbyStorage storage, LobbyConfig config) {
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.teleportOnJoin) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("serveressentials.command.lobby")) {
            return;
        }

        String worldKey = config.perWorld ? player.getWorld().getName() : "global";
        storage.getLobby(worldKey).thenAccept(optLocation -> {
            if (optLocation.isPresent()) {
                Location lobby = optLocation.get();
                Bukkit.getScheduler().runTask(config.plugin, () -> {
                    if (config.animationEnabled) {
                        AnimationHelper.playTeleportAnimation(config.plugin, player, config.animation);
                    }
                    player.teleport(lobby);
                    player.sendMessage(langManager.getMessageFor(player, "lobby.teleported",
                            "<green>Teleported to lobby!"));
                });
            }
        }).exceptionally(ex -> {
            config.plugin.getLogger().severe("Failed to teleport player on join: " + ex.getMessage());
            return null;
        });
    }
}