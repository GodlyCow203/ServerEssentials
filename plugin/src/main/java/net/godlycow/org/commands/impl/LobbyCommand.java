package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.config.LobbyConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.commands.CommandDataStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.lobby.storage.LobbyStorage;
import net.godlycow.org.lobby.helper.AnimationHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LobbyCommand extends CommandModule implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final LobbyStorage lobbyStorage;
    private final LobbyConfig config;

    public LobbyCommand(
            JavaPlugin plugin,
            PlayerLanguageManager langManager,
            CommandDataStorage commandStorage,
            LobbyStorage lobbyStorage,
            LobbyConfig config
    ) {
        super(commandStorage, langManager);
        this.plugin = plugin;
        this.langManager = langManager;
        this.lobbyStorage = lobbyStorage;
        this.config = config;
    }

    @Override
    protected String getCommandName() {
        return "lobby";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(
                    null,
                    "commands.only-players",
                    "<red>This command can only be used by players!"
            ));
            return true;
        }

        if (args.length == 0) {
            handleTeleport(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "set" -> handleSetCommand(player, args);
            case "remove" -> handleRemoveCommand(player, args);
            case "world" -> handleWorldCommand(player, args);
            default -> player.sendMessage(langManager.getMessageFor(
                    player,
                    "commands.unknown",
                    "<red>Unknown command. Usage: /lobby, /lobby set, /lobby remove"
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player)) return suggestions;

        if (args.length == 1) {
            if (player.hasPermission("serveressentials.command.lobby"))
                suggestions.add("teleport");
            if (player.hasPermission("serveressentials.command.lobby.set"))
                suggestions.add("set");
            if (player.hasPermission("serveressentials.command.lobby.remove"))
                suggestions.add("remove");
            if (player.hasPermission("serveressentials.command.lobby.world") && config.isPerWorld())
                suggestions.add("world");
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if ((sub.equals("set") || sub.equals("remove")) && config.isPerWorld()) {
                suggestions.add("world");
            } else if (sub.equals("world") && player.hasPermission("serveressentials.command.lobby.world")) {
                Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
            }
        }

        return suggestions;
    }

    private void handleTeleport(Player player) {
        if (!player.hasPermission("serveressentials.command.lobby")) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of(
                            "{permission}",
                            "serveressentials.command.lobby"
                    )
            ));
            return;
        }

        String worldKey = config.isPerWorld()
                ? player.getWorld().getName()
                : "global";

        lobbyStorage.hasLobby(worldKey).thenAccept(has -> {
            if (!has) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(langManager.getMessageFor(
                                player,
                                "lobby.no-lobby",
                                "<red>No lobby has been set."
                        ))
                );
                return;
            }
            checkCooldownAndTeleport(player);
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to check lobby: " + ex.getMessage());
            return null;
        });
    }

    private void checkCooldownAndTeleport(Player player) {
        checkCooldown(player).thenAccept(onCooldown -> {
            if (onCooldown) return;

            String worldKey = config.isPerWorld()
                    ? player.getWorld().getName()
                    : "global";

            lobbyStorage.getLobby(worldKey).thenAccept(optLocation ->
                    optLocation.ifPresent(lobby ->
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (config.isAnimationEnabled()) {
                                    AnimationHelper.playTeleportAnimation(
                                            plugin,
                                            player,
                                            config.getAnimation()
                                    );
                                }
                                player.teleport(lobby);
                                player.sendMessage(langManager.getMessageFor(
                                        player,
                                        "lobby.teleported",
                                        "<green>Teleported to lobby!"
                                ));
                            })
                    )
            );
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to teleport player: " + ex.getMessage());
            return null;
        });
    }

    private CompletableFuture<Boolean> checkCooldown(Player player) {
        if (player.hasPermission("serveressentials.command.lobby.bypass-cooldown")) {
            return CompletableFuture.completedFuture(false);
        }

        UUID uuid = player.getUniqueId();
        long now = Instant.now().getEpochSecond();
        String cooldownKey = "last-used";

        return this.storage.getState(uuid, getCommandName(), cooldownKey)
                .thenApply(opt -> opt.map(Long::parseLong).orElse(0L))
                .thenApply(lastUsed -> {
                    long elapsed = now - lastUsed;
                    long cooldownSeconds = config.getCooldown().getSeconds();
                    if (elapsed < cooldownSeconds) {
                        long remaining = cooldownSeconds - elapsed;
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.sendMessage(langManager.getMessageFor(
                                        player,
                                        "lobby.cooldown-active",
                                        "<red>Please wait <yellow>{time}</yellow> seconds before teleporting again.",
                                        LanguageManager.ComponentPlaceholder.of(
                                                "{time}",
                                                String.valueOf(remaining)
                                        )
                                ))
                        );
                        return true;
                    }
                    return false;
                })
                .thenCompose(onCooldown -> {
                    if (!onCooldown) {
                        return this.storage.setState(
                                uuid,
                                getCommandName(),
                                cooldownKey,
                                String.valueOf(now)
                        ).thenApply(v -> false);
                    }
                    return CompletableFuture.completedFuture(true);
                });
    }

    private void handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("serveressentials.command.lobby.set")) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of(
                            "{permission}",
                            "serveressentials.command.lobby.set"
                    )
            ));
            return;
        }

        Location loc = player.getLocation();
        boolean isWorldSpecific =
                args.length >= 2 &&
                        "world".equalsIgnoreCase(args[1]) &&
                        config.isPerWorld();

        CompletableFuture<Void> saveFuture = isWorldSpecific
                ? lobbyStorage.setWorldLobby(player.getWorld().getName(), loc)
                : lobbyStorage.setLobby(loc);

        saveFuture.thenRun(() -> {
            String messageKey = isWorldSpecific ? "lobby.set-world" : "lobby.set";
            Component message = isWorldSpecific
                    ? langManager.getMessageFor(
                    player,
                    messageKey,
                    "<green>Lobby set!",
                    LanguageManager.ComponentPlaceholder.of(
                            "{world}",
                            player.getWorld().getName()
                    )
            )
                    : langManager.getMessageFor(
                    player,
                    messageKey,
                    "<green>Lobby set!"
            );
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(message));
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to set lobby: " + ex.getMessage());
            return null;
        });
    }

    private void handleRemoveCommand(Player player, String[] args) {
        if (!player.hasPermission("serveressentials.command.lobby.remove")) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of(
                            "{permission}",
                            "serveressentials.command.lobby.remove"
                    )
            ));
            return;
        }

        boolean isWorldSpecific =
                args.length >= 2 &&
                        "world".equalsIgnoreCase(args[1]) &&
                        config.isPerWorld();

        String world = isWorldSpecific ? player.getWorld().getName() : null;

        lobbyStorage.removeLobby(world).thenRun(() ->
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(langManager.getMessageFor(
                                player,
                                "lobby.removed",
                                "<yellow>Lobby removed."
                        ))
                )
        ).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to remove lobby: " + ex.getMessage());
            return null;
        });
    }

    private void handleWorldCommand(Player player, String[] args) {
        if (!player.hasPermission("serveressentials.command.lobby.world") || !config.isPerWorld()) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of(
                            "{permission}",
                            "serveressentials.command.lobby.world"
                    )
            ));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "lobby.usage-world",
                    "<red>Usage: <yellow>/lobby world <world-name>"
            ));
            return;
        }

        String worldName = args[1];
        if (plugin.getServer().getWorld(worldName) == null) {
            player.sendMessage(langManager.getMessageFor(
                    player,
                    "lobby.world-not-found",
                    "<red>World <yellow>{world}</yellow> not found.",
                    LanguageManager.ComponentPlaceholder.of(
                            "{world}",
                            worldName
                    )
            ));
            return;
        }

        lobbyStorage.setWorldLobby(worldName, player.getLocation()).thenRun(() ->
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(langManager.getMessageFor(
                                player,
                                "lobby.set-world",
                                "<green>Lobby for world <yellow>{world}</yellow> set!",
                                LanguageManager.ComponentPlaceholder.of(
                                        "{world}",
                                        worldName
                                )
                        ))
                )
        ).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to set world lobby: " + ex.getMessage());
            return null;
        });
    }
}
