package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RemoveLobbyCommand implements CommandExecutor {

    // Use dynamic prefix from config
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!LobbyManager.hasLobby()) {
            player.sendMessage(getPrefix() + ChatColor.RED + "No lobby was set.");
            return true;
        }

        LobbyManager.removeLobby();
        player.sendMessage(getPrefix() + ChatColor.YELLOW + "Lobby location removed.");

        return true;
    }
}
