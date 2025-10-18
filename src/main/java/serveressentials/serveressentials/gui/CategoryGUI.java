package serveressentials.serveressentials.gui;

import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.pw.WarpStorage;
import serveressentials.serveressentials.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class CategoryGUI {

    private final WarpStorage storage;
    private final GUIConfig guiConfig;

    public CategoryGUI(WarpStorage storage, GUIConfig guiConfig) {
        this.storage = storage;
        this.guiConfig = guiConfig;
    }

    public void open(Player player) {
        List<String> categories = guiConfig.getCategories();

        int size = Math.max(27, ((categories.size() / 9) + 1) * 9);

        // ✅ Use ChatUtil.component to support hex colors
        Inventory gui = Bukkit.createInventory(null, size, ChatUtil.component(guiConfig.getCategoryGUITitle()));

        for (String category : categories) {
            gui.addItem(guiConfig.getCategoryItem(category));
        }

        player.openInventory(gui);
    }
}
