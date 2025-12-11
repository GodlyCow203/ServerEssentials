package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.HealConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class HealCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.heal";
    private static final String PERMISSION_OTHERS = "serveressentials.command.heal.others";

    private final PlayerLanguageManager langManager;
    private final HealConfig config;

    public HealCommand(PlayerLanguageManager langManager, HealConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        boolean hasHealSelf = sender.hasPermission(PERMISSION);
        boolean hasHealOthers = sender.hasPermission(PERMISSION_OTHERS);

        if (args.length == 0) {
            if (senderPlayer == null) {
                sender.sendMessage(langManager.getMessageFor(null, "commands.heal.only-player",
                        "<red>Only players can use this command!"));
                return true;
            }
            if (!hasHealSelf) {
                sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.no-permission",
                        "<red>You don't have permission to heal yourself!"));
                return true;
            }
            healPlayer(senderPlayer);
            sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.success-self",
                    "<green>You have been healed!"));
            return true;
        }

        if (args.length == 1) {
            if (!hasHealOthers) {
                sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.no-permission-others",
                        "<red>You don't have permission to heal other players!"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.player-not-found",
                        "<red>Player not found: {player}",
                        ComponentPlaceholder.of("{player}", args[0])));
                return true;
            }
            healPlayer(target);
            sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.success-other",
                    "<green>You healed <white>{player}</white>!",
                    ComponentPlaceholder.of("{player}", target.getName())));
            target.sendMessage(langManager.getMessageFor(target, "commands.heal.success-healed",
                    "<green>You have been healed by <white>{healer}</white>!",
                    ComponentPlaceholder.of("{healer}", sender.getName())));
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(senderPlayer, "commands.heal.usage",
                "<red>Usage: /heal [player]"));
        return true;
    }

    private void healPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setFireTicks(0);
        player.setRemainingAir(player.getMaximumAir());
        player.setFreezeTicks(0);
    }
}