package com.serveressentials.api.daily;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class DailyRewardItem {
    private final @NotNull Material material;
    private final int amount;
    private final @Nullable String name;
    private final @NotNull List<String> lore;
    private final @NotNull Map<String, Integer> enchantments;
    private final boolean glow;
    private final @NotNull Map<String, String> nbt;

    public DailyRewardItem(@NotNull Material material, int amount, @Nullable String name,
                           @NotNull List<String> lore, @NotNull Map<String, Integer> enchantments,
                           boolean glow, @NotNull Map<String, String> nbt) {
        this.material = Objects.requireNonNull(material, "material cannot be null");
        this.amount = Math.max(1, amount);
        this.name = name;
        this.lore = Objects.requireNonNull(lore, "lore cannot be null");
        this.enchantments = Objects.requireNonNull(enchantments, "enchantments cannot be null");
        this.glow = glow;
        this.nbt = Objects.requireNonNull(nbt, "nbt cannot be null");
    }

    public @NotNull Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public @Nullable String getName() { return name; }
    public @NotNull List<String> getLore() { return List.copyOf(lore); }
    public @NotNull Map<String, Integer> getEnchantments() { return Map.copyOf(enchantments); }
    public boolean isGlow() { return glow; }
    public @NotNull Map<String, String> getNbt() { return Map.copyOf(nbt); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyRewardItem)) return false;
        DailyRewardItem that = (DailyRewardItem) o;
        return amount == that.amount && glow == that.glow &&
                material.equals(that.material) &&
                Objects.equals(name, that.name) &&
                lore.equals(that.lore) &&
                enchantments.equals(that.enchantments) &&
                nbt.equals(that.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, amount, name, lore, enchantments, glow, nbt);
    }
}