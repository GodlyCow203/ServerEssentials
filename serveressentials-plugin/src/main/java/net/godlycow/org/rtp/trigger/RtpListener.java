package net.godlycow.org.rtp.trigger;

import net.godlycow.org.rtp.RtpConfig;
import net.godlycow.org.rtp.storage.RtpLocationStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.back.BackManager;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.managers.cooldown.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RtpListener implements Listener {

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RtpLocationStorage locationStorage;
    private final CooldownManager cooldownManager;
    private final BackManager backManager;
    private final RtpConfig config;

    private static final Set<Material> UNSAFE_MATERIALS = new HashSet<>();
    private static final Set<Material> NETHER_SAFE_GROUND = new HashSet<>();

    static {
        UNSAFE_MATERIALS.add(Material.LAVA);
        UNSAFE_MATERIALS.add(Material.WATER);

        UNSAFE_MATERIALS.add(Material.FIRE);
        UNSAFE_MATERIALS.add(Material.SOUL_FIRE);
        UNSAFE_MATERIALS.add(Material.MAGMA_BLOCK);
        UNSAFE_MATERIALS.add(Material.CACTUS);
        UNSAFE_MATERIALS.add(Material.SWEET_BERRY_BUSH);

        UNSAFE_MATERIALS.add(Material.VOID_AIR);
        UNSAFE_MATERIALS.add(Material.CAVE_AIR);

        UNSAFE_MATERIALS.add(Material.WITHER_ROSE);
        UNSAFE_MATERIALS.add(Material.POWDER_SNOW);

        NETHER_SAFE_GROUND.add(Material.NETHERRACK);
        NETHER_SAFE_GROUND.add(Material.CRIMSON_NYLIUM);
        NETHER_SAFE_GROUND.add(Material.WARPED_NYLIUM);
        NETHER_SAFE_GROUND.add(Material.SOUL_SOIL);
        NETHER_SAFE_GROUND.add(Material.BASALT);
        NETHER_SAFE_GROUND.add(Material.BLACKSTONE);
        NETHER_SAFE_GROUND.add(Material.NETHER_BRICKS);
        NETHER_SAFE_GROUND.add(Material.NETHERITE_BLOCK);
        NETHER_SAFE_GROUND.add(Material.ANCIENT_DEBRIS);
    }

    public RtpListener(Plugin plugin, PlayerLanguageManager langManager,
                       RtpLocationStorage locationStorage, CooldownManager cooldownManager,
                       BackManager backManager, RtpConfig config) {

        this.plugin = plugin;
        this.langManager = langManager;
        this.locationStorage = locationStorage;
        this.cooldownManager = cooldownManager;
        this.backManager = backManager;
        this.config = config;

        if (backManager == null) {
            plugin.getLogger().warning("BackManager is null in RtpListener constructor!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory gui = event.getClickedInventory();
        if (gui == null) return;

        Component expectedTitle = langManager.getMessageFor(player, "rtp.gui.title", "RTP Menu");
        InventoryView view = player.getOpenInventory();

        if (!view.title().equals(expectedTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        World world = getTargetWorld(clicked.getType());
        if (world == null) return;

        if (!config.isWorldEnabled(world.getName())) {
            player.sendMessage(langManager.getMessageFor(player, "rtp.world_disabled",
                    "RTP is disabled in this world!"));
            return;
        }

        UUID id = player.getUniqueId();

        if (cooldownManager.isOnCooldown(id)) {
            long remain = cooldownManager.getRemaining(id);
            player.sendMessage(langManager.getMessageFor(player, "rtp.cooldown",
                    "<red>Please wait <yellow>{seconds}s</yellow> before using RTP again.",
                    LanguageManager.ComponentPlaceholder.of("{seconds}", remain)));
            return;
        }

        cooldownManager.setCooldown(id, config.getCooldown(world.getName()));
        player.closeInventory();

        startRtpProcess(player, world);
    }

    private void startRtpProcess(Player player, World world) {

        player.sendMessage(langManager.getMessageFor(player, "rtp.searching",
                "<yellow>Searching for a safe location..."));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Random random = new Random();

            int min = config.getMinRadius(world.getName());
            int max = config.getMaxRadius(world.getName());

            if (max < min) {
                int swap = min;
                min = max;
                max = swap;
            }

            int maxAttempts = world.getEnvironment() == World.Environment.NETHER ? 50 : 25;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {

                int x = random.nextInt(max - min + 1) + min;
                int z = random.nextInt(max - min + 1) + min;
                if (random.nextBoolean()) x = -x;
                if (random.nextBoolean()) z = -z;

                final int fx = x;
                final int fz = z;

                CompletableFuture<Location> future = new CompletableFuture<>();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        world.loadChunk(fx >> 4, fz >> 4, true);

                        Location result;
                        if (world.getEnvironment() == World.Environment.NETHER) {
                            result = findSafeNetherLocation(world, fx, fz, random);
                        } else {
                            result = findSafeOverworldLocation(world, fx, fz);
                        }

                        future.complete(result);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error finding safe location: " + e.getMessage());
                        future.complete(null);
                    }
                });

                Location result = future.join();

                if (result != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        completeRtpTeleport(player, result);
                    });
                    return;
                }

                try { Thread.sleep(10L); } catch (InterruptedException ignore) {}
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(langManager.getMessageFor(player, "rtp.unsafe_location",
                        "<red>Could not find a safe location to teleport to!"));
            });
        });
    }

    private Location findSafeOverworldLocation(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location test = new Location(world, x + 0.5, y, z + 0.5);

        if (isLocationSafeSync(test)) {
            return test;
        }
        return null;
    }

    private Location findSafeNetherLocation(World world, int x, int z, Random random) {
        for (int i = 0; i < 15; i++) {
            int y;
            if (i == 0) {
                y = random.nextInt(90) + 30;
            } else {
                y = 30 + (i * 6);
                if (y > 120) y = 120 - (i % 15);
            }

            Location test = new Location(world, x + 0.5, y, z + 0.5);

            if (isNetherLocationSafeSync(test)) {
                return test;
            }
        }
        return null;
    }

    private boolean isLocationSafeSync(Location loc) {
        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (w == null) return false;

        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2)
            return false;

        if (!w.isChunkLoaded(x >> 4, z >> 4))
            return false;

        try {
            Block feet = w.getBlockAt(x, y, z);
            Block head = w.getBlockAt(x, y + 1, z);
            Block ground = w.getBlockAt(x, y - 1, z);

            if (!feet.getType().isAir()) return false;
            if (!head.getType().isAir()) return false;

            Material g = ground.getType();

            if (UNSAFE_MATERIALS.contains(g)) return false;
            if (!g.isSolid()) return false;

            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking block safety: " + e.getMessage());
            return false;
        }
    }


    private boolean isNetherLocationSafeSync(Location loc) {
        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (w == null) return false;

        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2)
            return false;

        if (!w.isChunkLoaded(x >> 4, z >> 4))
            return false;

        try {
            Block feet = w.getBlockAt(x, y, z);
            Block head = w.getBlockAt(x, y + 1, z);
            Block ground = w.getBlockAt(x, y - 1, z);

            if (!feet.getType().isAir()) return false;
            if (!head.getType().isAir()) return false;

            Material g = ground.getType();

            if (UNSAFE_MATERIALS.contains(g)) return false;

            if (!NETHER_SAFE_GROUND.contains(g) && !g.isSolid()) return false;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block nearbyGround = w.getBlockAt(x + dx, y - 1, z + dz);
                    if (nearbyGround.getType() == Material.LAVA) {
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking Nether block safety: " + e.getMessage());
            return false;
        }
    }

    private void completeRtpTeleport(Player player, Location loc) {
        try {
            if (backManager != null) {
                backManager.setLastLocation(player.getUniqueId(), player.getLocation());
            } else {
                plugin.getLogger().warning("BackManager is null - cannot save return location");
            }

            player.teleport(loc);

            player.sendMessage(langManager.getMessageFor(player, "rtp.teleport_success",
                    "<green>Teleported to <white>{world}</white>!",
                    LanguageManager.ComponentPlaceholder.of("{world}", loc.getWorld().getName())));

            locationStorage.saveRtpLocation(player.getUniqueId(), player.getName(), loc)
                    .exceptionally(ex -> {
                        plugin.getLogger().warning("Failed to save RTP location: " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            plugin.getLogger().severe("Teleportation failed: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(langManager.getMessageFor(player, "rtp.teleport_failed",
                    "<red>Teleport failed. Please try again."));
        }
    }

    private World getTargetWorld(Material material) {
        return switch (material) {
            case GRASS_BLOCK -> Bukkit.getWorld("world");
            case NETHERRACK -> Bukkit.getWorld("world_nether");
            case END_STONE -> Bukkit.getWorld("world_the_end");
            default -> null;
        };
    }
}