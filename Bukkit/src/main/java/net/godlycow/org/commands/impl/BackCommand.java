package net.godlycow.org.commands.impl;

import net.godlycow.org.back.BackManager;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.BackConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin; // ✅ Added import

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BackCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.back";
    private static final String PERMISSION_LOBBY = "serveressentials.command.back.lobby";
    private static final String PERMISSION_DEATH = "serveressentials.command.back.death";

    private final PlayerLanguageManager langManager;
    private final BackConfig config;
    private final BackManager backManager;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public BackCommand(PlayerLanguageManager langManager, BackConfig config, BackManager backManager, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.backManager = backManager;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.back.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.back.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            teleportToBack(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "lobby" -> {
                if (!player.hasPermission(PERMISSION_LOBBY)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.back.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_LOBBY)));
                    return true;
                }
                teleportToLobby(player);
            }
            case "death" -> {
                if (!player.hasPermission(PERMISSION_DEATH)) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.back.no-permission-sub",
                            "<red>You need permission <yellow>{subpermission}</yellow>!",
                            ComponentPlaceholder.of("{subpermission}", PERMISSION_DEATH)));
                    return true;
                }
                teleportToDeath(player);
            }
            default -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.back.unknown-usage",
                        "<red>Unknown subcommand. Use: <white>/back, /back lobby, /back death"));
            }
        }
        return true;
    }

    private void teleportToBack(Player player) {
        UUID uuid = player.getUniqueId();

        backManager.hasBack(uuid).thenAccept(has -> {
            if (!has) {
                player.sendMessage(langManager.getMessageFor(player, "commands.back.no-back",
                        "<red>No previous location saved."));
                return;
            }

            backManager.getLastLocation(uuid).thenAccept(location -> {
                if (location == null) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.back.invalid-back",
                            "<red>Your last location is invalid."));
                    return;
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(location);
                    player.sendMessage(langManager.getMessageFor(player, "commands.back.teleported-back",
                            "<green>Teleported back to your previous location."));

                    backManager.clearBack(uuid);
                    trackUsage(uuid, "back");
                });
            });
        });
    }

    private void teleportToLobby(Player player) {
        World world = Bukkit.getWorld(config.lobbyWorld());
        if (world == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.back.lobby-not-set",
                    "<red>Lobby location not set or world is unloaded."));
            return;
        }

        Location lobby = new Location(world, config.lobbyX(), config.lobbyY(), config.lobbyZ(), config.lobbyYaw(), config.lobbyPitch());
        player.teleport(lobby);
        player.sendMessage(langManager.getMessageFor(player, "commands.back.teleported-lobby",
                "<green>Teleported to the lobby."));

        trackUsage(player.getUniqueId(), "lobby");
    }

    private void teleportToDeath(Player player) {
        Location deathLocation = player.getLastDeathLocation();
        if (deathLocation == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.back.no-death-location",
                    "<red>No death location found."));
            return;
        }

        player.teleport(deathLocation);
        player.sendMessage(langManager.getMessageFor(player, "commands.back.teleported-death",
                "<green>Teleported to your last death location."));

        trackUsage(player.getUniqueId(), "death");
    }

    private void trackUsage(UUID playerId, String type) {
        dataStorage.getState(playerId, "back", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "back", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "back", "last_type", type);
            dataStorage.setState(playerId, "back", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        if (args.length != 1) return new ArrayList<>();

        List<String> completions = new ArrayList<>();
        String partial = args[0].toLowerCase();

        if (player.hasPermission(PERMISSION_LOBBY) && "lobby".startsWith(partial)) {
            completions.add("lobby");
        }
        if (player.hasPermission(PERMISSION_DEATH) && "death".startsWith(partial)) {
            completions.add("death");
        }
        if (player.hasPermission(PERMISSION) && "back".startsWith(partial)) {
            completions.add("back");
        }

        return completions;
    }
}