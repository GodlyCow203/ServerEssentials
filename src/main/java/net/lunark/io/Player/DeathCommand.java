package net.lunark.io.Player;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

public class DeathCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public DeathCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Death.only-players"));
            return true;
        }

        int deaths = player.getStatistic(Statistic.DEATHS);
        player.sendMessage(messages.get("Death.count", "{deaths}", String.valueOf(deaths)));

        return true;
    }
}
