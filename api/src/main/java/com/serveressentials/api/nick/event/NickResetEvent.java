package com.serveressentials.api.nick.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;


public final class NickResetEvent extends NickEvent {
    private final @NotNull UUID targetId;
    private final @NotNull String targetName;
    private final boolean isSelf;

    public NickResetEvent(@NotNull Player player, @NotNull UUID targetId, @NotNull String targetName, boolean isSelf) {
        super(player);
        this.targetId = targetId;
        this.targetName = targetName;
        this.isSelf = isSelf;
    }

    /**
     * Gets the UUID of the player whose nickname was reset.
     *
     * @return The target player UUID
     */
    public @NotNull UUID getTargetId() {
        return targetId;
    }

    /**
     * Gets the name of the player whose nickname was reset.
     *
     * @return The target player name
     */
    public @NotNull String getTargetName() {
        return targetName;
    }

    /**
     * Checks if the player reset their own nickname.
     *
     * @return true if self-reset, false if admin reset another player
     */
    public boolean isSelf() {
        return isSelf;
    }
}