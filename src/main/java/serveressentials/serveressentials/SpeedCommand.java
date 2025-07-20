package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class SpeedCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /speed [1-10]");
            return true;
        }

        try {
            int speed = Integer.parseInt(args[0]);
            if (speed < 1 || speed > 10) throw new NumberFormatException();

            float scaled = speed / 10.0f;
            if (player.isFlying()) {
                player.setFlySpeed(scaled);
                player.sendMessage(prefix + ChatColor.GREEN + "Fly speed set to " + speed);
            } else {
                player.setWalkSpeed(scaled);
                player.sendMessage(prefix + ChatColor.GREEN + "Walk speed set to " + speed);
            }

        } catch (NumberFormatException e) {
            player.sendMessage(prefix + ChatColor.RED + "Invalid number! Use 1-10.");
        }

        return true;
    }
}
