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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RtpListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RtpLocationStorage locationStorage;
    private final CooldownManager cooldownManager;
    private final BackManager backManager;
    private final RtpConfig config;
    private final ConcurrentHashMap<UUID, Long> activeRtpSessions = new ConcurrentHashMap<>();

    private static final Set<Material> UNSAFE_MATERIALS = new HashSet<>();
    private static final Set<Material> NETHER_SAFE_GROUND = new HashSet<>();
    private final ConcurrentHashMap<String, World> worldCache = new ConcurrentHashMap<>();

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
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory gui = event.getClickedInventory();
        if (gui == null) return;

        Component expectedTitle = langManager.getMessageFor(player, "commands.rtp.gui.title", "RTP Menu");
        if (!player.getOpenInventory().title().equals(expectedTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        World world = getTargetWorld(clicked.getType());
        if (world == null) return;

        if (!config.isWorldEnabled(world.getName())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rtp.world_disabled",
                    "RTP is disabled in this world!"));
            return;
        }

        UUID playerId = player.getUniqueId();

        if (activeRtpSessions.putIfAbsent(playerId, System.currentTimeMillis()) != null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rtp.already_pending",
                    "<red>You already have a pending RTP request!"));
            return;
        }

        if (cooldownManager.isOnCooldown(playerId)) {
            activeRtpSessions.remove(playerId);
            long remain = cooldownManager.getRemaining(playerId);
            player.sendMessage(langManager.getMessageFor(player, "commands.rtp.cooldown",
                    "<red>Please wait <yellow>{seconds}s</yellow> before using RTP again.",
                    LanguageManager.ComponentPlaceholder.of("{seconds}", remain)));
            return;
        }

        cooldownManager.setCooldown(playerId, config.getCooldown(world.getName()));
        player.closeInventory();

        CompletableFuture.runAsync(() -> {
            startRtpProcess(player, world)
                    .whenComplete((result, error) -> {
                        activeRtpSessions.remove(playerId);
                        if (error != null) {
                            plugin.getLogger().warning("RTP failed for " + player.getName() + ": " + error.getMessage());
                        }
                    });
        });
    }

    private CompletableFuture<Void> startRtpProcess(Player player, World world) {
        return CompletableFuture.runAsync(() -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.rtp.searching",
                    "<yellow>Searching for a safe location..."));

            java.util.Random random = new java.util.Random();
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

                Location result;
                if (world.getEnvironment() == World.Environment.NETHER) {
                    result = findSafeNetherLocation(world, x, z, random);
                } else {
                    result = findSafeOverworldLocation(world, x, z);
                }

                if (result != null) {
                    completeRtpTeleport(player, result);
                    return;
                }

                try { Thread.sleep(10L); } catch (InterruptedException ignore) {}
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.rtp.unsafe_location",
                        "<red>Could not find a safe location to teleport to!"));
            });
        });
    }

    private Location findSafeOverworldLocation(World world, int x, int z) {
        try {
            CompletableFuture<Location> future = new CompletableFuture<>();

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    world.loadChunk(x >> 4, z >> 4, true);
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    Location test = new Location(world, x + 0.5, y, z + 0.5);
                    future.complete(isLocationSafeSync(test) ? test : null);
                } catch (Exception e) {
                    future.complete(null);
                }
            });

            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    private Location findSafeNetherLocation(World world, int x, int z, java.util.Random random) {
        for (int i = 0; i < 15; i++) {
            int y = i == 0 ? random.nextInt(90) + 30 : 30 + (i * 6);
            if (y > 120) y = 120 - (i % 15);

            try {
                CompletableFuture<Location> future = new CompletableFuture<>();

                final int finalY = y;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        world.loadChunk(x >> 4, z >> 4, true);
                        Location test = new Location(world, x + 0.5, finalY, z + 0.5);
                        future.complete(isNetherLocationSafeSync(test) ? test : null);
                    } catch (Exception e) {
                        future.complete(null);
                    }
                });

                Location result = future.get();
                if (result != null) return result;
            } catch (Exception e) {
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
        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2) return false;
        if (!w.isChunkLoaded(x >> 4, z >> 4)) return false;

        try {
            Block feet = w.getBlockAt(x, y, z);
            Block head = w.getBlockAt(x, y + 1, z);
            Block ground = w.getBlockAt(x, y - 1, z);

            if (!feet.getType().isAir()) return false;
            if (!head.getType().isAir()) return false;

            Material g = ground.getType();
            return g.isSolid() && !UNSAFE_MATERIALS.contains(g);

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

        if (w == null || w.getEnvironment() != World.Environment.NETHER) return false;
        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2) return false;
        if (!w.isChunkLoaded(x >> 4, z >> 4)) return false;

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
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                if (backManager != null) {
                    backManager.setLastLocation(player.getUniqueId(), player.getLocation());
                }

                player.teleport(loc);

                player.sendMessage(langManager.getMessageFor(player, "commands.rtp.teleport_success",
                        "<green>Teleported to <white>{world}</white>!",
                        LanguageManager.ComponentPlaceholder.of("{world}", loc.getWorld().getName())));

                locationStorage.saveRtpLocation(player.getUniqueId(), player.getName(), loc)
                        .exceptionally(ex -> {
                            plugin.getLogger().warning("Failed to save RTP location: " + ex.getMessage());
                            return null;
                        });

            } catch (Exception e) {
                plugin.getLogger().severe("Teleportation failed: " + e.getMessage());
                player.sendMessage(langManager.getMessageFor(player, "commands.rtp.teleport_failed",
                        "<red>Teleport failed. Please try again."));
            }
        });
    }

    private World getTargetWorld(Material material) {
        switch (material) {
            case GRASS_BLOCK: return getWorld("world");
            case NETHERRACK: return getWorld("world_nether");
            case END_STONE: return getWorld("world_the_end");
            default: return null;
        }
    }

    private World getWorld(String name) {
        return worldCache.computeIfAbsent(name, Bukkit::getWorld);
    }
}