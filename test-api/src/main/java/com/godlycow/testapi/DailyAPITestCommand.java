package com.godlycow.testapi;

import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.daily.DailyCooldownInfo;
import com.serveressentials.api.daily.DailyReward;
import com.serveressentials.api.daily.event.DailyGUIOpenEvent;
import com.serveressentials.api.daily.event.DailyRewardClaimEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public final class DailyAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private DailyAPI dailyAPI;

    public DailyAPITestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setDailyAPI(DailyAPI dailyAPI) {
        this.dailyAPI = dailyAPI;
        plugin.getLogger().info("DailyAPI test command received API instance: " + (dailyAPI != null));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can test the Daily API");
            return true;
        }

        if (dailyAPI == null) {
            player.sendMessage("§cDailyAPI not available yet. Please wait for plugin to fully load.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open" -> testOpenGUI(player, args);
            case "claim" -> testClaim(player, args);
            case "claimed" -> testClaimedDays(player);
            case "last" -> testLastClaim(player);
            case "cooldown" -> testCooldown(player);
            case "reload" -> testReload(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e--- Daily API Test Commands ---");
        player.sendMessage("§7/dailyapitest open [page] §f- Open daily GUI");
        player.sendMessage("§7/dailyapitest claim <day> §f- Claim a specific day");
        player.sendMessage("§7/dailyapitest claimed §f- Check claimed days");
        player.sendMessage("§7/dailyapitest last §f- Check last claim time");
        player.sendMessage("§7/dailyapitest cooldown §f- Check cooldown status");
        player.sendMessage("§7/dailyapitest reload §f- Reload daily config");
    }

    private void testOpenGUI(Player player, String[] args) {
        int page = args.length >= 2 ? Integer.parseInt(args[1]) : 1;
        player.sendMessage("§eTesting openDailyGUI(page " + page + ")...");
        dailyAPI.openDailyGUI(player, page).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a✓ GUI opened successfully!");
            } else {
                player.sendMessage("§c✗ Failed to open GUI");
            }
        });
    }

    private void testClaim(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /dailyapitest claim <day>");
            return;
        }
        int day = Integer.parseInt(args[1]);
        player.sendMessage("§eTesting claimReward(day " + day + ")...");
        dailyAPI.claimReward(player, day).thenAccept(success -> {
            if (success) {
                player.sendMessage("§a✓ Claim successful!");
            } else {
                player.sendMessage("§c✗ Claim failed (cooldown/locked/already claimed)");
            }
        });
    }

    private void testClaimedDays(Player player) {
        player.sendMessage("§eTesting getClaimedDays()...");
        dailyAPI.getClaimedDays(player.getUniqueId()).thenAccept(days -> {
            player.sendMessage("§a✓ Claimed days: " + days);
        });
    }

    private void testLastClaim(Player player) {
        player.sendMessage("§eTesting getLastClaimTime()...");
        dailyAPI.getLastClaimTime(player.getUniqueId()).thenAccept(time -> {
            player.sendMessage("§a✓ Last claim: " + time.orElse(null));
        });
    }

    private void testCooldown(Player player) {
        player.sendMessage("§eTesting getTimeUntilNextClaim()...");
        dailyAPI.getTimeUntilNextClaim(player.getUniqueId()).thenAccept(info -> {
            player.sendMessage("§a✓ On cooldown: " + info.isOnCooldown());
            player.sendMessage("§a✓ Time remaining: " + info.format());
        });
    }

    private void testReload(Player player) {
        player.sendMessage("§eTesting reload()...");
        dailyAPI.reload().thenRun(() -> {
            player.sendMessage("§a✓ Daily config reloaded!");
        });
    }

    @EventHandler
    public void onDailyClaim(DailyRewardClaimEvent event) {
        plugin.getLogger().info(String.format(
                "[DailyTest] %s claimed day %d: %s items",
                event.getPlayer().getName(),
                event.getDay(),
                event.getReward().getItems().size()
        ));
    }

    @EventHandler
    public void onGUIOpen(DailyGUIOpenEvent event) {
        plugin.getLogger().info(String.format(
                "[DailyTest] %s opened daily GUI page %d",
                event.getPlayer().getName(),
                event.getPage()
        ));
    }
}