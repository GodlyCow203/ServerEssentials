package serveressentials.serveressentials.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.WarpMessages;
import serveressentials.serveressentials.warp.WarpManager;

public class WarpsCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final WarpManager warpManager;
    private final WarpMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public WarpsCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.warpManager = plugin.getWarpManager();
        this.messages = plugin.getWarpMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        // Permission check
        if (!player.hasPermission("serveressentials.warps")) {
            player.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (warpManager.getWarps().isEmpty()) {
            player.sendMessage(messages.get("warp-list-empty"));
            return true;
        }

        // Send configurable header
        player.sendMessage(messages.get("warp-list-header"));

        // Send each warp using the configurable entry format
        warpManager.getWarps().keySet().forEach(warp -> {
            Component line = messages.get("warp-list-entry", "<warp>", warp);
            player.sendMessage(line);
        });

        return true;
    }

}
