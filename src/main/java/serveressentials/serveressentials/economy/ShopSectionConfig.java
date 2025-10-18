package serveressentials.serveressentials.economy;

import org.bukkit.Material;
import java.util.*;

public class ShopSectionConfig {
    public String title;
    public int size;
    public int pages = 1;
    public int playerHeadSlot = -1;
    public int closeButtonSlot = -1;

    public Map<Integer, ShopItem> layout = new HashMap<>();
    public Map<String, ShopItem> items = new HashMap<>();


    public static class ShopItem {
        public Material material;
        public int amount;
        public String name;
        public List<String> lore;
        public double buyPrice;
        public double sellPrice;
        public boolean clickable = true;
        public String customItemId = null;
        public int slot;
        public int page = 1;
    }
}