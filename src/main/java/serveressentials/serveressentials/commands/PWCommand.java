package serveressentials.serveressentials.commands;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import serveressentials.serveressentials.ChatUtil;
import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.gui.CategoryGUI;
import serveressentials.serveressentials.gui.EditWarpGUI;
import serveressentials.serveressentials.pw.PlayerWarp;
import serveressentials.serveressentials.pw.WarpStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PWCommand implements CommandExecutor, TabCompleter {

    private final WarpStorage storage;
    private GUIConfig guiConfig;
    private final Plugin plugin;

    public PWCommand(Plugin plugin, WarpStorage storage, GUIConfig guiConfig) {
        this.plugin = plugin;
        this.storage = storage;
        this.guiConfig = guiConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtil.error("Only players can use this command."));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("serveressentials.pw.gui")) {
                player.sendMessage(ChatUtil.error("You do not have permission to open the warp GUI."));
                return true;
            }
            new CategoryGUI(storage, guiConfig).open(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                if (!player.hasPermission("serveressentials.pw.create")) {
                    player.sendMessage(ChatUtil.error("You do not have permission to create warps."));
                    return true;
                }
                return handleCreate(player, args);
            case "edit":
                if (!player.hasPermission("serveressentials.pw.edit")) {
                    player.sendMessage(ChatUtil.error("You do not have permission to edit warps."));
                    return true;
                }
                return handleEdit(player, args);
            case "reload":
                if (!player.hasPermission("serveressentials.pw.reload")) {
                    player.sendMessage(ChatUtil.error("You do not have permission to reload the warp config."));
                    return true;
                }
                return handleReload(sender);
            default:
                player.sendMessage(ChatUtil.error("Unknown subcommand. Usage: /pw [create|edit|reload]"));
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatUtil.error("Usage: /pw create <name>"));
            return true;
        }

        String warpName = args[1];
        if (storage.getWarps(player.getUniqueId()).stream().anyMatch(w -> w.getName().equalsIgnoreCase(warpName))) {
            player.sendMessage(ChatUtil.error("You already have a warp with that name."));
            return true;
        }

        PlayerWarp warp = new PlayerWarp(player.getUniqueId(), warpName, player.getLocation());
        storage.addWarp(warp);

        player.sendMessage(ChatUtil.success("Created warp '&f" + warpName + "&a'. Opening edit GUI..."));
        storage.setEditingWarp(player, warp);
        new EditWarpGUI(storage, guiConfig).open(player, warp);
        return true;
    }

    private boolean handleEdit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatUtil.error("Usage: /pw edit <warpName>"));
            return true;
        }

        String name = args[1];

        PlayerWarp warp = storage.getWarps(player.getUniqueId()).stream()
                .filter(w -> w.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (warp == null) {
            player.sendMessage(ChatUtil.error("You don't have a warp named '&f" + name + "&c'."));
            return true;
        }

        if (!warp.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatUtil.error("You can only edit your own warps."));
            return true;
        }

        storage.setEditingWarp(player, warp);
        new EditWarpGUI(storage, guiConfig).open(player, warp);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        guiConfig.reload(plugin.getConfig());
        sender.sendMessage(ChatUtil.success("ServerEssentials warp config reloaded."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = List.of("create", "edit", "reload");
            String partial = args[0].toLowerCase();
            for (String sub : subs) {
                completions.add(sub);
            }
        } else if (args.length == 2 && sender instanceof Player player) {
            String sub = args[0].toLowerCase();
            if (sub.equals("edit")) {
                UUID uuid = player.getUniqueId();
                List<String> warpNames = storage.getWarps(uuid).stream()
                        .map(PlayerWarp::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
                completions.addAll(warpNames);
            }
        }

        return completions;
    }

    public void setGuiConfig(GUIConfig guiConfig) {
        this.guiConfig = guiConfig;
    }
}
