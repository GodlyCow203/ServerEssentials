package serveressentials.serveressentials.gui;

import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.pw.PlayerWarp;
import serveressentials.serveressentials.pw.WarpStorage;
import serveressentials.serveressentials.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class WarpListGUI {

    private final WarpStorage storage;
    private final GUIConfig guiConfig;

    public WarpListGUI(WarpStorage storage, GUIConfig guiConfig) {
        this.storage = storage;
        this.guiConfig = guiConfig;
    }


    public void open(Player player, String category) {
        List<PlayerWarp> warps = storage.getWarpsInCategory(category);

        int size = Math.max(27, ((warps.size() / 9) + 1) * 9);

        // ✅ Convert GUI title with hex colors
        Inventory gui = Bukkit.createInventory(null, size, ChatUtil.component(guiConfig.getWarpListTitle(category)));

        for (PlayerWarp warp : warps) {
            gui.addItem(guiConfig.getWarpListItem(warp));
        }

        player.openInventory(gui);
    }
}
