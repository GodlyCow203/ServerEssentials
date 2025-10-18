package serveressentials.serveressentials.gui;

import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.pw.PlayerWarp;
import serveressentials.serveressentials.pw.WarpStorage;
import serveressentials.serveressentials.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EditWarpGUI {

    private final WarpStorage storage;
    private final GUIConfig guiConfig;

    public EditWarpGUI(WarpStorage storage, GUIConfig guiConfig) {
        this.storage = storage;
        this.guiConfig = guiConfig;
    }

    public void open(Player player, PlayerWarp warp) {
        // ✅ Use component-based title with hex color support
        Inventory gui = Bukkit.createInventory(null, 27, ChatUtil.component(guiConfig.getEditWarpTitle(warp.getName())));

        gui.setItem(10, guiConfig.getEditWarpItem("name", warp.getName()));
        gui.setItem(11, guiConfig.getEditWarpItem("description", warp.getDescription() != null ? warp.getDescription() : ""));
        gui.setItem(12, guiConfig.getEditWarpItem("category", warp.getCategory()));
        gui.setItem(13, guiConfig.getEditWarpItem("icon", warp.getIcon().name()));
        gui.setItem(14, guiConfig.getEditWarpItem("location", ""));
        gui.setItem(15, guiConfig.getEditWarpItem("cooldown", String.valueOf(warp.getCooldownSeconds())));

        player.openInventory(gui);
    }
}