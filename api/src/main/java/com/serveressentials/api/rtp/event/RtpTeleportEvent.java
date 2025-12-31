package com.serveressentials.api.rtp.event;

import com.serveressentials.api.rtp.RtpLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;


public final class RtpTeleportEvent extends RtpEvent {
    private final @NotNull RtpLocation rtpLocation;
    private final @Nullable Location fromLocation;
    private final @NotNull String worldName;

    public RtpTeleportEvent(@NotNull Player player, @NotNull RtpLocation rtpLocation,
                            @Nullable Location fromLocation, @NotNull String worldName) {
        super(player);
        this.rtpLocation = Objects.requireNonNull(rtpLocation, "rtpLocation cannot be null");
        this.fromLocation = fromLocation;
        this.worldName = Objects.requireNonNull(worldName, "worldName cannot be null");
    }

    /**
     * Gets the RTP location information.
     *
     * @return The RTP location
     */
    public @NotNull RtpLocation getRtpLocation() {
        return rtpLocation;
    }

    /**
     * Gets the player's original location before teleport.
     *
     * @return The original location, or null if not available
     */
    public @Nullable Location getFromLocation() {
        return fromLocation;
    }

    /**
     * Gets the target world name.
     *
     * @return The world name
     */
    public @NotNull String getWorldName() {
        return worldName;
    }
}