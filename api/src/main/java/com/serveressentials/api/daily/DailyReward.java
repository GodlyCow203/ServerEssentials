package com.serveressentials.api.daily;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class DailyReward {
    private final int day;
    private final int slot;
    private final int page;
    private final @NotNull List<DailyRewardItem> items;

    public DailyReward(int day, int slot, int page, @NotNull List<DailyRewardItem> items) {
        this.day = day;
        this.slot = slot;
        this.page = page;
        this.items = Objects.requireNonNull(items, "items cannot be null");
    }

    public int getDay() { return day; }
    public int getSlot() { return slot; }
    public int getPage() { return page; }
    public @NotNull List<DailyRewardItem> getItems() { return List.copyOf(items); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyReward that = (DailyReward) o;
        return day == that.day && slot == that.slot && page == that.page && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, slot, page, items);
    }

    @Override
    public String toString() {
        return "DailyReward{" + "day=" + day + ", slot=" + slot + ", page=" + page + ", items=" + items + '}';
    }
}