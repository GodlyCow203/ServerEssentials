package net.lunark.io.economy;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSectionConfig {
    public String title;
    public int size;
    public int pages = 1;
    public int playerHeadSlot = -1;
    public int closeButtonSlot = -1;
    public Map<Integer, LayoutItem> layout = new HashMap<>();
    public Map<String, ShopItem> items = new HashMap<>();

    public static class LayoutItem {
        public Material material;
        public String name;
        public List<String> lore;
        public boolean clickable = false;
    }

    public static class ShopItem {
        public Material material;
        public int amount = 1;
        public String name;
        public List<String> lore;
        public double buyPrice = -1;
        public double sellPrice = -1;
        public boolean clickable = true;
        public String customItemId;
        public int slot;
        public int page = 1;
    }
}