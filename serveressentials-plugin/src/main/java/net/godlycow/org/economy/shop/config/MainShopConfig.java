package net.godlycow.org.economy.shop.config;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainShopConfig {
    public String title;
    public int size;
    public Map<Integer, LayoutItem> layout = new HashMap<>();
    public Map<Integer, SectionButton> sectionButtons = new HashMap<>();

    public static class LayoutItem {
        public Material material;
        public String name;
        public boolean clickable = false;
    }

    public static class SectionButton {
        public Material material;
        public String name;
        public List<String> lore;
        public String file;
    }
}