package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.TreeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.TreeType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class TreeCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.tree";

    private final List<String> treeTypes = Arrays.asList(
            "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK",
            "CHERRY", "MANGROVE"
    );

    private final PlayerLanguageManager langManager;
    private final TreeConfig config;
    private final CommandDataStorage dataStorage;

    public TreeCommand(PlayerLanguageManager langManager, TreeConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.tree.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tree.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.tree.specify-type",
                    "<red>Please specify a tree type." ));
            return true;
        }

        try {
            TreeType type = TreeType.valueOf(args[0].toUpperCase());

            boolean success = player.getWorld().generateTree(player.getLocation(), type);

            if (success) {
                player.sendMessage(langManager.getMessageFor(player, "commands.tree.tree-planted",
                        "<green>Tree planted: <yellow>{type}",
                        ComponentPlaceholder.of("{type}", type.name())));

                trackUsage(player.getUniqueId(), type.name(), true);
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.tree.cannot-grow",
                        "<red>Tree cannot grow here! Make sure there's enough space and suitable ground." ));

                trackUsage(player.getUniqueId(), type.name(), false);
            }

        } catch (IllegalArgumentException e) {
            String validTypes = String.join(", ", treeTypes);
            player.sendMessage(langManager.getMessageFor(player, "commands.tree.invalid-type",
                    "<red>Invalid tree type! Valid types: <yellow>{types}",
                    ComponentPlaceholder.of("{types}", validTypes)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (!player.hasPermission(PERMISSION)) return List.of();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return treeTypes.stream()
                    .filter(t -> t.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String treeType, boolean success) {
        dataStorage.getState(playerId, "tree", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "tree", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "tree", "last_tree_type", treeType);
            dataStorage.setState(playerId, "tree", "last_success", String.valueOf(success));
        });
    }
}