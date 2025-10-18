package serveressentials.serveressentials.economy;

import org.bukkit.Material;
import java.util.*;

public class MainShopConfig {
    public String title;
    public int size;
    public Map<Integer, ShopDecoration> layout = new HashMap<>();
    public Map<Integer, ShopSectionButton> sectionButtons = new HashMap<>();

    public static class ShopDecoration {
        public Material material;
        public String name;
        public boolean clickable;
    }

    public static class ShopSectionButton {
        public Material material;
        public String name;
        public List<String> lore;
        public String file;
    }
}
