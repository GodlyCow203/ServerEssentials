package serveressentials.serveressentials.server;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.util.ServerMessages;

public class WorldListCommand implements CommandExecutor {

    private final ServerMessages messages;

    public WorldListCommand(ServerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.worldlist")) {
            sender.sendMessage(messages.get("worldlist.no-permission"));
            return true;
        }

        sender.sendMessage(messages.get("worldlist.header"));

        for (World world : Bukkit.getWorlds()) {
            String name = world.getName();
            boolean loaded = Bukkit.getWorld(name) != null;

            int loadedChunks = loaded ? world.getLoadedChunks().length : 0;
            int entityCount = loaded ? world.getEntities().size() : 0;
            int playerCount = loaded ? world.getPlayers().size() : 0;

            String statusPath = loaded ? "worldlist.status-loaded" : "worldlist.status-unloaded";
            String status = messages.getConfig().getString(statusPath, loaded ? "<green>Loaded" : "<red>Unloaded");

            Component entry = messages.get("worldlist.entry",
                    "<world>", name,
                    "<status>", status,
                    "<chunks>", String.valueOf(loadedChunks),
                    "<entities>", String.valueOf(entityCount),
                    "<players>", String.valueOf(playerCount));

            sender.sendMessage(entry);
        }

        return true;
    }
}
