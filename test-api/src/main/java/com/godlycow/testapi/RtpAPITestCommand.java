package com.godlycow.testapi;

import com.serveressentials.api.rtp.RtpAPI;
import com.serveressentials.api.rtp.RtpLocation;
import com.serveressentials.api.rtp.RtpWorldConfig;
import com.serveressentials.api.rtp.event.RtpTeleportEvent;
import com.serveressentials.api.rtp.event.RtpLocationSaveEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public final class RtpAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private RtpAPI api;

    public RtpAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull RtpAPI api) {
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
            player.sendMessage("§cRTPAPI is not yet available. Please try again in a moment.");
            return true;
        }

        if (args.length == 0) {
            api.openRtpGUI(player).thenAccept(success -> {
                if (!success) {
                    player.sendMessage("§cFailed to open RTP GUI!");
                }
            });
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("teleport")) {
            if (args.length == 2) {
                String worldName = args[1];
                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    player.sendMessage("§cWorld '" + worldName + "' not found!");
                    return true;
                }
                api.randomTeleport(player, world).thenAccept(success -> {
                    if (!success) {
                        player.sendMessage("§cRTP failed! Check permissions, cooldown, or world config.");
                    }
                });
            } else {
                api.randomTeleport(player).thenAccept(success -> {
                    if (!success) {
                        player.sendMessage("§cRTP failed! Check permissions, cooldown, or world config.");
                    }
                });
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("last")) {
            api.getLastRtpLocation(player.getUniqueId()).thenAccept(opt -> {
                if (opt.isPresent()) {
                    RtpLocation loc = opt.get();
                    player.sendMessage("§6Your last RTP location:");
                    player.sendMessage("§7World: " + loc.getWorldName());
                    player.sendMessage("§7Coords: " + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
                    player.sendMessage("§7Time: " + new java.util.Date(loc.getTimestamp()));
                } else {
                    player.sendMessage("§eNo previous RTP location found.");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            String worldName = args[1];
            api.getWorldConfig(worldName).thenAccept(opt -> {
                if (opt.isPresent()) {
                    RtpWorldConfig config = opt.get();
                    player.sendMessage("§6RTP Config for '" + worldName + "':");
                    player.sendMessage("§7Enabled: " + config.isEnabled());
                    player.sendMessage("§7Min Radius: " + config.getMinRadius());
                    player.sendMessage("§7Max Radius: " + config.getMaxRadius());
                    player.sendMessage("§7Cooldown: " + config.getCooldownSeconds() + "s");
                } else {
                    player.sendMessage("§cNo RTP config found for world '" + worldName + "'");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("enabled")) {
            String worldName = args[1];
            api.isRtpEnabled(worldName).thenAccept(enabled -> {
                player.sendMessage("§eRTP " + (enabled ? "§aenabled" : "§cdisabled") + " §efor world '" + worldName + "'");
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cooldown")) {
            api.getRemainingCooldown(player.getUniqueId()).thenAccept(remaining -> {
                if (remaining > 0) {
                    player.sendMessage("§eRemaining cooldown: " + remaining + " seconds");
                } else {
                    player.sendMessage("§aNo cooldown active");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6RTPAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            api.getLastRtpLocation(player.getUniqueId()).thenAccept(opt -> {
                player.sendMessage("§7Last location: " + (opt.isPresent() ? "§aAvailable" : "§cNone"));
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aRTP configuration reloaded!");
            });
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onRtpTeleport(@NotNull RtpTeleportEvent event) {
        plugin.getLogger().info("[RtpAPITest] " + event.getPlayer().getName() +
                " teleported via RTP to " + event.getWorldName() +
                " at " + event.getRtpLocation());
    }

    @EventHandler
    public void onRtpLocationSave(@NotNull RtpLocationSaveEvent event) {
        plugin.getLogger().info("[RtpAPITest] RTP location saved for " + event.getPlayer().getName() +
                ": " + event.getRtpLocation());
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6RTPAPI Test Command Usage:");
        player.sendMessage("§7/rtapitest - Open RTP GUI");
        player.sendMessage("§7/rtapitest teleport [world] - Perform RTP");
        player.sendMessage("§7/rtapitest last - Get last RTP location");
        player.sendMessage("§7/rtapitest config <world> - View world config");
        player.sendMessage("§7/rtapitest enabled <world> - Check if enabled");
        player.sendMessage("§7/rtapitest cooldown - Check cooldown");
        player.sendMessage("§7/rtapitest status - Show API status");
        player.sendMessage("§7/rtapitest reload - Reload configuration");
    }
}