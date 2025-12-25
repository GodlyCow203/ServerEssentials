package net.godlycow.org.daily.reward;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class DailyReward {
    public final int day;
    public final int slot;
    public final int page;
    public final List<RewardItem> items;

    public DailyReward(int day, int slot, int page, List<RewardItem> items) {
        this.day = day;
        this.slot = slot;
        this.page = page;
        this.items = items;
    }

    public static class RewardItem {
        public final Material material;
        public final int amount;
        public final String name;
        public final List<String> lore;
        public final Map<String, Integer> enchantments;
        public final boolean glow;
        public final Map<String, String> nbt;

        public RewardItem(Material material, int amount, String name, List<String> lore,
                          Map<String, Integer> enchantments, boolean glow, Map<String, String> nbt) {
            this.material = material;
            this.amount = amount;
            this.name = name;
            this.lore = lore;
            this.enchantments = enchantments;
            this.glow = glow;
            this.nbt = nbt;
        }
    }
}