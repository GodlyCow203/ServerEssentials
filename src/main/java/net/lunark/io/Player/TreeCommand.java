package net.lunark.io.Player;

import org.bukkit.TreeType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.util.PlayerMessages;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TreeCommand implements CommandExecutor, TabCompleter {

    private final List<String> treeTypes = Arrays.asList(
            "OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK"
    );

    private final PlayerMessages messages;

    public TreeCommand(PlayerMessages messages) {
        this.messages = messages;

        messages.addDefault("tree.only-players", "<red>Only players can use this command!");
        messages.addDefault("tree.specify-type", "<red>Please specify a tree type.");
        messages.addDefault("tree.invalid-type", "<red>Invalid tree type! Valid types: {types}");
        messages.addDefault("tree.tree-planted", "<green>Tree planted: {type}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("tree.only-players"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(messages.get("tree.specify-type"));
            return true;
        }

        try {
            TreeType type = TreeType.valueOf(args[0].toUpperCase());
            player.getWorld().generateTree(player.getLocation(), type);

            player.sendMessage(messages.get(
                    "tree.tree-planted",
                    "{type}", type.name()
            ));
        } catch (IllegalArgumentException e) {
            player.sendMessage(messages.get(
                    "tree.invalid-type",
                    "{types}", String.join(", ", treeTypes)
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return treeTypes.stream()
                    .filter(t -> t.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
