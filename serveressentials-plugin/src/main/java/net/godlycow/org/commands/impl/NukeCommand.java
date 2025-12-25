package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.NukeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class NukeCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.nuke";

    private final PlayerLanguageManager langManager;
    private final NukeConfig config;
    private final CommandDataStorage dataStorage;

    public NukeCommand(PlayerLanguageManager langManager, NukeConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.nuke.only-player",
                    "<red>Only players can use this command!").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.nuke.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        World world = player.getWorld();
        Location loc = player.getLocation();

        // Create explosion with config values
        world.createExplosion(loc, config.explosionPower(), config.setFire(), config.breakBlocks());

        player.sendMessage(langManager.getMessageFor(player, "commands.nuke.deployed",
                "<green>Nuke deployed! <gray>(Power: {power})",
                ComponentPlaceholder.of("{power}", config.explosionPower())));

        // Store usage statistics (async)
        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "nuke", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "nuke", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "nuke", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}