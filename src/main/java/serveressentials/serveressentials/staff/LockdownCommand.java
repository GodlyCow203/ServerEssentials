package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LockdownCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final MessagesManager messagesManager;
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

    private boolean lockdownActive = false;
    private boolean awaitingConfirmation = false;
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final String originalMotd;

    public LockdownCommand(JavaPlugin plugin, MessagesManager messagesManager, String originalMotd) {
        this.plugin = plugin;
        this.messagesManager = messagesManager;
        this.originalMotd = originalMotd;

        // Add default messages
        messagesManager.addDefault("Lockdown.no-permission", "<red>You don't have permission to use this command.");
        messagesManager.addDefault("Lockdown.not-active", "<yellow>Lockdown is not active.");
        messagesManager.addDefault("Lockdown.trigger-confirm", "<red>Are you sure you want to trigger LOCKDOWN? Type '/lockdown confirm' to proceed.");
        messagesManager.addDefault("Lockdown.triggered", "<dark_red><bold>!!! LOCKDOWN IS NOW ACTIVE !!!");
        messagesManager.addDefault("Lockdown.lockdown-enabled-op", "<red><bold>⚠ LOCKDOWN ENABLED ⚠");
        messagesManager.addDefault("Lockdown.disabled", "<green>Lockdown has been disabled.");
        messagesManager.addDefault("Lockdown.usage", "<yellow>Usage: /lockdown confirm or /lockdown disable");
        messagesManager.addDefault("Lockdown.kick-message", "<red>LOCKDOWN is active. Come back later.");
        messagesManager.addDefault("Lockdown.motd-lockdown", "<red><bold>⚠ LOCKDOWN MODE ⚠\n<yellow>Server access restricted");
    }

    public boolean isLockdownActive() {
        return lockdownActive;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission("serveressentials.lockdown")) {
            sender.sendMessage(plain.serialize(messagesManager.get("staff.yml", "Lockdown.no-permission")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
            if (!lockdownActive) {
                player.sendMessage(plain.serialize(messagesManager.get("staff.yml", "Lockdown.not-active")));
                return true;
            }
            disableLockdown();
            Bukkit.broadcast(messagesManager.get("staff.yml", "Lockdown.disabled")); // fixed
            return true;
        }


        if (!awaitingConfirmation) {
            awaitingConfirmation = true;
            player.sendMessage(plain.serialize(messagesManager.get("staff.yml", "Lockdown.trigger-confirm")));
            Bukkit.getScheduler().runTaskLater(plugin, () -> awaitingConfirmation = false, 20 * 10);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            awaitingConfirmation = false;
            triggerLockdown();
            return true;
        }

        player.sendMessage(plain.serialize(messagesManager.get("staff.yml", "Lockdown.usage")));
        return true;
    }

    private void triggerLockdown() {
        lockdownActive = true;
        Bukkit.setMotd(plain.serialize(messagesManager.get("staff.yml", "Lockdown.motd-lockdown")));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.isOp()) {
                frozenPlayers.add(p.getUniqueId());
                p.sendTitle("§4LOCKDOWN", "§cYou are being removed...", 10, 60, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (p.isOnline()) {
                        p.kick(messagesManager.get("staff.yml", "Lockdown.kick-message")); // pass Component
                    }
                }, 60L);
            } else {
                p.sendMessage(messagesManager.get("staff.yml", "Lockdown.lockdown-enabled-op"));
            }
        }

        Bukkit.broadcast(messagesManager.get("staff.yml", "Lockdown.triggered")); // pass Component
    }


    private void disableLockdown() {
        lockdownActive = false;
        frozenPlayers.clear();
        Bukkit.setMotd(originalMotd);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (lockdownActive && !event.getPlayer().isOp()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    plain.serialize(messagesManager.get("staff.yml", "Lockdown.kick-message")));
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (lockdownActive) {
            event.setMotd(plain.serialize(messagesManager.get("staff.yml", "Lockdown.motd-lockdown")));
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
