package net.lunark.io.scoreboard;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private final CustomScoreboardManager manager;

    public ScoreboardCommand(CustomScoreboardManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        if (args.length == 0) {
            manager.getMessages().send(player, "commands.help");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload":
                if (!sender.hasPermission("scoreboard.reload")) {
                    manager.getMessages().send(player, "commands.no-permission");
                    return true;
                }
                manager.reload();
                manager.getMessages().send(player, "commands.reload.success");

                for (Player p : player.getServer().getOnlinePlayers()) {
                    String layout = manager.getConfigHandler().getLayoutForPlayer(p, manager.getStorage());
                    if (manager.getStorage().isEnabled(p)) {
                        manager.getUpdater().update(p, layout);
                    } else {
                        manager.getUpdater().clear(p);
                    }
                }
                break;

            case "toggle":
                boolean state = manager.getStorage().togglePlayer(player);

                if (state) {
                    manager.getMessages().send(player, "commands.toggle.enabled");
                    manager.getUpdater().update(player);
                } else {
                    manager.getMessages().send(player, "commands.toggle.disabled");
                    manager.getUpdater().clear(player);
                }
                break;



            case "color":
                if (args.length < 2) {
                    manager.getMessages().send(player, "commands.color.usage");
                    return true;
                }

                String newLayout = args[1].toLowerCase();
                if (!manager.getConfigHandler().layoutExists(newLayout)) {
                    manager.getMessages().send(player, "commands.color.not-found");
                    return true;
                }

                manager.getStorage().setPlayerLayout(player, newLayout);
                manager.getUpdater().update(player, newLayout);
                manager.getMessages().send(player, "commands.color.changed", "<layout>", newLayout);
                break;

            default:
                manager.getMessages().send(player, "commands.help");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("reload");
            options.add("toggle");
            options.add("color");

            String arg = args[0].toLowerCase();
            options.removeIf(s -> !s.startsWith(arg));
            return options;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            List<String> layouts = manager.getConfigHandler().getLayouts();
            String arg = args[1].toLowerCase();
            layouts.removeIf(s -> !s.toLowerCase().startsWith(arg));
            return layouts;
        }

        return Collections.emptyList();
    }
}
