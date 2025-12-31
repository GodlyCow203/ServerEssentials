package com.serveressentials.api.sellgui;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class SellGUILayout {
    private final @NotNull String title;
    private final int size;
    private final @NotNull String currencySymbol;

    public SellGUILayout(@NotNull String title, int size, @NotNull String currencySymbol) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.size = size;
        this.currencySymbol = Objects.requireNonNull(currencySymbol, "currencySymbol cannot be null");
    }

    public @NotNull String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public @NotNull String getCurrencySymbol() {
        return currencySymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellGUILayout that = (SellGUILayout) o;
        return size == that.size && Objects.equals(title, that.title) && Objects.equals(currencySymbol, that.currencySymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, size, currencySymbol);
    }

    @Override
    public @NotNull String toString() {
        return "SellGUILayout{" + "title='" + title + '\'' + ", size=" + size + ", currencySymbol='" + currencySymbol + '\'' + '}';
    }
}