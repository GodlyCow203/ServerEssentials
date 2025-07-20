package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!LobbyManager.hasLobby()) {
            player.sendMessage(getPrefix() + ChatColor.RED + "No lobby is set.");
            return true;
        }

        Location lobby = LobbyManager.getLobby();
        player.teleport(lobby);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to lobby.");

        return true;
    }
}
