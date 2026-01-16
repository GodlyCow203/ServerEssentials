package net.godlycow.org.nick.api;

import com.serveressentials.api.nick.NickAPI;
import com.serveressentials.api.nick.NickInfo;
import com.serveressentials.api.nick.NickValidationRules;
import com.serveressentials.api.nick.event.NickSetEvent;
import com.serveressentials.api.nick.event.NickResetEvent;
import net.godlycow.org.EssC;
import net.godlycow.org.commands.config.NickConfig;
import net.godlycow.org.nick.NickManager;
import net.godlycow.org.nick.storage.NickStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class NickAPIImpl implements NickAPI {
    private final @NotNull EssC plugin;
    private final @NotNull NickStorage nickStorage;
    private final @NotNull NickConfig nickConfig;
    private final @NotNull NickManager nickManager;

    public NickAPIImpl(@NotNull EssC plugin,
                       @NotNull NickStorage nickStorage,
                       @NotNull NickConfig nickConfig,
                       @NotNull NickManager nickManager) {
        this.plugin = plugin;
        this.nickStorage = nickStorage;
        this.nickConfig = nickConfig;
        this.nickManager = nickManager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> setNickname(@NotNull Player player, @NotNull String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            if (!validateNickname(player, nickname).join()) {
                return false;
            }

            UUID playerId = player.getUniqueId();

            String oldNickname = nickManager.getAllCachedNicks().getOrDefault(playerId, player.getName());

            long remainingCooldown = getRemainingCooldown(playerId).join();
            if (remainingCooldown > 0) {
                return false;
            }

            int dailyChanges = getDailyChanges(playerId).join();
            if (nickConfig.maxChangesPerDay > 0 && dailyChanges >= nickConfig.maxChangesPerDay) {
                return false;
            }

            nickStorage.setNickname(playerId, nickname).join();
            if (nickConfig.maxChangesPerDay > 0) {
                nickStorage.incrementDailyChanges(playerId).join();
            }

            nickManager.applyNick(playerId, nickname);

            NickInfo nickInfo = new NickInfo(
                    playerId,
                    player.getName(),
                    nickname,
                    System.currentTimeMillis(),
                    dailyChanges + 1
            );

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new NickSetEvent(player, nickInfo, oldNickname));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> resetNickname(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            UUID playerId = player.getUniqueId();

            nickStorage.removeNickname(playerId).join();

            nickManager.removeNick(playerId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new NickResetEvent(player, playerId, player.getName(), true));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> resetOtherNickname(@NotNull Player sender, @NotNull String targetName) {
        return CompletableFuture.supplyAsync(() -> {
            if (!sender.hasPermission("essc.command.nicks")) {
                return false;
            }

            @Nullable Player target = plugin.getServer().getPlayer(targetName);
            if (target == null) {
                return false;
            }

            UUID targetId = target.getUniqueId();

            nickStorage.removeNickname(targetId).join();

            nickManager.removeNick(targetId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new NickResetEvent(sender, targetId, target.getName(), false));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<NickInfo>> getNickname(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, String> cache = nickManager.getAllCachedNicks();
            if (cache.containsKey(playerId)) {
                String nickname = cache.get(playerId);
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    return Optional.of(new NickInfo(playerId, player.getName(), nickname, System.currentTimeMillis(), 0));
                }
            }

            return nickStorage.getNickname(playerId).join().map(nickname -> {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    return new NickInfo(playerId, player.getName(), nickname, System.currentTimeMillis(), 0);
                }
                return null;
            });
        });
    }

    @Override
    public @NotNull List<NickInfo> getAllNicknames() {
        Map<UUID, String> cache = nickManager.getAllCachedNicks();
        return cache.entrySet().stream()
                .map(entry -> {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null) {
                        return new NickInfo(entry.getKey(), player.getName(), entry.getValue(), System.currentTimeMillis(), 0);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull NickValidationRules getValidationRules() {
        return new NickValidationRules(
                nickConfig.minLength,
                nickConfig.maxLength,
                nickConfig.allowFormatting,
                nickConfig.allowReset,
                nickConfig.allowDuplicates,
                nickConfig.cooldown,
                nickConfig.maxChangesPerDay,
                nickConfig.blockedWords,
                nickConfig.blacklistPatterns
        );
    }

    @Override
    public @NotNull CompletableFuture<Boolean> validateNickname(@NotNull Player player, @NotNull String nickname) {
        return CompletableFuture.supplyAsync(() -> {
            String stripped = stripFormatting(nickname);

            if (stripped.length() < nickConfig.minLength || stripped.length() > nickConfig.maxLength) {
                return false;
            }

            for (String word : nickConfig.blockedWords) {
                if (stripped.toLowerCase().contains(word.toLowerCase())) {
                    return false;
                }
            }

            for (String pattern : nickConfig.blacklistPatterns) {
                try {
                    if (java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE)
                            .matcher(stripped).find()) {
                        return false;
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }

            if (!nickConfig.allowFormatting && containsFormatting(nickname)) {
                return false;
            }

            if (!nickConfig.allowDuplicates) {
                for (Map.Entry<UUID, String> entry : nickManager.getAllCachedNicks().entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(nickname) && !entry.getKey().equals(player.getUniqueId())) {
                        return false;
                    }
                }
            }

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId) {
        if (nickConfig.cooldown <= 0) {
            return CompletableFuture.completedFuture(0L);
        }

        return nickStorage.getNickname(playerId).thenCompose(opt -> {
            if (!opt.isPresent()) {
                return CompletableFuture.completedFuture(0L);
            }
            return CompletableFuture.completedFuture(0L);
        });
    }

    @Override
    public @NotNull CompletableFuture<Integer> getDailyChanges(@NotNull UUID playerId) {
        String today = java.time.LocalDate.now().toString();
        return nickStorage.getDailyChanges(playerId, today).thenApply(opt -> opt.orElse(0));
    }

    @Override
    public boolean isEnabled() {
        return nickConfig.enabled;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            nickConfig.reload();
            nickManager.reloadAllNicks();
            plugin.getLogger().info("[ServerEssentials] Nickname configuration reloaded");
        });
    }

    private @NotNull String stripFormatting(@NotNull String input) {
        return input.replaceAll("<[^>]+>", "");
    }

    private boolean containsFormatting(@NotNull String input) {
        return input.contains("<") && input.contains(">");
    }
}