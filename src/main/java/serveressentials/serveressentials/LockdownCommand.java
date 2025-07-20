package serveressentials.serveressentials;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LockdownCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private boolean lockdownActive = false;
    private boolean awaitingConfirmation = false;
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private String originalMotd;

    public LockdownCommand(JavaPlugin plugin, String originalMotd) {
        this.plugin = plugin;
        this.originalMotd = originalMotd;
    }

    public boolean isLockdownActive() {
        return lockdownActive;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
            if (!lockdownActive) {
                player.sendMessage(ChatColor.YELLOW + "Lockdown is not active.");
                return true;
            }
            disableLockdown();
            Bukkit.broadcastMessage(ChatColor.GREEN + "Lockdown has been disabled.");
            return true;
        }

        if (!awaitingConfirmation) {
            awaitingConfirmation = true;
            player.sendMessage(ChatColor.RED + "Are you sure you want to trigger LOCKDOWN?");
            player.sendMessage(ChatColor.RED + "Type '/lockdown confirm' to proceed.");
            Bukkit.getScheduler().runTaskLater(plugin, () -> awaitingConfirmation = false, 20 * 10);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            awaitingConfirmation = false;
            triggerLockdown();
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /lockdown confirm or /lockdown disable");
        return true;
    }

    private void triggerLockdown() {
        lockdownActive = true;
        Bukkit.setMotd("§c§l⚠ LOCKDOWN MODE ⚠\n§eServer access restricted");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp()) {
                frozenPlayers.add(p.getUniqueId());

                p.sendTitle("§4LOCKDOWN", "§cYou are being removed...", 10, 60, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (p.isOnline()) {
                        p.kickPlayer("§cLOCKDOWN is active. Come back later.");
                    }
                }, 60L); // 3 seconds
            } else {
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "⚠ LOCKDOWN ENABLED ⚠");
            }
        }

        Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!! LOCKDOWN IS NOW ACTIVE !!!");
    }

    private void disableLockdown() {
        lockdownActive = false;
        frozenPlayers.clear();
        Bukkit.setMotd(originalMotd);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (lockdownActive && !event.getPlayer().isOp()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "LOCKDOWN in effect. Try again later.");
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (lockdownActive) {
            event.setMotd("§c§l⚠ LOCKDOWN MODE ⚠\n§eServer access restricted");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (lockdownActive && frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (lockdownActive && frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (lockdownActive && frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p && frozenPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player p && frozenPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
