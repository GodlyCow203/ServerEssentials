package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final HashMap<UUID, UUID> lastMessageMap;

    public ReplyCommand(HashMap<UUID, UUID> lastMessageMap) {
        this.lastMessageMap = lastMessageMap;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String prefix = getPrefix();

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /reply <message>");
            return true;
        }

        if (!lastMessageMap.containsKey(player.getUniqueId())) {
            player.sendMessage(prefix + ChatColor.RED + "No one to reply to.");
            return true;
        }

        UUID targetUUID = lastMessageMap.get(player.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            player.sendMessage(prefix + ChatColor.RED + "The player you are trying to reply to is not online.");
            return true;
        }

        String message = String.join(" ", args);

        player.sendMessage(ChatColor.GRAY + "To " + target.getName() + ": " + ChatColor.WHITE + message);
        target.sendMessage(ChatColor.GRAY + "From " + player.getName() + ": " + ChatColor.WHITE + message);

        // Update reply map so target can reply back
        lastMessageMap.put(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
