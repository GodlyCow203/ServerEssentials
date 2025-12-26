package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.BottomConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BottomCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.bottom";

    private final PlayerLanguageManager langManager;
    private final BottomConfig config;
    private final CommandDataStorage dataStorage;

    public BottomCommand(PlayerLanguageManager langManager, BottomConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.bottom.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.bottom.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Location loc = player.getLocation();
        boolean found = false;

        for (int y = 0; y < loc.getBlockY(); y++) {
            Location check = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (check.getBlock().getType() != Material.AIR &&
                    check.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                player.teleport(check);
                player.sendMessage(langManager.getMessageFor(player, "commands.bottom.success",
                        "<green>Teleported to bottom! <gray>(Y: {y})",
                        ComponentPlaceholder.of("{y}", y)));
                found = true;
                break;
            }
        }

        if (!found) {
            player.sendMessage(langManager.getMessageFor(player, "commands.bottom.no-safe-ground",
                    "<red>No safe ground below!"));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "bottom", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "bottom", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "bottom", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}