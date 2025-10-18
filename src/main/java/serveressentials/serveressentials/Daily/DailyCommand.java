package serveressentials.serveressentials.Daily;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

public class DailyCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DailyCommand(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("serveressentials.daily")) {
            player.sendMessage(miniMessage.deserialize(plugin.getDailyRewards()
                    .getMessagesManager().get("no-permission", "%permission%", "serveressentials.daily")));
            return true;
        }

        plugin.getDailyRewards().openRewardsGUI(player, 1);

        return true;
    }
}
