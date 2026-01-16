package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.TopConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class TopCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.top";

    private final PlayerLanguageManager langManager;
    private final TopConfig config;
    private final CommandDataStorage dataStorage;

    public TopCommand(PlayerLanguageManager langManager, TopConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.top.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.top.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Location loc = player.getLocation();
        boolean teleported = false;

        for (int y = loc.getWorld().getMaxHeight() - 1; y > loc.getBlockY(); y--) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR) {
                player.teleport(check.add(0, 1, 0));
                player.sendMessage(langManager.getMessageFor(player, "commands.top.teleported",
                        "<green>Teleported to the highest block above you!"));
                teleported = true;
                break;
            }
        }

        if (!teleported) {
            player.sendMessage(langManager.getMessageFor(player, "commands.top.no-block",
                    "<red>No solid block found above you!"));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "top", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "top", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "top", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}