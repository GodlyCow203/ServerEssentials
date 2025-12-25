package net.godlycow.org.commands.impl;

import net.godlycow.org.tpa.TPAConfig;
import net.godlycow.org.tpa.trigger.TPAListener;
import net.godlycow.org.tpa.model.TPARequest;
import net.godlycow.org.tpa.storage.TPAStorage;
import net.kyori.adventure.text.Component;

import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TPACommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final TPAStorage storage;
    private final TPAConfig config;
    private final TPAListener listener;
    private final Map<UUID, Long> activeCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> activeToggles = ConcurrentHashMap.newKeySet();

    public TPACommand(JavaPlugin plugin, PlayerLanguageManager langManager, TPAStorage storage,
                      TPAConfig config, TPAListener listener) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.listener = listener;
        loadInitialData();
    }

    private void loadInitialData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(player ->
                    storage.getToggle(player.getUniqueId()).thenAccept(toggle -> {
                        if (toggle) activeToggles.add(player.getUniqueId());
                    })
            );
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        String name = cmd.getName().toLowerCase();

        if (!player.hasPermission("serveressentials.command.tpa.bypass.cooldown")) {
            storage.getCooldown(player.getUniqueId()).thenAccept(cooldownTime -> {
                long now = System.currentTimeMillis();
                int cd = config.cooldown;
                if (cooldownTime > 0 && (now - cooldownTime) < cd * 1000L) {
                    long remaining = cd - TimeUnit.MILLISECONDS.toSeconds(now - cooldownTime);
                    Component msg = langManager.getMessageFor(player, "tpa.cooldown",
                            "<red>Please wait {time} seconds before using TPA again.",
                            LanguageManager.ComponentPlaceholder.of("{time}", String.valueOf(remaining))
                    );
                    player.sendMessage(msg);
                    return;
                }
                processCommand(player, name, args);
            });
            return true;
        }

        return processCommand(player, name, args);
    }

    private boolean processCommand(Player player, String name, String[] args) {

        // Build the permission dynamically
        String permission = "serveressentials.command." + name.toLowerCase();

        if (!player.hasPermission(permission)) {
            return true;
        }

        switch (name) {

            case "tpa":
                if (args.length == 0) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.usage.tpa",
                            "<red>Usage: /tpa <player>").toString());
                    return false;
                }
                handleTpa(player, args[0], false);
                break;

            case "tpahere":
                if (args.length == 0) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.usage.tpahere",
                            "<red>Usage: /tpahere <player>").toString());
                    return false;
                }
                handleTpa(player, args[0], true);
                break;

            case "tpaccept":
                handleAcceptDeny(player, args.length > 0 ? args[0] : null, true);
                break;

            case "tpdeny":
                handleAcceptDeny(player, args.length > 0 ? args[0] : null, false);
                break;

            case "tpacancel":
                handleCancel(player, args.length > 0 ? args[0] : null);
                break;

            case "tpall":
                handleTpall(player);
                break;

            case "tpatoggle":
                handleToggle(player);
                break;

            case "tpainfo":
                handleInfo(player);
                break;

            default:
                return false;
        }
        return true;
    }


    private void handleTpa(Player sender, String targetName, boolean here) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            Component msg = langManager.getMessageFor(sender, "tpa.no-player",
                    "<red>Player {player} not found.",
                    LanguageManager.ComponentPlaceholder.of("{player}", targetName)
            );
            sender.sendMessage(msg);
            return;
        }

        if (sender.equals(target)) {
            Component msg = langManager.getMessageFor(sender, "tpa.cannot-tpa-self",
                    "<red>You cannot send a TPA request to yourself.");
            sender.sendMessage(msg);
            return;
        }

        if (!validateRequest(sender, target)) return;

        double cost = here ? config.costTpahere : config.costTpa;
        if (cost > 0 && !chargePlayer(sender, cost)) return;

        TPARequest request = TPARequest.create(sender.getUniqueId(), target.getUniqueId(), here, cost);

        storage.saveRequest(request).thenRun(() -> {
            Component senderMsg = langManager.getMessageFor(sender, here ? "tpa.request-sent-here" : "tpa.request-sent",
                    "<green>TPA request sent to {player}.",
                    LanguageManager.ComponentPlaceholder.of("{player}", target.getName())
            );
            sender.sendMessage(senderMsg);

            Component targetMsg = langManager.getMessageFor(target, here ? "tpa.request-received-here" : "tpa.request-received",
                    "<green>{player} wants to teleport to you. Use <yellow>/tpaccept {player}</yellow> or <red>/tpdeny {player}</red>",
                    LanguageManager.ComponentPlaceholder.of("{player}", sender.getName())
            );
            target.sendMessage(targetMsg);

            startTimeout(request);

            storage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis());
        });
    }

    private void handleAcceptDeny(Player player, String senderName, boolean accept) {
        storage.getActiveRequests(player.getUniqueId()).thenAccept(requests -> {
            if (requests.isEmpty()) {
                Component msg = langManager.getMessageFor(player, "tpa.no-request",
                        "<red>You have no pending TPA requests.");
                player.sendMessage(msg);
                return;
            }

            TPARequest request = senderName != null ?
                    findRequestByName(requests, senderName) :
                    requests.get(requests.size() - 1);

            if (request == null && senderName != null) {
                Component msg = langManager.getMessageFor(player, "tpa.no-request-from-player",
                        "<red>No pending request from {player}.",
                        LanguageManager.ComponentPlaceholder.of("{player}", senderName));
                player.sendMessage(msg);
                return;
            }

            if (request == null) {
                Component msg = langManager.getMessageFor(player, "tpa.no-request",
                        "<red>You have no pending TPA requests.");
                player.sendMessage(msg);
                return;
            }

            storage.removeRequest(request.senderId, request.targetId).thenRun(() -> {
                if (accept) {
                    acceptRequest(request);
                } else {
                    denyRequest(request);
                }
            });
        });
    }

    private void handleCancel(Player sender, String targetName) {
        if (targetName == null) {
            storage.getActiveRequestsForSender(sender.getUniqueId()).thenAccept(requests -> {
                if (requests.isEmpty()) {
                    Component msg = langManager.getMessageFor(sender, "tpa.no-requests-sent",
                            "<red>You have no pending TPA requests to cancel.");
                    sender.sendMessage(msg);
                    return;
                }

                requests.forEach(req ->
                        storage.removeRequest(req.senderId, req.targetId).thenRun(() -> {
                            Player target = Bukkit.getPlayer(req.targetId);
                            if (target != null) {
                                Component msg = langManager.getMessageFor(target, "tpa.request-cancelled-target",
                                        "<yellow>{player} cancelled their TPA request.",
                                        LanguageManager.ComponentPlaceholder.of("{player}", sender.getName()));
                                target.sendMessage(msg);
                            }
                        })
                );

                Component msg = langManager.getMessageFor(sender, "tpa.request-cancelled-all",
                        "<green>Cancelled all your TPA requests.");
                sender.sendMessage(msg);
            });
        } else {
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                Component msg = langManager.getMessageFor(sender, "tpa.no-player",
                        "<red>Player {player} not found.",
                        LanguageManager.ComponentPlaceholder.of("{player}", targetName));
                sender.sendMessage(msg);
                return;
            }

            storage.removeRequest(sender.getUniqueId(), target.getUniqueId()).thenRun(() -> {
                Component senderMsg = langManager.getMessageFor(sender, "tpa.request-cancelled",
                        "<green>Cancelled TPA request to {player}.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()));
                sender.sendMessage(senderMsg);

                Component targetMsg = langManager.getMessageFor(target, "tpa.request-cancelled-target",
                        "<yellow>{player} cancelled their TPA request.",
                        LanguageManager.ComponentPlaceholder.of("{player}", sender.getName()));
                target.sendMessage(targetMsg);
            });
        }
    }

    private void handleTpall(Player sender) {
        double cost = config.costTpall;
        if (cost > 0 && !chargePlayer(sender, cost)) return;

        int sent = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(sender)) continue;
            if (activeToggles.contains(target.getUniqueId())) continue;

            TPARequest request = TPARequest.create(sender.getUniqueId(), target.getUniqueId(), true, 0);
            storage.saveRequest(request);

            Component msg = langManager.getMessageFor(target, "tpa.request-received-all",
                    "<green>{player} wants everyone to teleport to them. Use <yellow>/tpaccept {player}</yellow>",
                    LanguageManager.ComponentPlaceholder.of("{player}", sender.getName()));
            target.sendMessage(msg);

            startTimeout(request);
            sent++;
        }

        Component senderMsg = langManager.getMessageFor(sender, "tpa.request-sent-all",
                "<green>Sent TPA request to {count} players.",
                LanguageManager.ComponentPlaceholder.of("{count}", String.valueOf(sent)));
        sender.sendMessage(senderMsg);

        storage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis());
    }

    private void handleToggle(Player player) {
        boolean current = activeToggles.contains(player.getUniqueId());
        boolean newState = !current;

        storage.setToggle(player.getUniqueId(), newState).thenRun(() -> {
            if (newState) {
                activeToggles.add(player.getUniqueId());
                Component msg = langManager.getMessageFor(player, "tpa.toggle-on",
                        "<green>TPA requests are now <bold>DISABLED</bold>.");
                player.sendMessage(msg);
            } else {
                activeToggles.remove(player.getUniqueId());
                Component msg = langManager.getMessageFor(player, "tpa.toggle-off",
                        "<green>TPA requests are now <bold>ENABLED</bold>.");
                player.sendMessage(msg);
            }
        });
    }

    private void handleInfo(Player player) {
        CompletableFuture.allOf(
                storage.getActiveRequests(player.getUniqueId()),
                storage.getActiveRequestsForSender(player.getUniqueId())
        ).thenAccept((result) -> {
            storage.getActiveRequests(player.getUniqueId()).thenAccept(incoming -> {
                storage.getActiveRequestsForSender(player.getUniqueId()).thenAccept(outgoing -> {
                    if (incoming.isEmpty() && outgoing.isEmpty()) {
                        Component msg = langManager.getMessageFor(player, "tpa.no-request",
                                "<red>You have no pending TPA requests.");
                        player.sendMessage(msg);
                    } else {
                        if (!incoming.isEmpty()) {
                            player.sendMessage(langManager.getMessageFor(player, "tpa.info-incoming-header",
                                    "<gold>Incoming requests:").toString());
                            incoming.forEach(req -> {
                                String senderName = Bukkit.getOfflinePlayer(req.senderId).getName();
                                player.sendMessage(langManager.getMessageFor(player, "tpa.info-request",
                                        "<yellow>- {player} ({type})",
                                        LanguageManager.ComponentPlaceholder.of("{player}", senderName),
                                        LanguageManager.ComponentPlaceholder.of("{type}", req.here ? "TPAHere" : "TPA")
                                ).toString());
                            });
                        }

                        if (!outgoing.isEmpty()) {
                            player.sendMessage(langManager.getMessageFor(player, "tpa.info-outgoing-header",
                                    "<gold>Outgoing requests:").toString());
                            outgoing.forEach(req -> {
                                String targetName = Bukkit.getOfflinePlayer(req.targetId).getName();
                                player.sendMessage(langManager.getMessageFor(player, "tpa.info-request",
                                        "<yellow>- {player} ({type})",
                                        LanguageManager.ComponentPlaceholder.of("{player}", targetName),
                                        LanguageManager.ComponentPlaceholder.of("{type}", req.here ? "TPAHere" : "TPA")
                                ).toString());
                            });
                        }
                    }
                });
            });
        });
    }

    private boolean validateRequest(Player sender, Player target) {
        if (activeToggles.contains(target.getUniqueId())) {
            Component msg = langManager.getMessageFor(sender, "tpa.target-toggled",
                    "<red>{player} has TPA disabled.",
                    LanguageManager.ComponentPlaceholder.of("{player}", target.getName())
            );
            sender.sendMessage(msg);
            return false;
        }

        if (!config.crossWorld && !sender.getWorld().equals(target.getWorld())) {
            Component msg = langManager.getMessageFor(sender, "tpa.crossworld-disabled",
                    "<red>Cross-world teleportation is disabled.");
            sender.sendMessage(msg);
            return false;
        }

        if (config.blockedWorlds.contains(target.getWorld().getName())) {
            Component msg = langManager.getMessageFor(sender, "tpa.blocked-world",
                    "<red>You cannot teleport to this world.");
            sender.sendMessage(msg);
            return false;
        }

        return true;
    }

    private boolean chargePlayer(Player player, double cost) {
        if (!config.economyEnabled || cost <= 0) return true;
        return true;
    }

    private void startTimeout(TPARequest request) {
        int timeoutTicks = config.timeout * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            storage.removeRequest(request.senderId, request.targetId).thenRun(() -> {
                Player sender = Bukkit.getPlayer(request.senderId);
                Player target = Bukkit.getPlayer(request.targetId);

                if (sender != null) {
                    Component msg = langManager.getMessageFor(sender, "tpa.request-expired",
                            "<red>Your TPA request to {player} expired.",
                            LanguageManager.ComponentPlaceholder.of("{player}", target != null ? target.getName() : "Unknown")
                    );
                    sender.sendMessage(msg);
                }

                if (target != null) {
                    Component msg = langManager.getMessageFor(target, "tpa.request-expired-target",
                            "<red>TPA request from {player} expired.",
                            LanguageManager.ComponentPlaceholder.of("{player}", sender != null ? sender.getName() : "Unknown")
                    );
                    target.sendMessage(msg);
                }
            });
        }, timeoutTicks);
    }

    private TPARequest findRequestByName(List<TPARequest> requests, String senderName) {
        return requests.stream()
                .filter(req -> {
                    String name = Bukkit.getOfflinePlayer(req.senderId).getName();
                    return name != null && name.equalsIgnoreCase(senderName);
                })
                .findFirst()
                .orElse(null);
    }

    private void acceptRequest(TPARequest request) {
        Player sender = Bukkit.getPlayer(request.senderId);
        Player target = Bukkit.getPlayer(request.targetId);

        if (sender == null || target == null) {
            return;
        }

        Component senderMsg = langManager.getMessageFor(sender, "tpa.request-accepted",
                "<green>{player} accepted your TPA request. Teleporting in {delay}s...",
                LanguageManager.ComponentPlaceholder.of("{player}", target.getName()),
                LanguageManager.ComponentPlaceholder.of("{delay}", String.valueOf(config.teleportDelay))
        );
        sender.sendMessage(senderMsg);

        Component targetMsg = langManager.getMessageFor(target, "tpa.request-accepted-target",
                "<green>You accepted {player}'s TPA request.",
                LanguageManager.ComponentPlaceholder.of("{player}", sender.getName())
        );
        target.sendMessage(targetMsg);

        int warmupTicks = config.warmup * 20;
        int delayTicks = config.teleportDelay * 20;
        int totalDelay = warmupTicks + delayTicks;

        if (warmupTicks > 0) {
            sender.sendMessage(langManager.getMessageFor(sender, "tpa.warmup-start",
                    "<yellow>Warmup started... Don't move!"));
            listener.registerWarmupTask(sender.getUniqueId(),
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {}, warmupTicks).getTaskId());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                Location loc = request.here ? sender.getLocation() : target.getLocation();
                Player toTeleport = request.here ? target : sender;
                toTeleport.teleport(loc);

                if (config.particlesEnabled) {
                    try {
                        org.bukkit.Particle particle = org.bukkit.Particle.valueOf(config.particleType);
                        loc.getWorld().spawnParticle(particle, loc, 50, 1, 1, 1);
                    } catch (Exception ex) {
                    }
                }

                playSound(sender, config.soundTeleport);
                playSound(target, config.soundTeleport);

                listener.unregisterWarmupTask(sender.getUniqueId());

            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to teleport during TPA: " + ex.getMessage());
            }
        }, totalDelay);
    }

    private void denyRequest(TPARequest request) {
        Player sender = Bukkit.getPlayer(request.senderId);
        Player target = Bukkit.getPlayer(request.targetId);

        if (sender != null) {
            Component msg = langManager.getMessageFor(sender, "tpa.request-denied",
                    "<red>{player} denied your TPA request.",
                    LanguageManager.ComponentPlaceholder.of("{player}", target != null ? target.getName() : "Unknown")
            );
            sender.sendMessage(msg);
            playSound(sender, config.soundDeny);
        }

        if (target != null) {
            Component msg = langManager.getMessageFor(target, "tpa.request-denied-target",
                    "<green>You denied {player}'s TPA request.",
                    LanguageManager.ComponentPlaceholder.of("{player}", sender != null ? sender.getName() : "Unknown")
            );
            target.sendMessage(msg);
            playSound(target, config.soundDeny);
        }

        if (config.economyEnabled && request.cost > 0) {
        }
    }

    private void playSound(Player player, String soundName) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (Exception ex) {
        }
    }
}