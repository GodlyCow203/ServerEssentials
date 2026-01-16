package com.serveressentials.api.daily.event;

import com.serveressentials.api.daily.DailyReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DailyRewardClaimEvent extends DailyEvent {
    private final int day;
    private final @NotNull DailyReward reward;

    public DailyRewardClaimEvent(@NotNull Player player, int day, @NotNull DailyReward reward) {
        super(player);
        this.day = day;
        this.reward = Objects.requireNonNull(reward, "reward cannot be null");
    }

    public int getDay() { return day; }
    public @NotNull DailyReward getReward() { return reward; }
}