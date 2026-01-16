package com.serveressentials.api.mail.event;

import com.serveressentials.api.mail.MailInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;


public final class MailSendEvent extends MailEvent {
    private final @NotNull MailInfo mailInfo;
    private final @NotNull String targetName;
    private final @Nullable UUID targetId;

    public MailSendEvent(@NotNull Player sender, @NotNull MailInfo mailInfo,
                         @NotNull String targetName, @Nullable UUID targetId) {
        super(sender);
        this.mailInfo = Objects.requireNonNull(mailInfo, "mailInfo cannot be null");
        this.targetName = Objects.requireNonNull(targetName, "targetName cannot be null");
        this.targetId = targetId;
    }

    /**
     * Gets the mail information that was sent.
     *
     * @return The mail info
     */
    public @NotNull MailInfo getMailInfo() {
        return mailInfo;
    }

    /**
     * Gets the name of the target player.
     *
     * @return The target player name
     */
    public @NotNull String getTargetName() {
        return targetName;
    }

    /**
     * Gets the UUID of the target player if available.
     *
     * @return The target player UUID, or null if not available
     */
    public @Nullable UUID getTargetId() {
        return targetId;
    }
}