package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.BreakConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BreakCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.break";

    private final PlayerLanguageManager langManager;
    private final BreakConfig config;
    private final CommandDataStorage dataStorage;

    public BreakCommand(PlayerLanguageManager langManager, BreakConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.break.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.break.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Block targetBlock = player.getTargetBlock(Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR),
                config.maxDistance());

        if (targetBlock == null || targetBlock.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.break.no-block",
                    "<red>You are not looking at a breakable block within <yellow>{distance}</yellow> blocks.",
                    ComponentPlaceholder.of("{distance}", config.maxDistance())));
            return true;
        }


        targetBlock.setType(Material.AIR);

        player.sendMessage(langManager.getMessageFor(player, "commands.break.success",
                "<green>Broke the block at <yellow>{x}, {y}, {z}</yellow>.",
                ComponentPlaceholder.of("{x}", targetBlock.getX()),
                ComponentPlaceholder.of("{y}", targetBlock.getY()),
                ComponentPlaceholder.of("{z}", targetBlock.getZ())));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "break", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "break", "usage_count", String.valueOf(count + 1));
        });

        return true;
    }
}