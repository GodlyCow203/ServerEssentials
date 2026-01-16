package com.serveressentials.api.economy.event;

import com.serveressentials.api.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class EconomyDepositEvent extends EconomyEvent {
    private final double amount;
    private final @NotNull EconomyResponse response;

    public EconomyDepositEvent(@NotNull OfflinePlayer player, double amount, @NotNull EconomyResponse response) {
        super(player);
        this.amount = amount;
        this.response = Objects.requireNonNull(response, "response cannot be null");
    }

    public double getAmount() { return amount; }
    public @NotNull EconomyResponse getResponse() { return response; }
}