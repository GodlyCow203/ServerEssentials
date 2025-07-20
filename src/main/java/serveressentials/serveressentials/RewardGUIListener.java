package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RewardGUIListener implements Listener {

    private final ServerEssentials plugin;

    public RewardGUIListener(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Playtime Rewards")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

    }
}
