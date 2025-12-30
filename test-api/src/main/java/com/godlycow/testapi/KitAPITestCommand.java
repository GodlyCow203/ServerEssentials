package com.godlycow.testapi;

import com.serveressentials.api.kit.KitAPI;
import com.serveressentials.api.kit.KitInfo;
import com.serveressentials.api.kit.event.KitClaimEvent;
import com.serveressentials.api.kit.event.KitOpenGUIEvent;
import com.serveressentials.api.kit.event.KitOpenPreviewEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class KitAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private KitAPI api;

    public KitAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull KitAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cKitAPI is not yet available. Please try again in a moment.");
            return true;
        }

        if (args.length == 0) {
            api.openKitGUI(player).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cFailed to open kit GUI!");
                }
            });
            return true;
        }

        if (args.length == 1) {
            String kitId = args[0];
            api.openKitPreview(player, kitId).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cKit '" + kitId + "' not found or failed to open preview!");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("claim")) {
            String kitId = args[1];
            api.claimKit(player, kitId).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aSuccessfully claimed kit '" + kitId + "'!");
                } else {
                    player.sendMessage("§cFailed to claim kit '" + kitId + "'! Check permissions or cooldown.");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            api.getAvailableKits(player).thenAccept(kits -> {
                player.sendMessage("§6Available Kits:");
                for (KitInfo kit : kits) {
                    player.sendMessage("§7- " + kit.getName() + " (ID: " + kit.getId() +
                            ", Cooldown: " + kit.getCooldown() + "s)");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
            String kitId = args[1];
            api.getRemainingCooldown(player, kitId).thenAccept(remaining -> {
                if (remaining > 0) {
                    player.sendMessage("§eRemaining cooldown for '" + kitId + "': " + remaining + " seconds");
                } else {
                    player.sendMessage("§aNo cooldown for kit '" + kitId + "'");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aKit configuration reloaded!");
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6KitAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            player.sendMessage("§7API Available: " + (api != null));
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onKitClaim(@NotNull KitClaimEvent event) {
        plugin.getLogger().info("[KitAPITest] KitClaimEvent: " + event.getPlayer().getName() +
                " claimed kit '" + event.getKitName() + "' (ID: " + event.getKitId() + ")");
    }

    @EventHandler
    public void onKitGUIOpen(@NotNull KitOpenGUIEvent event) {
        plugin.getLogger().info("[KitAPITest] KitOpenGUIEvent: " + event.getPlayer().getName() +
                " opened kit GUI with " + event.getKitCount() + " kits");
    }

    @EventHandler
    public void onKitPreviewOpen(@NotNull KitOpenPreviewEvent event) {
        plugin.getLogger().info("[KitAPITest] KitOpenPreviewEvent: " + event.getPlayer().getName() +
                " previewing kit '" + event.getKitName() + "' (ID: " + event.getKitId() + ")");
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6KitAPI Test Command Usage:");
        player.sendMessage("§7/kittest - Open kit GUI");
        player.sendMessage("§7/kittest <kitId> - Preview a kit");
        player.sendMessage("§7/kittest claim <kitId> - Claim a kit");
        player.sendMessage("§7/kittest list - List all kits");
        player.sendMessage("§7/kittest cooldown <kitId> - Check cooldown");
        player.sendMessage("§7/kittest reload - Reload kit configuration");
        player.sendMessage("§7/kittest status - Show API status");
    }
}