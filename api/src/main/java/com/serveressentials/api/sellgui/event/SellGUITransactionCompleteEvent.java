package com.serveressentials.api.sellgui.event;

import com.serveressentials.api.sellgui.SellTransaction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public final class SellGUITransactionCompleteEvent extends SellGUIEvent {
    private final @NotNull SellTransaction transaction;

    public SellGUITransactionCompleteEvent(@NotNull Player player, @NotNull SellTransaction transaction) {
        super(player);
        this.transaction = Objects.requireNonNull(transaction, "transaction cannot be null");
    }

    public @NotNull SellTransaction getTransaction() {
        return transaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SellGUITransactionCompleteEvent that = (SellGUITransactionCompleteEvent) o;
        return Objects.equals(transaction, that.transaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transaction);
    }

    @Override
    public @NotNull String toString() {
        return "SellGUITransactionCompleteEvent{" + "player=" + getPlayer().getName() + ", transaction=" + transaction + '}';
    }
}