package net.lunark.io.server;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import net.lunark.io.util.ServerMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UnloadWorldCommand implements CommandExecutor, TabCompleter {

    private final ServerMessages messages;

    public UnloadWorldCommand(ServerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(messages.get("unloadworld.usage"));
            return true;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Component msg = messages.get("unloadworld.not-found", "<world>", worldName);
            sender.sendMessage(msg);
            return true;
        }

        if (!Bukkit.unloadWorld(world, true)) {
            Component msg = messages.get("unloadworld.failed", "<world>", worldName);
            sender.sendMessage(msg);
            return true;
        }

        Component msg = messages.get("unloadworld.success", "<world>", worldName);
        sender.sendMessage(msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worldNames.add(world.getName());
            }

            String input = args[0].toLowerCase();
            worldNames.removeIf(name -> !name.toLowerCase().startsWith(input));
            return worldNames;
        }
        return Collections.emptyList();
    }
}
