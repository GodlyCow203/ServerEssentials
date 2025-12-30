package com.serveressentials.api.mail.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public final class MailClearEvent extends MailEvent {
    private final int clearedCount;

    public MailClearEvent(@NotNull Player player, int clearedCount) {
        super(player);
        this.clearedCount = clearedCount;
    }

    /**
     * Gets the number of mail messages that were cleared.
     *
     * @return The number of cleared messages
     */
    public int getClearedCount() {
        return clearedCount;
    }
}