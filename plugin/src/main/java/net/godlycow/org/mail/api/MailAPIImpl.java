package net.godlycow.org.mail.api;

import com.serveressentials.api.mail.MailAPI;
import com.serveressentials.api.mail.MailInfo;
import com.serveressentials.api.mail.MailStats;
import com.serveressentials.api.mail.event.MailSendEvent;
import com.serveressentials.api.mail.event.MailReadEvent;
import com.serveressentials.api.mail.event.MailClearEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.mail.MailConfig;
import net.godlycow.org.mail.model.MailMessage;
import net.godlycow.org.mail.storage.MailStorage;
import net.godlycow.org.mail.trigger.MailListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MailAPIImpl implements MailAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull MailStorage mailStorage;
    private final @NotNull MailConfig mailConfig;
    private final @NotNull MailListener mailListener;

    public MailAPIImpl(@NotNull ServerEssentials plugin,
                       @NotNull MailStorage mailStorage,
                       @NotNull MailConfig mailConfig,
                       @NotNull MailListener mailListener) {
        this.plugin = plugin;
        this.mailStorage = mailStorage;
        this.mailConfig = mailConfig;
        this.mailListener = mailListener;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> sendMail(@NotNull Player sender,
                                                        @NotNull String targetName,
                                                        @NotNull String message) {
        return CompletableFuture.supplyAsync(() -> {
            long remainingCooldown = getRemainingCooldown(sender.getUniqueId()).join();
            if (remainingCooldown > 0) {
                return false;
            }

            if (message.length() > mailConfig.maxLength) {
                return false;
            }

            OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(targetName);
            if (target == null || target.getName() == null) {
                target = plugin.getServer().getOfflinePlayer(targetName);
                if (target.getName() == null) {
                    return false;
                }
            }

            String plainMessage = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand()
                    .serialize(net.kyori.adventure.text.Component.text(message));

            MailMessage mailMessage = MailMessage.create(
                    sender.getUniqueId(),
                    sender.getName(),
                    plainMessage
            );

            mailStorage.addMail(target.getUniqueId(), mailMessage).join();
            mailStorage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis()).join();

            MailInfo mailInfo = new MailInfo(
                    sender.getUniqueId(),
                    sender.getName(),
                    message,
                    System.currentTimeMillis(),
                    target.getUniqueId(),
                    false
            );

            OfflinePlayer finalTarget = target;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(
                        new MailSendEvent(sender, mailInfo, finalTarget.getName(), finalTarget.getUniqueId())
                );
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<List<MailInfo>> getMailbox(@NotNull Player player) {
        return mailStorage.getMailbox(player.getUniqueId())
                .thenApply(mails -> mails.stream()
                        .map(mail -> new MailInfo(
                                mail.senderId(),
                                mail.senderName(),
                                mail.message(),
                                mail.timestamp(),
                                player.getUniqueId(),
                                false
                        ))
                        .collect(Collectors.toList()))
                .thenCompose(mailInfos -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new MailReadEvent(player, mailInfos));
                    });
                    return CompletableFuture.completedFuture(mailInfos);
                });
    }

    @Override
    public @NotNull CompletableFuture<List<MailInfo>> getUnreadMailbox(@NotNull Player player) {
        return mailStorage.getUnreadCount(player.getUniqueId())
                .thenCompose(unreadCount -> {
                    if (unreadCount == 0) {
                        return CompletableFuture.completedFuture(List.of());
                    }
                    return getMailbox(player).thenApply(mailInfos ->
                            mailInfos.subList(0, Math.min(unreadCount, mailInfos.size()))
                    );
                });
    }

    @Override
    public @NotNull CompletableFuture<MailStats> getMailStats(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            int totalCount = mailStorage.getTotalMailCount(playerId).join();
            int unreadCount = mailStorage.getUnreadCount(playerId).join();
            long lastActivity = mailStorage.getCooldown(playerId).join();
            return new MailStats(playerId, totalCount, unreadCount, lastActivity);
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> markAllAsRead(@NotNull UUID playerId) {
        return mailStorage.markAllAsRead(playerId);
    }

    @Override
    public @NotNull CompletableFuture<Void> clearMailbox(@NotNull UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            int count = mailStorage.getTotalMailCount(playerId).join();
            mailStorage.clearMailbox(playerId).join();

            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new MailClearEvent(player, count));
                });
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId) {
        return mailStorage.getCooldown(playerId).thenApply(lastUsed -> {
            if (lastUsed == 0) {
                return 0L;
            }
            long now = System.currentTimeMillis();
            int cooldownMs = mailConfig.cooldown * 1000;
            long elapsed = now - lastUsed;
            long remaining = cooldownMs - elapsed;
            return Math.max(0, remaining / 1000);
        });
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("mail.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            mailConfig.reload();
            plugin.getLogger().info("[ServerEssentials] Mail configuration reloaded");
        });
    }

    /**
     * Resets the join notification for a player.
     *
     * @param playerId The UUID of the player
     */
    public void resetJoinNotification(@NotNull UUID playerId) {
        mailListener.resetNotification(playerId);
    }
}