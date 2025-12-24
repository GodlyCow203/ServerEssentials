package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmuteCommand extends CommandModule implements CommandExecutor {
    private final PlayerLanguageManager langManager;
    private final MuteCommand muteCommand;

    public UnmuteCommand(PlayerLanguageManager langManager,
                         CommandDataStorage commandStorage, MuteCommand muteCommand) {
        super(commandStorage, langManager);
        this.langManager = langManager;
        this.muteCommand = muteCommand;
    }

    @Override
    protected String getCommandName() {
        return "unmute";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.command.unmute")) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.unmute")));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "unmute.usage",
                    "<red>Usage: <yellow>/unmute <player>"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getName() == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                    "unmute.player-not-found",
                    "<red>Player <#00ffff>{player}</#00ffff> not found.",
                    LanguageManager.ComponentPlaceholder.of("{player}", targetName)));
            return true;
        }

        muteCommand.isMuted(target.getUniqueId()).thenAccept(isMuted -> {
            if (!isMuted) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "unmute.not-muted",
                        "<red><yellow>{player}</yellow> is not currently muted.",
                        LanguageManager.ComponentPlaceholder.of("{player}", targetName)));
                return;
            }

            muteCommand.unmute(target.getUniqueId()).thenRun(() -> {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null,
                        "unmute.unmuted",
                        "<green>Successfully unmuted <#00ffff>{player}</#00ffff>",
                        LanguageManager.ComponentPlaceholder.of("{player}", targetName)));

                if (target.isOnline()) {
                    Player player = (Player) target.getPlayer();
                    player.sendMessage(langManager.getMessageFor(player,
                            "unmute.notify",
                            "<green>You have been unmuted!"));
                }
            });
        });

        return true;
    }
}