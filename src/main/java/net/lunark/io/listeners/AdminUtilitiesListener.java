package net.lunark.io.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import net.lunark.io.staff.AdminUtilitiesCommand;

public class AdminUtilitiesListener implements Listener {

    private final AdminUtilitiesCommand adminUtilities;

    public AdminUtilitiesListener(AdminUtilitiesCommand adminUtilities) {
        this.adminUtilities = adminUtilities;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (adminUtilities.isGodMode(player)) {
            event.setCancelled(true);
        }
    }
}
