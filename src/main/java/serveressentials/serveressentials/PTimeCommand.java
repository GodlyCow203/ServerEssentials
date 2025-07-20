package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PTimeCommand implements CommandExecutor {

    // Dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /ptime <day|night|reset>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "day" -> player.setPlayerTime(1000, false);
                case "night" -> player.setPlayerTime(13000, false);
                case "reset" -> player.resetPlayerTime();
                default -> {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Invalid argument! Use day, night, or reset.");
                    return true;
                }
            }

            player.sendMessage(getPrefix() + ChatColor.YELLOW + "Your personal time has been updated.");
        } else {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
        }
        return true;
    }
}
