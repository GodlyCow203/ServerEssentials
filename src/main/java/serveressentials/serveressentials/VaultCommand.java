package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class VaultCommand implements CommandExecutor {

    private final VaultManager vaultManager;

    public VaultCommand(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /pv <1-10>");
            return true;
        }

        try {
            int vaultNumber = Integer.parseInt(args[0]);
            if (vaultNumber < 1 || vaultNumber > 10) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Invalid vault number. Use /pv <1-10>");
                return true;
            }
            vaultManager.openVault(player, vaultNumber);
        } catch (NumberFormatException e) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Invalid vault number. Use /pv <1-10>");
        }

        return true;
    }
}
