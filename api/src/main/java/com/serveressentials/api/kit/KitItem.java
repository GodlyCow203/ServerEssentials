package com.serveressentials.api.kit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Objects;


public final class KitItem {
    private final @NotNull String type;
    private final int amount;
    private final @Nullable String name;
    private final @Nullable java.util.List<String> lore;
    private final @Nullable Map<String, Integer> enchantments;

    public KitItem(
            @NotNull String type,
            int amount,
            @Nullable String name,
            @Nullable java.util.List<String> lore,
            @Nullable Map<String, Integer> enchantments
    ) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.enchantments = enchantments;
    }

    public @NotNull String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable java.util.List<String> getLore() {
        return lore;
    }

    public @Nullable Map<String, Integer> getEnchantments() {
        return enchantments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KitItem)) return false;
        KitItem that = (KitItem) obj;
        return amount == that.amount &&
                type.equals(that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(lore, that.lore) &&
                Objects.equals(enchantments, that.enchantments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, name, lore, enchantments);
    }

    @Override
    public String toString() {
        return "KitItem{" +
                "type='" + type + '\'' +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", lore=" + lore +
                ", enchantments=" + enchantments +
                '}';
    }
}