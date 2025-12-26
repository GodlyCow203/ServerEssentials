package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.PlayerInfoConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class PlayerInfoCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.playerinfo";

    private final PlayerLanguageManager langManager;
    private final PlayerInfoConfig config;

    public PlayerInfoCommand(PlayerLanguageManager langManager, PlayerInfoConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.usage",
                    "<red>Usage: /playerinfo <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.not-found",
                    "<red>Player not found."));
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.header",
                "<gold>----- Player Info -----"));

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.name",
                "<yellow>Name: <white>{player}",
                ComponentPlaceholder.of("{player}", target.getName())));

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.uuid",
                "<yellow>UUID: <white>{uuid}",
                ComponentPlaceholder.of("{uuid}", target.getUniqueId().toString())));

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.health",
                "<yellow>Health: <white>{health}/{maxhealth}",
                ComponentPlaceholder.of("{health}", String.valueOf(target.getHealth())),
                ComponentPlaceholder.of("{maxhealth}", String.valueOf(target.getMaxHealth()))));

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.food",
                "<yellow>Food: <white>{food}",
                ComponentPlaceholder.of("{food}", String.valueOf(target.getFoodLevel()))));

        sender.sendMessage(langManager.getMessageFor(player, "commands.playerinfo.location",
                "<yellow>Location: <white>{location}",
                ComponentPlaceholder.of("{location}", target.getLocation().toVector().toString())));

        return true;
    }
}