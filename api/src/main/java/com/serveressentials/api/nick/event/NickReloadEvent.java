package com.serveressentials.api.nick.event;

import com.serveressentials.api.nick.NickInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;


public final class NickReloadEvent extends NickEvent {
    private final @NotNull List<NickInfo> affectedNicks;

    public NickReloadEvent(@NotNull Player player, @NotNull List<NickInfo> affectedNicks) {
        super(player);
        this.affectedNicks = affectedNicks;
    }

    /**
     * Gets all nicknames that were reloaded.
     *
     * @return List of affected nick info
     */
    public @NotNull List<NickInfo> getAffectedNicks() {
        return affectedNicks;
    }
}