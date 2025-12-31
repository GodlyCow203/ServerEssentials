package net.godlycow.org.tpa.api;

import com.serveressentials.api.tpa.*;
import com.serveressentials.api.tpa.event.*;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.tpa.TPAConfig;
import net.godlycow.org.tpa.model.TPARequest;
import net.godlycow.org.tpa.storage.TPAStorage;
import net.godlycow.org.tpa.trigger.TPAListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// Developerss Note: Economy is not yet Implemented 12/31/25

public final class TPAAPIImpl implements TPAAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull TPAConfig config;
    private final @NotNull TPAStorage storage;
    private final @NotNull TPAListener listener;

    public TPAAPIImpl(@NotNull ServerEssentials plugin, @NotNull TPAConfig config,
                      @NotNull TPAStorage storage, @NotNull TPAListener listener) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.listener = listener;
    }

    @Override
    public @NotNull CompletableFuture<TPARequestData> sendRequest(@NotNull Player sender, @NotNull Player target, boolean here) {
        return CompletableFuture.supplyAsync(() -> {
            if (!validateRequest(sender, target)) {
                throw new IllegalArgumentException("Invalid request");
            }

            double cost = here ? config.costTpahere : config.costTpa;
            if (cost > 0) {
            }

            TPARequest request = TPARequest.create(sender.getUniqueId(), target.getUniqueId(), here, cost);
            storage.saveRequest(request).join();

            TPARequestData requestData = new TPARequestData(
                    request.senderId, request.targetId, request.here, request.cost, request.timestamp
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new TPARequestSendEvent(sender, requestData));
            });

            storage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis()).join();

            startTimeout(request);

            return requestData;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> acceptRequest(@NotNull Player target, @Nullable UUID senderId) {
        return storage.getActiveRequests(target.getUniqueId()).thenApply(requests -> {
            if (requests.isEmpty()) {
                return false;
            }

            TPARequest request;
            if (senderId != null) {
                request = findRequestBySender(requests, senderId);
                if (request == null) {
                    return false;
                }
            } else {
                request = requests.get(requests.size() - 1);
            }

            storage.removeRequest(request.senderId, request.targetId).join();
            acceptRequestInternal(request);

            TPARequestData requestData = new TPARequestData(
                    request.senderId, request.targetId, request.here, request.cost, request.timestamp
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new TPARequestAcceptEvent(target, requestData));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> denyRequest(@NotNull Player target, @Nullable UUID senderId) {
        return storage.getActiveRequests(target.getUniqueId()).thenApply(requests -> {
            if (requests.isEmpty()) {
                return false;
            }

            TPARequest request;
            if (senderId != null) {
                request = findRequestBySender(requests, senderId);
                if (request == null) {
                    return false;
                }
            } else {
                request = requests.get(requests.size() - 1);
            }

            storage.removeRequest(request.senderId, request.targetId).join();
            denyRequestInternal(request);

            TPARequestData requestData = new TPARequestData(
                    request.senderId, request.targetId, request.here, request.cost, request.timestamp
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new TPARequestDenyEvent(target, requestData));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Integer> cancelRequest(@NotNull Player sender, @Nullable UUID targetId) {
        if (targetId == null) {
            return storage.getActiveRequestsForSender(sender.getUniqueId()).thenApply(requests -> {
                int count = requests.size();
                requests.forEach(req -> storage.removeRequest(req.senderId, req.targetId).join());
                return count;
            });
        } else {
            return storage.removeRequest(sender.getUniqueId(), targetId)
                    .thenApply(v -> 1)
                    .exceptionally(ex -> 0);
        }
    }

    @Override
    public @NotNull CompletableFuture<Integer> sendRequestToAll(@NotNull Player sender) {
        return CompletableFuture.supplyAsync(() -> {
            double cost = config.costTpall;
            if (cost > 0) {

            }

            int sent = 0;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(sender)) continue;
                if (isRequestsDisabled(target.getUniqueId())) continue;

                TPARequest request = TPARequest.create(sender.getUniqueId(), target.getUniqueId(), true, 0);
                storage.saveRequest(request).join();

                TPARequestData requestData = new TPARequestData(
                        request.senderId, request.targetId, request.here, request.cost, request.timestamp
                );

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new TPARequestSendEvent(sender, requestData));
                });

                startTimeout(request);
                sent++;
            }

            storage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis()).join();
            return sent;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> toggleRequests(@NotNull Player player) {
        return storage.getToggle(player.getUniqueId()).thenApply(current -> {
            boolean newState = !current;
            storage.setToggle(player.getUniqueId(), newState).join();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new TPAToggleEvent(player, newState));
            });

            return newState;
        });
    }

    @Override
    public @NotNull CompletableFuture<TPAPlayerData> getPlayerData(@NotNull UUID playerId) {
        return CompletableFuture.allOf(
                storage.getCooldown(playerId),
                storage.getToggle(playerId),
                storage.getActiveRequestsForSender(playerId),
                storage.getActiveRequests(playerId)
        ).thenApply(v -> {
            long cooldown = storage.getCooldown(playerId).join();
            boolean toggle = storage.getToggle(playerId).join();
            int sent = storage.getActiveRequestsForSender(playerId).join().size();
            int received = storage.getActiveRequests(playerId).join().size();

            return new TPAPlayerData(playerId, cooldown, toggle, sent, received);
        });
    }

    @Override
    public @NotNull CompletableFuture<List<TPARequestData>> getActiveRequests(@NotNull UUID targetId) {
        return storage.getActiveRequests(targetId).thenApply(requests ->
                requests.stream().map(req -> new TPARequestData(
                        req.senderId, req.targetId, req.here, req.cost, req.timestamp
                )).collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull TPASettings getSettings() {
        return new TPASettings(
                config.cooldown, config.timeout, config.warmup, config.teleportDelay,
                config.cancelOnMove, config.blockMoveThresholdBlocks, config.crossWorld,
                config.blockedWorlds, config.economyEnabled, config.particlesEnabled, config.particleType
        );
    }

    @Override
    public @NotNull TPACosts getCosts() {
        return new TPACosts(
                config.costTpa, config.costTpahere, config.costTpall,
                config.refundOnDeny, config.refundOnExpire
        );
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            plugin.getLogger().info("[ServerEssentials] TPA configuration reloaded");
        });
    }

    private boolean validateRequest(Player sender, Player target) {
        if (config.blockedWorlds.contains(target.getWorld().getName())) {
            return false;
        }

        if (!config.crossWorld && !sender.getWorld().equals(target.getWorld())) {
            return false;
        }

        return !isRequestsDisabled(target.getUniqueId());
    }

    private boolean isRequestsDisabled(UUID playerId) {
        return storage.getToggle(playerId).join();
    }

    private TPARequest findRequestBySender(List<TPARequest> requests, UUID senderId) {
        return requests.stream()
                .filter(req -> req.senderId.equals(senderId))
                .findFirst()
                .orElse(null);
    }

    private void startTimeout(TPARequest request) {
        int timeoutTicks = config.timeout * 20;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            storage.removeRequest(request.senderId, request.targetId).thenRun(() -> {
                Player sender = Bukkit.getPlayer(request.senderId);
                Player target = Bukkit.getPlayer(request.targetId);

                if (sender != null || target != null) {
                    TPARequestData requestData = new TPARequestData(
                            request.senderId, request.targetId, request.here, request.cost, request.timestamp
                    );

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (target != null) {
                            Bukkit.getPluginManager().callEvent(new TPARequestExpireEvent(target, requestData));
                        }
                    });
                }
            });
        }, timeoutTicks);
    }

    private void acceptRequestInternal(TPARequest request) {
        Player sender = Bukkit.getPlayer(request.senderId);
        Player target = Bukkit.getPlayer(request.targetId);

        if (sender == null || target == null) return;

        int warmupTicks = config.warmup * 20;
        int delayTicks = config.teleportDelay * 20;
        int totalDelay = warmupTicks + delayTicks;

        if (warmupTicks > 0) {
            listener.registerWarmupTask(sender.getUniqueId(),
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {}, warmupTicks).getTaskId());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location loc = request.here ? sender.getLocation() : target.getLocation();
            Player toTeleport = request.here ? target : sender;
            toTeleport.teleport(loc);

            listener.unregisterWarmupTask(sender.getUniqueId());
        }, totalDelay);
    }

    private void denyRequestInternal(TPARequest request) {
        if (config.economyEnabled && config.refundOnDeny && request.cost > 0) {
        }
    }
}