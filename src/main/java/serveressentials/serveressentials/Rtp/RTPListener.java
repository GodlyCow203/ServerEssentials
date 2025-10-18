package serveressentials.serveressentials.Rtp;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import serveressentials.serveressentials.Managers.BackManager;
import serveressentials.serveressentials.Managers.CooldownManager;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.config.RTPConfig;
import serveressentials.serveressentials.util.RTPMessages;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RTPListener implements Listener {

    private final ServerEssentials plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RTPListener(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        RTPMessages messages = new RTPMessages(plugin);
        String expected = MiniMessage.miniMessage().serialize(messages.get("gui.title"));
        String actual = MiniMessage.miniMessage().serialize(event.getView().title());

        if (!expected.equals(actual)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        World world = switch (clicked.getType()) {
            case GRASS_BLOCK -> Bukkit.getWorld("world");
            case NETHERRACK -> Bukkit.getWorld("world_nether");
            case END_STONE -> Bukkit.getWorld("world_the_end");
            default -> null;
        };

        if (world == null) {
            player.sendMessage(messages.get("world-not-loaded"));
            return;
        }

        if (!RTPConfig.isEnabled(world.getName())) {
            player.sendMessage(messages.get("world-disabled"));
            return;
        }

        if (CooldownManager.isOnCooldown(player.getUniqueId())) {
            long remaining = CooldownManager.getRemaining(player.getUniqueId());
            player.sendMessage(messages.get("cooldown", "{seconds}", String.valueOf(remaining)));
            return;
        }

        CooldownManager.setCooldown(player.getUniqueId(), RTPConfig.getCooldown(world.getName()));

        int min = RTPConfig.getMinRadius(world.getName());
        int max = RTPConfig.getMaxRadius(world.getName());
        Random random = new Random();

        int x = random.nextInt(max - min + 1) + min;
        int z = random.nextInt(max - min + 1) + min;
        if (random.nextBoolean()) x = -x;
        if (random.nextBoolean()) z = -z;

        int y = (world.getEnvironment() == World.Environment.NETHER) ? 64 : world.getHighestBlockYAt(x, z) + 1;
        Location loc = new Location(world, x + 0.5, y, z + 0.5);

        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
        player.closeInventory();

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 100, 1, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(loc);
            player.sendMessage(messages.get("teleport-success", "{world}", world.getName()));
            world.spawnParticle(Particle.PORTAL, loc, 100, 1, 1, 1);
            world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }, 40L);

        File rtpLogFile = new File(plugin.getDataFolder(), "rtplocations.yml");
        YamlConfiguration rtpLog = YamlConfiguration.loadConfiguration(rtpLogFile);
        rtpLog.set(player.getName(), world.getName() + ": " + x + " " + y + " " + z);
        try {
            rtpLog.save(rtpLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        BackManager.setLastLocation(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }
}
