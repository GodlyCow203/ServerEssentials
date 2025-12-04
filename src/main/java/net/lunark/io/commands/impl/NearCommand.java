package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.NearConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class NearCommand implements CommandExecutor {
    private static final String PERMISSION_NODE = "serveressentials.command.near";
    private final PlayerLanguageManager langManager;
    private final NearConfig config;

    public NearCommand(PlayerLanguageManager langManager, NearConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.near.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.near.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        Location loc = player.getLocation();
        List<Component> nearbyPlayers = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;

            double distance = p.getLocation().distance(loc);
            if (distance <= config.maxDistance) {
                nearbyPlayers.add(langManager.getMessageFor(player, "commands.near.entry",
                        "<yellow>- {name} <gray>({distance} blocks)",
                        ComponentPlaceholder.of("{name}", p.getName()),
                        ComponentPlaceholder.of("{distance}", String.valueOf((int) distance))
                ));
            }
        }

        if (nearbyPlayers.isEmpty()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.near.none",
                    "<red>No players are nearby."));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.near.header",
                    "<green>Players near you:"));
            nearbyPlayers.forEach(player::sendMessage);
        }

        return true;
    }
}