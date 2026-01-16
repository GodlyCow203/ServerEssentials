package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.RealNameConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class RealNameCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.realname";

    private final PlayerLanguageManager langManager;
    private final RealNameConfig config;
    private final CommandDataStorage dataStorage;

    public RealNameCommand(PlayerLanguageManager langManager, RealNameConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.realname.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.realname.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.realname.usage",
                    "<red>Usage: <white>/realname <nickname>"));
            return true;
        }

        String nickname = args[0];

        Player found = null;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getDisplayName().equalsIgnoreCase(nickname)) {
                found = online;
                break;
            }
        }

        if (found != null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.realname.found",
                    "<green>Nickname <yellow>{nickname} <green>belongs to <white>{realname}",
                    ComponentPlaceholder.of("{nickname}", nickname),
                    ComponentPlaceholder.of("{realname}", found.getName())));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.realname.not-found",
                    "<red>No player found with nickname <yellow>{nickname}",
                    ComponentPlaceholder.of("{nickname}", nickname)));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "realname", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "realname", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "realname", "last_search", nickname);
        });

        return true;
    }
}