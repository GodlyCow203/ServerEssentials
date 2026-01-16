package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.LoadWorldConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class LoadWorldCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.loadworld";

    private final PlayerLanguageManager langManager;
    private final LoadWorldConfig config;

    public LoadWorldCommand(PlayerLanguageManager langManager, LoadWorldConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.loadworld.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.loadworld.usage",
                    "<red>Usage: /loadworld <world>"));
            return true;
        }

        String worldName = args[0];
        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.loadworld.already-loaded",
                    "<red>World <yellow>{world}</yellow> is already loaded!",
                    ComponentPlaceholder.of("{world}", worldName)));
            return true;
        }

        boolean loaded = Bukkit.getServer().createWorld(new WorldCreator(worldName)) != null;

        if (loaded) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.loadworld.success",
                    "<green>Successfully loaded world <yellow>{world}</yellow>.",
                    ComponentPlaceholder.of("{world}", worldName)));
        } else {
            sender.sendMessage(langManager.getMessageFor(player, "commands.loadworld.failed",
                    "<red>Failed to load world <yellow>{world}</yellow>!",
                    ComponentPlaceholder.of("{world}", worldName)));
        }

        return true;
    }
}