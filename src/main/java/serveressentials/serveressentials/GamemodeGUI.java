package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GamemodeGUI implements Listener {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static void openGamemodeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, translateHexColor("<#00FFFF>Gamemode Selector"));

        gui.setItem(1, createItem(Material.GRASS_BLOCK, "<#00AA00>Survival", "<#AAAAAA>Click to switch to Survival mode"));
        gui.setItem(3, createItem(Material.BRICKS, "<#FFAA00>Creative", "<#AAAAAA>Click to switch to Creative mode"));
        gui.setItem(5, createItem(Material.MAP, "<#AA00AA>Adventure", "<#AAAAAA>Click to switch to Adventure mode"));
        gui.setItem(7, createItem(Material.ENDER_EYE, "<#5555FF>Spectator", "<#AAAAAA>Click to switch to Spectator mode"));

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateHexColor(name));
            meta.setLore(Collections.singletonList(translateHexColor(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String translateHexColor(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = toLegacyHex(hex);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String toLegacyHex(String hex) {
        char[] chars = hex.toCharArray();
        return "§x" +
                "§" + chars[0] +
                "§" + chars[1] +
                "§" + chars[2] +
                "§" + chars[3] +
                "§" + chars[4] +
                "§" + chars[5];
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(translateHexColor("<#00FFFF>Gamemode Selector"))) {
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;

            Player player = (Player) e.getWhoClicked();

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            switch (e.getCurrentItem().getType()) {
                case GRASS_BLOCK -> player.setGameMode(GameMode.SURVIVAL);
                case BRICKS -> player.setGameMode(GameMode.CREATIVE);
                case MAP -> player.setGameMode(GameMode.ADVENTURE);
                case ENDER_EYE -> player.setGameMode(GameMode.SPECTATOR);
                default -> {
                    return;
                }
            }

            player.closeInventory();
            player.sendMessage(translateHexColor("<#00FF00>Gamemode changed to <#FFFF00>" + player.getGameMode().name()));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
}
