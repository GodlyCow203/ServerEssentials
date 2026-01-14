package net.godlycow.org.rtp.api;

import com.serveressentials.api.rtp.RtpAPI;
import com.serveressentials.api.rtp.RtpLocation;
import com.serveressentials.api.rtp.RtpWorldConfig;
import com.serveressentials.api.rtp.event.RtpTeleportEvent;
import com.serveressentials.api.rtp.event.RtpLocationSaveEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.rtp.RtpConfig;
import net.godlycow.org.rtp.storage.RtpLocationStorage;
import net.godlycow.org.rtp.trigger.RtpListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class RtpAPIImpl implements RtpAPI {
    private final ServerEssentials plugin;
    private final RtpLocationStorage rtpLocationStorage;
    private final RtpConfig rtpConfig;
    private final RtpListener rtpListener;
    private final PlayerLanguageManager langManager;
    private final ConcurrentHashMap<String, World> worldCache = new ConcurrentHashMap<>();

    public RtpAPIImpl(ServerEssentials plugin,
                      RtpLocationStorage rtpLocationStorage,
                      RtpConfig rtpConfig,
                      RtpListener rtpListener,
                      PlayerLanguageManager langManager) {
        this.plugin = plugin;
        this.rtpLocationStorage = rtpLocationStorage;
        this.rtpConfig = rtpConfig;
        this.rtpListener = rtpListener;
        this.langManager = langManager;
    }

    @Override
    public CompletableFuture<Boolean> randomTeleport(Player player) {
        return randomTeleport(player, player.getWorld());
    }

    @Override
    public CompletableFuture<Boolean> randomTeleport(Player player, World world) {
        if (!player.hasPermission("serveressentials.command.rtp")) {
            return CompletableFuture.completedFuture(false);
        }

        return isRtpEnabled(world.getName())
                .thenCompose(enabled -> {
                    if (!enabled) {
                        return CompletableFuture.completedFuture(false);
                    }

                    return getRemainingCooldown(player.getUniqueId())
                            .thenCompose(remaining -> {
                                if (remaining > 0) {
                                    return CompletableFuture.completedFuture(false);
                                }

                                int minRadius = rtpConfig.getMinRadius(world.getName());
                                int maxRadius = rtpConfig.getMaxRadius(world.getName());

                                return findSafeLocation(player, world, minRadius, maxRadius)
                                        .thenCompose(safeLocation -> {
                                            if (safeLocation == null) {
                                                CompletableFuture.runAsync(() ->
                                                        player.sendMessage(langManager.getMessageFor(
                                                                player,
                                                                "commands.rtp.unsafe_location",
                                                                "<red>Could not find a safe location to teleport to!"
                                                        ))
                                                );
                                                return CompletableFuture.completedFuture(false);
                                            }

                                            return completeTeleport(player, safeLocation, world)
                                                    .thenApply(v -> true);
                                        });
                            });
                });
    }


    @Override
    public CompletableFuture<Boolean> openRtpGUI(Player player) {
        if (!player.hasPermission("serveressentials.command.rtp")) {
            return CompletableFuture.completedFuture(false);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Component title = langManager.getMessageFor(player, "commands.rtp.gui.title", "RTP Menu");
            org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 9, title);

            gui.setItem(2, createGuiItem(player, Material.GRASS_BLOCK, "overworld"));
            gui.setItem(4, createGuiItem(player, Material.NETHERRACK, "nether"));
            gui.setItem(6, createGuiItem(player, Material.END_STONE, "end"));

            player.openInventory(gui);
        });

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Optional<RtpLocation>> getLastRtpLocation(UUID playerId) {
        return rtpLocationStorage.getRtpLocation(playerId)
                .thenApply(opt -> opt.map(loc -> new RtpLocation(
                        playerId,
                        "Unknown",
                        loc.getWorld().getName(),
                        loc.getX(),
                        loc.getY(),
                        loc.getZ(),
                        System.currentTimeMillis()
                )));
    }

    @Override
    public CompletableFuture<Void> saveRtpLocation(UUID playerId, Location location) {
        Player player = plugin.getServer().getPlayer(playerId);
        String playerName = player != null ? player.getName() : "Unknown";

        return rtpLocationStorage.saveRtpLocation(playerId, playerName, location)
                .thenRun(() -> {
                    if (player != null && player.isOnline()) {
                        RtpLocation rtpLocation = new RtpLocation(
                                playerId,
                                player.getName(),
                                location.getWorld().getName(),
                                location.getX(),
                                location.getY(),
                                location.getZ(),
                                System.currentTimeMillis()
                        );
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(new RtpLocationSaveEvent(player, rtpLocation));
                        });
                    }
                });
    }

    @Override
    public CompletableFuture<Long> getRemainingCooldown(UUID playerId) {
        return CompletableFuture.completedFuture(0L);
    }

    @Override
    public CompletableFuture<Optional<RtpWorldConfig>> getWorldConfig(String worldName) {
        return CompletableFuture.supplyAsync(() -> {
            boolean enabled = rtpConfig.isWorldEnabled(worldName);
            int minRadius = rtpConfig.getMinRadius(worldName);
            int maxRadius = rtpConfig.getMaxRadius(worldName);
            int cooldown = rtpConfig.getCooldown(worldName);

            if (!enabled && minRadius == 0 && maxRadius == 0 && cooldown == 0) {
                return Optional.empty();
            }

            return Optional.of(new RtpWorldConfig(worldName, enabled, minRadius, maxRadius, cooldown));
        });
    }

    @Override
    public CompletableFuture<Boolean> isRtpEnabled(String worldName) {
        return CompletableFuture.supplyAsync(() -> rtpConfig.isWorldEnabled(worldName));
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("rtp.enabled", true);
    }

    @Override
    public CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            rtpConfig.reload();
            worldCache.clear();
            plugin.getLogger().info("[ServerEssentials] RTP configuration reloaded");
        });
    }

    private CompletableFuture<Location> findSafeLocation(Player player, World world,
                                                         int minRadius, int maxRadius) {
        return CompletableFuture.supplyAsync(() -> {
            java.util.Random random = new java.util.Random();
            int maxAttempts = world.getEnvironment() == World.Environment.NETHER ? 50 : 25;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int x = random.nextInt(maxRadius - minRadius + 1) + minRadius;
                int z = random.nextInt(maxRadius - minRadius + 1) + minRadius;
                if (random.nextBoolean()) x = -x;
                if (random.nextBoolean()) z = -z;

                if (world.getEnvironment() == World.Environment.NETHER) {
                    Location result = findSafeNetherLocation(world, x, z, random);
                    if (result != null) return result;
                } else {
                    Location result = findSafeOverworldLocation(world, x, z);
                    if (result != null) return result;
                }
            }
            return null;
        });
    }

    private Location findSafeOverworldLocation(World world, int x, int z) {
        return CompletableFuture.supplyAsync(() -> {
            world.loadChunk(x >> 4, z >> 4, true);
            int y = world.getHighestBlockYAt(x, z) + 1;
            Location test = new Location(world, x + 0.5, y, z + 0.5);
            return isLocationSafeSync(test) ? test : null;
        }).join();
    }

    private Location findSafeNetherLocation(World world, int x, int z, java.util.Random random) {
        for (int i = 0; i < 15; i++) {
            int y = i == 0 ? random.nextInt(90) + 30 : 30 + (i * 6);
            if (y > 120) y = 120 - (i % 15);

            Location test = new Location(world, x + 0.5, y, z + 0.5);
            if (isNetherLocationSafeSync(test)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    world.loadChunk(x >> 4, z >> 4, true);
                });
                return test;
            }
        }
        return null;
    }

    private boolean isLocationSafeSync(Location loc) {
        World w = loc.getWorld();
        if (w == null) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2) return false;
        if (!w.isChunkLoaded(x >> 4, z >> 4)) return false;

        try {
            org.bukkit.block.Block feet = w.getBlockAt(x, y, z);
            org.bukkit.block.Block head = w.getBlockAt(x, y + 1, z);
            org.bukkit.block.Block ground = w.getBlockAt(x, y - 1, z);

            if (!feet.getType().isAir()) return false;
            if (!head.getType().isAir()) return false;

            return ground.getType().isSolid();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNetherLocationSafeSync(Location loc) {
        World w = loc.getWorld();
        if (w == null || w.getEnvironment() != World.Environment.NETHER) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (y < w.getMinHeight() + 1 || y > w.getMaxHeight() - 2) return false;
        if (!w.isChunkLoaded(x >> 4, z >> 4)) return false;

        try {
            org.bukkit.block.Block feet = w.getBlockAt(x, y, z);
            org.bukkit.block.Block head = w.getBlockAt(x, y + 1, z);
            org.bukkit.block.Block ground = w.getBlockAt(x, y - 1, z);

            if (!feet.getType().isAir()) return false;
            if (!head.getType().isAir()) return false;

            Material g = ground.getType();
            return g.isSolid() && !isUnsafeNetherMaterial(g);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUnsafeNetherMaterial(Material mat) {
        return mat == Material.LAVA || mat == Material.MAGMA_BLOCK || mat == Material.FIRE;
    }

    private CompletableFuture<Void> completeTeleport(Player player, Location safeLocation, World world) {
        return CompletableFuture.runAsync(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location fromLocation = player.getLocation();
                player.teleport(safeLocation);

                saveRtpLocation(player.getUniqueId(), safeLocation);

                RtpLocation rtpLocation = new RtpLocation(
                        player.getUniqueId(),
                        player.getName(),
                        world.getName(),
                        safeLocation.getX(),
                        safeLocation.getY(),
                        safeLocation.getZ(),
                        System.currentTimeMillis()
                );

                Bukkit.getPluginManager().callEvent(new RtpTeleportEvent(player, rtpLocation, fromLocation, world.getName()));

                player.sendMessage(langManager.getMessageFor(player, "commands.rtp.teleport_success",
                        "<green>Teleported to <white>{world}</white>!",
                        LanguageManager.ComponentPlaceholder.of("{world}", world.getName())));
            });
        });
    }

    private ItemStack createGuiItem(Player player, Material material, String worldKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(langManager.getMessageFor(player, "rtp.gui." + worldKey + ".name", worldKey));
        meta.lore(langManager.getMessageList(player, "rtp.gui." + worldKey + ".lore"));
        item.setItemMeta(meta);
        return item;
    }
}