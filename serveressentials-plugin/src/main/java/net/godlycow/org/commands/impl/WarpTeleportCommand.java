package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpTeleportCommand extends CommandModule implements CommandExecutor {
    private final WarpManager warpManager;
    private final JavaPlugin plugin;

    public WarpTeleportCommand(JavaPlugin plugin, WarpManager warpManager,
                               PlayerLanguageManager langManager, CommandDataStorage commandStorage) {
        super(commandStorage, langManager);
        this.plugin = plugin;
        this.warpManager = warpManager;
    }

    @Override
    protected String getCommandName() {
        return "warp";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-players",
                    "<red>This command can only be used by players!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "warp.usage",
                    "<red>Usage: <yellow>/warp <name>"));
            return true;
        }

        if (!player.hasPermission("serveressentials.command.warp")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.warp")));
            return true;
        }

        String warpName = args[0].toLowerCase();
        plugin.getLogger().info("Player " + player.getName() + " attempting to warp to '" + warpName + "'");
        checkCooldownAndTeleport(player, warpName);
        return true;
    }

    private void checkCooldownAndTeleport(Player player, String warpName) {
        if (player.hasPermission("serveressentials.command.warp.bypass-cooldown")) {
            teleport(player, warpName);
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = Instant.now().getEpochSecond();
        String cooldownKey = "last-warp";

        this.storage.getState(uuid, getCommandName(), cooldownKey)
                .thenApply(opt -> opt.map(Long::parseLong).orElse(0L))
                .thenCompose(lastUsed -> {
                    long elapsed = now - lastUsed;
                    if (elapsed < warpManager.getCooldown().getSeconds()) {
                        long remaining = warpManager.getCooldown().getSeconds() - elapsed;
                        player.sendMessage(langManager.getMessageFor(player, "warp.cooldown-active",
                                "<red>Please wait <yellow>{time}</yellow> seconds before warping again.",
                                LanguageManager.ComponentPlaceholder.of("{time}", String.valueOf(remaining))));
                        return CompletableFuture.completedFuture(null);
                    }
                    return this.storage.setState(uuid, getCommandName(), cooldownKey, String.valueOf(now))
                            .thenCompose(v -> teleport(player, warpName));
                });
    }

    private CompletableFuture<Void> teleport(Player player, String warpName) {
        plugin.getLogger().info("Fetching warp '" + warpName + "' from database...");

        return warpManager.getWarp(warpName).thenAccept(optionalLocation -> {
            plugin.getLogger().info("Database returned: " + optionalLocation);

            if (optionalLocation.isPresent()) {
                Optional<Location> loc = optionalLocation.get();
                if (loc.isPresent()) {
                    Location location = loc.get();
                    plugin.getLogger().info("Warp location found: " + location + " in world " + location.getWorld());

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        try {
                            if (location.getWorld() == null) {
                                player.sendMessage("§cWarp world is not loaded!");
                                plugin.getLogger().warning("Warp '" + warpName + "' has null world!");
                                return;
                            }

                            boolean success = player.teleport(location);
                            plugin.getLogger().info("Teleport successful: " + success);

                            if (success) {
                                player.sendMessage(langManager.getMessageFor(player, "warp.success",
                                        "<green>Teleported to warp <yellow>{warp}</yellow>!",
                                        LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
                            } else {
                                player.sendMessage("§cTeleport failed! Location might be unsafe.");
                            }
                        } catch (Exception e) {
                            plugin.getLogger().severe("Error teleporting player: " + e.getMessage());
                            e.printStackTrace();
                            player.sendMessage("§cError teleporting to warp!");
                        }
                    });
                } else {
                    plugin.getLogger().warning("Warp '" + warpName + "' exists but location is corrupted (null)");
                    player.sendMessage("§cWarp location is corrupted! Contact an admin.");
                }
            } else {
                plugin.getLogger().info("Warp '" + warpName + "' not found in database");
                player.sendMessage(langManager.getMessageFor(player, "warp.not-found",
                        "<red>Warp <yellow>{warp}</yellow> not found.",
                        LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to fetch warp '" + warpName + "': " + ex.getMessage());
            ex.printStackTrace();
            player.sendMessage("§cError accessing warp data!");
            return null;
        });
    }
}