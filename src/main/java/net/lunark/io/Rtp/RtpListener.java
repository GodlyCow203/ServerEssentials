package net.lunark.io.Rtp;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.Managers.BackManager;
import net.lunark.io.Managers.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;
import java.util.UUID;

public class RtpListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RtpLocationStorage locationStorage;
    private final CooldownManager cooldownManager;
    private final BackManager backManager;
    private final RtpConfig config;

    public RtpListener(Plugin plugin, PlayerLanguageManager langManager,
                       RtpLocationStorage locationStorage, CooldownManager cooldownManager,
                       BackManager backManager, RtpConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.locationStorage = locationStorage;
        this.cooldownManager = cooldownManager;
        this.backManager = backManager;
        this.config = config;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory gui = event.getClickedInventory();
        if (gui == null) return;

        // Check if this is our RTP GUI
        Component expectedTitle = langManager.getMessageFor(player, "rtp.gui.title", "RTP Menu");

        InventoryView view = player.getOpenInventory();

        if (!view.title().equals(expectedTitle)) return;


        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Determine world
        World world = getTargetWorld(clicked.getType());
        if (world == null) return;

        // Check if RTP is enabled for this world
        if (!config.isWorldEnabled(world.getName())) {
            player.sendMessage(langManager.getMessageFor(player, "rtp.world_disabled",
                    "RTP is disabled in this world!"));
            return;
        }

        // Check cooldown
        UUID playerId = player.getUniqueId();
        if (cooldownManager.isOnCooldown(playerId)) {
            long remaining = cooldownManager.getRemaining(playerId);
            player.sendMessage(langManager.getMessageFor(player, "rtp.cooldown",
                    "<red>Please wait <yellow>{seconds}s</yellow> before using RTP again",
                    LanguageManager.ComponentPlaceholder.of("{seconds}", remaining)));
            return;
        }

        // Set cooldown
        cooldownManager.setCooldown(playerId, config.getCooldown(world.getName()));

        // Generate location
        Location rtpLocation = generateRtpLocation(world);

        // Save back location and teleport
        backManager.setLastLocation(playerId, player.getLocation());
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.teleport(rtpLocation);
            player.sendMessage(langManager.getMessageFor(player, "rtp.teleport_success",
                    "<green>Teleported to <white>{world}</white>!",
                    LanguageManager.ComponentPlaceholder.of("{world}", world.getName())));
        }, 20L);

        // Save to database
        locationStorage.saveRtpLocation(playerId, player.getName(), rtpLocation);
    }

    private World getTargetWorld(Material material) {
        return switch (material) {
            case GRASS_BLOCK -> Bukkit.getWorld("world");
            case NETHERRACK -> Bukkit.getWorld("world_nether");
            case END_STONE -> Bukkit.getWorld("world_the_end");
            default -> null;
        };
    }

    private Location generateRtpLocation(World world) {
        Random random = new Random();
        int min = config.getMinRadius(world.getName());
        int max = config.getMaxRadius(world.getName());

        int x = random.nextInt(max - min + 1) + min;
        int z = random.nextInt(max - min + 1) + min;
        if (random.nextBoolean()) x = -x;
        if (random.nextBoolean()) z = -z;

        int y = (world.getEnvironment() == World.Environment.NETHER)
                ? 64
                : world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x + 0.5, y, z + 0.5);
    }
}