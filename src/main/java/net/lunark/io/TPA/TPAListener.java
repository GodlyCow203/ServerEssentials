package net.lunark.io.TPA;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TPAListener implements Listener {
    private final TPAConfig config;
    private final PlayerLanguageManager langManager;
    private final Map<UUID, Integer> warmupTasks = new ConcurrentHashMap<>();

    public TPAListener(TPAConfig config, PlayerLanguageManager langManager) {
        this.config = config;
        this.langManager = langManager;
    }

    public void registerWarmupTask(UUID playerId, int taskId) {
        warmupTasks.put(playerId, taskId);
    }

    public void unregisterWarmupTask(UUID playerId) {
        warmupTasks.remove(playerId);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!config.cancelOnMove) return;

        Player player = e.getPlayer();
        boolean blockLevel = config.blockMoveThresholdBlocks;
        boolean moved = blockLevel ?
                !e.getFrom().getBlock().equals(e.getTo().getBlock()) :
                !e.getFrom().toVector().equals(e.getTo().toVector());

        if (!moved) return;

        Integer taskId = warmupTasks.get(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            warmupTasks.remove(player.getUniqueId());

            player.sendMessage(langManager.getMessageFor(player,
                    "tpa.teleport-cancelled-move",
                    "<red>Teleport cancelled due to movement.",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{player}", player.getName())
            ));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        warmupTasks.remove(e.getPlayer().getUniqueId());
    }
}