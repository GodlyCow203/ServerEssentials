package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            if (!BackManager.hasBack(player.getUniqueId())) {
                player.sendMessage(getPrefix() + ChatColor.RED + "No previous location saved.");
                return true;
            }

            Location backLocation = BackManager.getLastLocation(player.getUniqueId());
            if (backLocation == null) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Your last location is invalid.");
                return true;
            }

            player.teleport(backLocation);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported back to your previous location.");
            BackManager.clearBack(player.getUniqueId());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "lobby" -> {
                Location lobbyLocation = getLobbyLocation();
                if (lobbyLocation == null) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Lobby location not set.");
                    return true;
                }
                player.teleport(lobbyLocation);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to the lobby.");
            }
            case "death" -> {
                Location deathLocation = player.getLastDeathLocation();
                if (deathLocation == null) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "No death location found.");
                    return true;
                }
                player.teleport(deathLocation);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to your last death location.");
            }
            case "back" -> {
                if (!BackManager.hasBack(player.getUniqueId())) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "No previous location saved.");
                    return true;
                }

                Location backLocation = BackManager.getLastLocation(player.getUniqueId());
                if (backLocation == null) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Your last location is invalid.");
                    return true;
                }

                player.teleport(backLocation);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported back to your previous location.");
                BackManager.clearBack(player.getUniqueId());
            }
            default -> {
                player.sendMessage(getPrefix() + ChatColor.RED + "Unknown command. Use: /back, /back lobby, /back death.");
            }
        }

        return true;
    }

    private Location getLobbyLocation() {
        return new Location(Bukkit.getWorld("world"), 0, 100, 0); // Example lobby location
    }
}
