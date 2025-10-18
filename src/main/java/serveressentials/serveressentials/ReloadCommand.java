package serveressentials.serveressentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.Managers.ReloadManager;

public class ReloadCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final ReloadManager reloadManager;

    public ReloadCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.reloadManager = new ReloadManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("server.sereload") && !sender.isOp()) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§eStarting full plugin reload...");
        reloadManager.reloadEverything(sender);
        sender.sendMessage("§aReload completed!");
        return true;
    }
}
