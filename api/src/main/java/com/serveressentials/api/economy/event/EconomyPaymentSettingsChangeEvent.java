package com.serveressentials.api.economy.event;

import com.serveressentials.api.economy.EconomyPaymentSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class EconomyPaymentSettingsChangeEvent extends EconomyEvent {
    private final @NotNull EconomyPaymentSettings oldSettings;
    private final @NotNull EconomyPaymentSettings newSettings;

    public EconomyPaymentSettingsChangeEvent(@NotNull OfflinePlayer player,
                                             @NotNull EconomyPaymentSettings oldSettings,
                                             @NotNull EconomyPaymentSettings newSettings) {
        super(player);
        this.oldSettings = Objects.requireNonNull(oldSettings, "oldSettings cannot be null");
        this.newSettings = Objects.requireNonNull(newSettings, "newSettings cannot be null");
    }

    public @NotNull EconomyPaymentSettings getOldSettings() { return oldSettings; }
    public @NotNull EconomyPaymentSettings getNewSettings() { return newSettings; }
}