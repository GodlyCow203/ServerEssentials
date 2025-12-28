package com.godlycow.testapi;

import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.afk.event.AFKStatusEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.UUID;

public class AFKAPITestCommand implements CommandExecutor, Listener {
    private final TestAPI plugin;
    private static final String PREFIX = ChatColor.GOLD + "[AFKTester] " + ChatColor.RESET;

    public AFKAPITestCommand(TestAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command!");
            return true;
        }

        AFKAPI afkAPI = plugin.getAFKAPI();
        if (afkAPI == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "AFK API not available!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                testStatus(player, afkAPI);
                break;
            case "list":
                testListAFK(player, afkAPI);
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage(PREFIX + ChatColor.RED + "Usage: /afktest set <true|false>");
                    return true;
                }
                testSetAFK(player, afkAPI, args[1]);
                break;
            case "toggle":
                testToggleAFK(player, afkAPI);
                break;
            case "update":
                afkAPI.updatePlayerActivity(player);
                player.sendMessage(PREFIX + ChatColor.GREEN + "✅ Activity updated!");
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void testStatus(Player player, AFKAPI api) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== AFK API Status ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "AFK Enabled: " +
                (api.isAFKEnabled() ? ChatColor.GREEN + "✅ YES" : ChatColor.RED + "❌ NO"));
        player.sendMessage(PREFIX + ChatColor.AQUA + "Timeout: " + ChatColor.YELLOW + api.getAFKTimeoutSeconds() + "s");
        player.sendMessage(PREFIX + ChatColor.AQUA + "You are AFK: " +
                (api.isAFK(player) ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));
        player.sendMessage(PREFIX + ChatColor.GREEN + "✅ AFK API is working!");
    }

    private void testListAFK(Player player, AFKAPI api) {
        player.sendMessage(PREFIX + ChatColor.GREEN + "=== AFK Players ===");
        Collection<UUID> afkPlayers = api.getAFKPlayers();
        if (afkPlayers.isEmpty()) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "No players are AFK!");
            return;
        }
        afkPlayers.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                player.sendMessage(PREFIX + ChatColor.AQUA + "• " + p.getName());
            }
        });
    }

    private void testSetAFK(Player player, AFKAPI api, String state) {
        boolean afk = Boolean.parseBoolean(state);
        api.setAFK(player, afk).thenAccept(success -> {
            player.sendMessage(success ?
                    PREFIX + ChatColor.GREEN + "✅ AFK status set to " + afk :
                    PREFIX + ChatColor.RED + "❌ Failed to set AFK status!"
            );
        });
    }

    private void testToggleAFK(Player player, AFKAPI api) {
        api.toggleAFK(player).thenAccept(success -> {
            player.sendMessage(success ?
                    PREFIX + ChatColor.GREEN + "✅ AFK status toggled!" :
                    PREFIX + ChatColor.RED + "❌ Failed to toggle AFK status!"
            );
        });
    }

    private void sendHelp(Player player) {
        player.sendMessage(PREFIX + ChatColor.GOLD + "=== AFK API Tester ===");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/afktest status" + ChatColor.GRAY + " - Check API status");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/afktest list" + ChatColor.GRAY + " - List AFK players");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/afktest set <true|false>" + ChatColor.GRAY + " - Set AFK status");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/afktest toggle" + ChatColor.GRAY + " - Toggle AFK status");
        player.sendMessage(PREFIX + ChatColor.AQUA + "/afktest update" + ChatColor.GRAY + " - Update activity");
    }

    @EventHandler
    public void onAFKStatusChange(AFKStatusEvent event) {
        plugin.getLogger().info(PREFIX + "AFK Status Change - Player: " + event.getPlayer().getName() +
                ", AFK: " + event.isAFK());
    }
}