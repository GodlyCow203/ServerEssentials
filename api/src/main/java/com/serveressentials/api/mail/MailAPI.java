package com.serveressentials.api.mail;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public interface MailAPI {

    /**
     * Sends a mail message from one player to another.
     *
     * @param sender The player sending the mail
     * @param targetName The name of the target player
     * @param message The message content
     * @return CompletableFuture that completes with true if mail was sent successfully, false otherwise
     */
    @NotNull CompletableFuture<Boolean> sendMail(@NotNull Player sender, @NotNull String targetName, @NotNull String message);

    /**
     * Gets all mail messages for a player's mailbox.
     *
     * @param player The player whose mailbox to retrieve
     * @return CompletableFuture that completes with a list of mail messages
     */
    @NotNull CompletableFuture<List<MailInfo>> getMailbox(@NotNull Player player);

    /**
     * Gets unread mail messages for a player's mailbox.
     *
     * @param player The player whose unread mail to retrieve
     * @return CompletableFuture that completes with a list of unread mail messages
     */
    @NotNull CompletableFuture<List<MailInfo>> getUnreadMailbox(@NotNull Player player);

    /**
     * Gets mailbox statistics for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with mailbox statistics
     */
    @NotNull CompletableFuture<MailStats> getMailStats(@NotNull UUID playerId);

    /**
     * Marks all mail messages as read for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes when marking is done
     */
    @NotNull CompletableFuture<Void> markAllAsRead(@NotNull UUID playerId);

    /**
     * Clears all mail messages for a player.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes when clearing is done
     */
    @NotNull CompletableFuture<Void> clearMailbox(@NotNull UUID playerId);

    /**
     * Gets the remaining cooldown time for a player to send mail.
     *
     * @param playerId The UUID of the player
     * @return CompletableFuture that completes with remaining cooldown in seconds, or 0 if no cooldown
     */
    @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId);

    /**
     * Checks if the mail feature is enabled.
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Reloads the mail configuration.
     *
     * @return CompletableFuture that completes when reload is finished
     */
    @NotNull CompletableFuture<Void> reload();
}