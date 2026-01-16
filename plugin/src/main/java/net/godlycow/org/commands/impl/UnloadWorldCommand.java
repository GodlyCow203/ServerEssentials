package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.UnloadWorldConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class UnloadWorldCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "essc.command.unloadworld";

    private final PlayerLanguageManager langManager;
    private final UnloadWorldConfig config;

    public UnloadWorldCommand(PlayerLanguageManager langManager, UnloadWorldConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.unloadworld.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.unloadworld.usage",
                    "<red>Usage: /unloadworld <world>"));
            return true;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.unloadworld.not-found",
                    "<red>World <yellow>{world}</yellow> not found!",
                    ComponentPlaceholder.of("{world}", worldName)));
            return true;
        }

        if (!Bukkit.unloadWorld(world, true)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.unloadworld.failed",
                    "<red>Failed to unload world <yellow>{world}</yellow>!",
                    ComponentPlaceholder.of("{world}", worldName)));
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(player, "commands.unloadworld.success",
                "<green>Successfully unloaded world <yellow>{world}</yellow>.",
                ComponentPlaceholder.of("{world}", worldName)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(PERMISSION)) {
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