package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.RebootConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class RebootCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.reboot";

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RebootConfig config;

    public RebootCommand(Plugin plugin, PlayerLanguageManager langManager, RebootConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.reboot.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.reboot.usage",
                    "<red>Usage: /reboot <minutes>"));
            return true;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.reboot.usage",
                    "<red>Usage: /reboot <minutes>"));
            return true;
        }

        int seconds = minutes * 60;
        broadcastMessage("commands.reboot.announce-scheduled", minutes);

        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining == 0) {
                    broadcastMessage("commands.reboot.announce-now", 0);
                    broadcastTitle("commands.reboot.titles.now", 0, Duration.ofSeconds(2));
                    playEffects();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    cancel();
                    return;
                }

                if (remaining == 3600 || remaining == 1800 || remaining == 600 || remaining == 300 || remaining == 60) {
                    int timeInMinutes = remaining / 60;
                    broadcastMessage("commands.reboot.announce-minutes", timeInMinutes);
                    broadcastTitle("commands.reboot.titles.minute-warning", timeInMinutes, Duration.ofSeconds(2));
                }

                if (remaining <= 3 && remaining > 0) {
                    broadcastMessage("commands.reboot.announce-seconds", remaining);
                    broadcastTitle("commands.reboot.titles.second-warning", remaining, Duration.ofSeconds(2));
                    playEffects();
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void broadcastMessage(String key, int time) {
        Component message = langManager.getMessageFor(null, key,
                "<red>⚠ Server will reboot in {time} minute(s).",
                ComponentPlaceholder.of("{time}", String.valueOf(time)));
        Bukkit.broadcast(message);
    }

    private void broadcastTitle(String key, int time, Duration stay) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Component title = langManager.getMessageFor(player, key,
                    "<yellow>⏳ Reboot in {time} minute(s).",
                    ComponentPlaceholder.of("{time}", String.valueOf(time)));

            player.showTitle(Title.title(
                    title,
                    Component.empty(),
                    Title.Times.times(Duration.ofMillis(500), stay, Duration.ofMillis(500))
            ));
        }
    }

    private void playEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        }
    }
}