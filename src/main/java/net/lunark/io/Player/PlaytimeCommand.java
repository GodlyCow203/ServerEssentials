package net.lunark.io.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.util.PlayerMessages;

public class PlaytimeCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public PlaytimeCommand(PlayerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Playtime.not-a-player"));
            return true;
        }

        if (args.length > 0) {
            sender.sendMessage(messages.get("Playtime.usage"));
            return true;
        }

        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long minutes = ticks / (20 * 60);
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        Component playtimeMsg = messages.get(
                "Playtime.playtime",
                "{player}", player.getName(),
                "{hours}", String.valueOf(hours),
                "{minutes}", String.valueOf(remainingMinutes)
        );

        player.sendMessage(playtimeMsg);
        return true;
    }
}
