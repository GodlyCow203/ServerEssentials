package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.AltsConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class AltsCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.alts";
    private final PlayerLanguageManager langManager;
    private final AltsConfig config;

    public AltsCommand(PlayerLanguageManager langManager, AltsConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player playerSender = (sender instanceof Player) ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.usage", "<red>Usage: /alts <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.player-offline", "<red>Player must be online to check their IP."));
            return true;
        }

        InetAddress targetIP = target.getAddress().getAddress();
        List<String> matchedPlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(target)) continue;
            if (player.getAddress().getAddress().equals(targetIP)) {
                matchedPlayers.add(player.getName());
            }
        }

        sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.header", "<yellow>Players with the same IP as {target}:", ComponentPlaceholder.of("{target}", target.getName())));

        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.none", "<gray>- None found online."));
        } else {
            for (String name : matchedPlayers) {
                sender.sendMessage(langManager.getMessageFor(playerSender, "commands.alts.found", "<gray>- {player}", ComponentPlaceholder.of("{player}", name)));
            }
        }

        return true;
    }
}