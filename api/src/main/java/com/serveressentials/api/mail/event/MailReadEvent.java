package com.serveressentials.api.mail.event;

import com.serveressentials.api.mail.MailInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Objects;

public final class MailReadEvent extends MailEvent {
    private final @NotNull List<MailInfo> mails;

    public MailReadEvent(@NotNull Player player, @NotNull List<MailInfo> mails) {
        super(player);
        this.mails = Objects.requireNonNull(mails, "mails cannot be null");
    }

    /**
     * Gets the list of mail messages that were read.
     *
     * @return The list of mail info
     */
    public @NotNull List<MailInfo> getMails() {
        return mails;
    }
}