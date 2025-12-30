package com.serveressentials.api.nick.event;

import com.serveressentials.api.nick.NickInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class NickSetEvent extends NickEvent {
    private final @NotNull NickInfo nickInfo;
    private final @NotNull String oldNickname;

    public NickSetEvent(@NotNull Player player, @NotNull NickInfo nickInfo, @NotNull String oldNickname) {
        super(player);
        this.nickInfo = Objects.requireNonNull(nickInfo, "nickInfo cannot be null");
        this.oldNickname = Objects.requireNonNull(oldNickname, "oldNickname cannot be null");
    }

    /**
     * Gets the new nickname information.
     *
     * @return The nick info
     */
    public @NotNull NickInfo getNickInfo() {
        return nickInfo;
    }

    /**
     * Gets the player's previous nickname.
     *
     * @return The old nickname
     */
    public @NotNull String getOldNickname() {
        return oldNickname;
    }
}