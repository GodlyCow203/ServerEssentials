package com.serveressentials.api.rtp.event;

import com.serveressentials.api.rtp.RtpLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;


public final class RtpLocationSaveEvent extends RtpEvent {
    private final @NotNull RtpLocation rtpLocation;

    public RtpLocationSaveEvent(@NotNull Player player, @NotNull RtpLocation rtpLocation) {
        super(player);
        this.rtpLocation = Objects.requireNonNull(rtpLocation, "rtpLocation cannot be null");
    }

    /**
     * Gets the RTP location that was saved.
     *
     * @return The RTP location
     */
    public @NotNull RtpLocation getRtpLocation() {
        return rtpLocation;
    }
}