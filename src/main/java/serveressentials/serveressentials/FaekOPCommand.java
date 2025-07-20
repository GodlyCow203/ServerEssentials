package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class FaekOPCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (args.length != 1) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /fakeop <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
            return true;
        }

        String fakeMessage = ChatColor.GRAY + "[Server: Made " + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " a server operator]";

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(fakeMessage);
        }

        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        sender.sendMessage(prefix + ChatColor.GREEN + "Fake OP sent to " + ChatColor.YELLOW + target.getName());

        return true;
    }
}
