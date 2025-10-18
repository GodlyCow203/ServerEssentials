package serveressentials.serveressentials.staff;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.HashSet;
import java.util.Set;

public class FreezeCommand implements CommandExecutor, Listener {

    public static final Set<Player> frozenPlayers = new HashSet<>();
    private final MessagesManager messages;

    public FreezeCommand(MessagesManager messages, Plugin plugin) {
        this.messages = messages;
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Add default messages (optional)
        messages.addDefault("freeze.only-players", "<red>Only players can use this command.");
        messages.addDefault("freeze.usage", "<red>Usage: /freeze <player>");
        messages.addDefault("freeze.not-found", "<red>Player not found or not online.");
        messages.addDefault("freeze.froze", "<green>Froze <white>{player}</white>.");
        messages.addDefault("freeze.unfroze", "<yellow>Unfroze <white>{player}</white>.");
        messages.addDefault("freeze.you-frozen", "<red>You have been frozen!");
        messages.addDefault("freeze.you-unfrozen", "<green>You have been unfrozen.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessageComponent("freeze.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(messages.getMessageComponent("freeze.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(messages.getMessageComponent("freeze.not-found"));
            return true;
        }

        if (frozenPlayers.contains(target)) {
            frozenPlayers.remove(target);
            player.sendMessage(messages.getMessageComponent("freeze.unfroze", "{player}", target.getName()));
            target.sendMessage(messages.getMessageComponent("freeze.you-unfrozen"));
        } else {
            frozenPlayers.add(target);
            player.sendMessage(messages.getMessageComponent("freeze.froze", "{player}", target.getName()));
            target.sendMessage(messages.getMessageComponent("freeze.you-frozen"));
        }

        return true;
    }

    // ---------------- EVENT HANDLERS ----------------

    // Prevent frozen players from executing any command
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (frozenPlayers.contains(event.getPlayer())) {
            // Allow some commands if needed (optional)
            event.getPlayer().sendMessage(messages.getMessageComponent("freeze.you-frozen"));
            event.setCancelled(true);
        }
    }

    // Prevent frozen players from moving except using allowed items (like Ender Pearls)
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player)) {
            // Allow movement if player is teleporting via Ender Pearl
            if (player.isGliding() || player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL) {
                return;
            }

            // Otherwise freeze position
            event.setCancelled(true);
        }
    }

    // Optional: prevent interaction with blocks/items
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player)) {
            Material mat = event.getItem() != null ? event.getItem().getType() : null;
            // Allow Ender Pearls, block placement/use blocked
            if (mat == Material.ENDER_PEARL) return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (frozenPlayers.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
