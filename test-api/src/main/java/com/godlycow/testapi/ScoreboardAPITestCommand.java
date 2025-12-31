package com.godlycow.testapi;

import com.serveressentials.api.scoreboard.ScoreboardAPI;
import com.serveressentials.api.scoreboard.ScoreboardLayout;
import com.serveressentials.api.scoreboard.ScoreboardPlayerData;
import com.serveressentials.api.scoreboard.event.ScoreboardEvent;
import com.serveressentials.api.scoreboard.event.ScoreboardLayoutChangeEvent;
import com.serveressentials.api.scoreboard.event.ScoreboardReloadEvent;
import com.serveressentials.api.scoreboard.event.ScoreboardToggleEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public final class ScoreboardAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private ScoreboardAPI api;

    public ScoreboardAPITestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(ScoreboardAPI api) {
        this.api = api;
        plugin.getLogger().info("ScoreboardAPI test command received API instance: " + (api != null ? "OK" : "NULL"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cAPI not yet initialized. Please wait...");
            return true;
        }

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> handleToggle(player);
            case "layout" -> handleLayout(player, args);
            case "reload" -> handleReload(player);
            case "status" -> handleStatus(player);
            case "layouts" -> handleLayouts(player);
            default -> showUsage(player);
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage("§eScoreboard API Test Commands:");
        player.sendMessage("§7/scoreboardtest toggle §f- Toggle your scoreboard");
        player.sendMessage("§7/scoreboardtest layout <name> §f- Change layout");
        player.sendMessage("§7/scoreboardtest reload §f- Reload scoreboards");
        player.sendMessage("§7/scoreboardtest status §f- Show your scoreboard status");
        player.sendMessage("§7/scoreboardtest layouts §f- List available layouts");
    }

    private void handleToggle(Player player) {
        player.sendMessage("§7Toggling scoreboard...");
        api.toggleScoreboard(player).thenAccept(newState -> {
            player.sendMessage("§aScoreboard is now " + (newState ? "enabled" : "disabled"));
        }).exceptionally(ex -> {
            player.sendMessage("§cError toggling scoreboard: " + ex.getMessage());
            plugin.getLogger().warning("Scoreboard toggle error: " + ex.getMessage());
            return null;
        });
    }

    private void handleLayout(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /scoreboardtest layout <layout-name>");
            return;
        }

        String layout = args[1];
        player.sendMessage("§7Setting layout to: §f" + layout);
        api.setLayout(player, layout).thenAccept(v -> {
            player.sendMessage("§aLayout changed successfully!");
        }).exceptionally(ex -> {
            player.sendMessage("§cError changing layout: " + ex.getMessage());
            return null;
        });
    }

    private void handleReload(Player player) {
        player.sendMessage("§7Reloading scoreboards...");
        api.reload().thenAccept(v -> {
            player.sendMessage("§aScoreboards reloaded!");
        }).exceptionally(ex -> {
            player.sendMessage("§cError reloading: " + ex.getMessage());
            return null;
        });
    }

    private void handleStatus(Player player) {
        player.sendMessage("§eYour Scoreboard Status:");
        api.getPlayerData(player).thenAccept(data -> {
            player.sendMessage("§7Enabled: §f" + data.isEnabled());
            player.sendMessage("§7Layout: §f" + data.getLayout());
            player.sendMessage("§7Last Update: §f" + data.getLastUpdate());
        }).exceptionally(ex -> {
            player.sendMessage("§cError fetching status: " + ex.getMessage());
            return null;
        });
    }

    private void handleLayouts(Player player) {
        player.sendMessage("§eAvailable Layouts:");
        api.getLayouts().thenAccept(layouts -> {
            if (layouts.isEmpty()) {
                player.sendMessage("§cNo layouts available");
                return;
            }
            layouts.forEach(layout -> {
                player.sendMessage("§7- §f" + layout.getTitle() + " §7(" + layout.getLines().size() + " lines)");
            });
        }).exceptionally(ex -> {
            player.sendMessage("§cError fetching layouts: " + ex.getMessage());
            return null;
        });
    }

    @EventHandler
    public void onScoreboardEvent(ScoreboardEvent event) {
        plugin.getLogger().info("§e[API Test]§f ScoreboardEvent fired for: " + event.getPlayer().getName());
    }

    @EventHandler
    public void onScoreboardToggle(ScoreboardToggleEvent event) {
        plugin.getLogger().info("§e[API Test]§f Scoreboard toggled for " + event.getPlayer().getName() + " -> " + event.getNewState());
    }

    @EventHandler
    public void onScoreboardLayoutChange(ScoreboardLayoutChangeEvent event) {
        plugin.getLogger().info("§e[API Test]§f Layout changed for " + event.getPlayer().getName() + " -> " + event.getNewLayout());
    }

    @EventHandler
    public void onScoreboardReload(ScoreboardReloadEvent event) {
        plugin.getLogger().info("§e[API Test]§f Scoreboard reloaded for " + event.getPlayer().getName());
    }
}