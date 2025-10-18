package serveressentials.serveressentials.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import serveressentials.serveressentials.util.ServerMessages;

import java.time.Duration;

public class RebootCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ServerMessages messages;

    public RebootCommand(JavaPlugin plugin, ServerMessages messages) {
        this.plugin = plugin;
        this.messages = messages;

        // Add defaults if missing
        messages.addDefault("reboot.usage", "<red>Usage: /reboot <minutes>");
        messages.addDefault("reboot.announce-scheduled", "<red>⚠ Server will reboot in <time> minute(s).");
        messages.addDefault("reboot.announce-minutes", "<yellow>⏳ Reboot in <time> minute(s).");
        messages.addDefault("reboot.announce-seconds", "<gold>⏳ Reboot in <time> second(s).");
        messages.addDefault("reboot.announce-now", "<red><bold>⚠ Rebooting now!");

        messages.addDefault("titles.minute-warning", "<yellow>⏳ Reboot in <time> minute(s).");
        messages.addDefault("titles.second-warning", "<gold><bold><time></bold>");
        messages.addDefault("titles.reboot-now", "<red><bold>⚠ Rebooting now!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(messages.get("reboot.usage"));
            return true;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("reboot.usage"));
            return true;
        }

        int seconds = minutes * 60;

        // Initial announcement
        broadcastMessage("reboot.announce-scheduled", minutes);

        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining == 0) {
                    broadcastMessage("reboot.announce-now", 0);
                    broadcastTitle("titles.reboot-now", 0, Duration.ofSeconds(2));
                    playEffects();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    cancel();
                    return;
                }

                // Minute warnings: 1h, 30m, 10m, 5m, 1m
                if (remaining == 3600 || remaining == 1800 || remaining == 600 || remaining == 300 || remaining == 60) {
                    broadcastMessage("reboot.announce-minutes", remaining / 60);
                    broadcastTitle("titles.minute-warning", remaining / 60, Duration.ofSeconds(2));
                }

                // Second warnings: 3, 2, 1
                if (remaining <= 3 && remaining > 0) {
                    broadcastMessage("reboot.announce-seconds", remaining);
                    broadcastTitle("titles.second-warning", remaining, Duration.ofSeconds(2));
                    playEffects();
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private void broadcastMessage(String path, int time) {
        Component msg = messages.get(path).replaceText(builder ->
                builder.match("<time>").replacement(String.valueOf(time)));
        Bukkit.broadcast(msg);
    }

    private void broadcastTitle(String path, int time, Duration stay) {
        Component title = messages.get(path).replaceText(builder ->
                builder.match("<time>").replacement(String.valueOf(time)));

        for (Player player : Bukkit.getOnlinePlayers()) {
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
