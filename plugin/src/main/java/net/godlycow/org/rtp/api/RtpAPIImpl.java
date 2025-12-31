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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class RtpAPIImpl implements RtpAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull RtpLocationStorage rtpLocationStorage;
    private final @NotNull RtpConfig rtpConfig;
    private final @NotNull RtpListener rtpListener;
    private final @NotNull PlayerLanguageManager langManager;

    public RtpAPIImpl(@NotNull ServerEssentials plugin,
                      @NotNull RtpLocationStorage rtpLocationStorage,
                      @NotNull RtpConfig rtpConfig,
                      @NotNull RtpListener rtpListener,
                      @NotNull PlayerLanguageManager langManager) {
        this.plugin = plugin;
        this.rtpLocationStorage = rtpLocationStorage;
        this.rtpConfig = rtpConfig;
        this.rtpListener = rtpListener;
        this.langManager = langManager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> randomTeleport(@NotNull Player player) {
        return randomTeleport(player, player.getWorld());
    }

    @Override
    public @NotNull CompletableFuture<Boolean> randomTeleport(@NotNull Player player, @NotNull World world) {
        return CompletableFuture.supplyAsync(() -> {
            if (!player.hasPermission("serveressentials.command.rtp")) {
                return false;
            }

            if (!isRtpEnabled(world.getName()).join()) {
                return false;
            }

            long remainingCooldown = getRemainingCooldown(player.getUniqueId()).join();
            if (remainingCooldown > 0) {
                return false;
            }

            int minRadius = rtpConfig.getMinRadius(world.getName());
            int maxRadius = rtpConfig.getMaxRadius(world.getName());

            if (maxRadius < minRadius) {
                int swap = minRadius;
                minRadius = maxRadius;
                maxRadius = swap;
            }

            Location safeLocation = findSafeLocation(player, world, minRadius, maxRadius).join();
            if (safeLocation == null) {
                return false;
            }

            Location fromLocation = player.getLocation();

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(safeLocation);

                saveRtpLocation(player.getUniqueId(), safeLocation).join();

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

                player.sendMessage(langManager.getMessageFor(player, "rtp.teleport_success",
                        "<green>Teleported to <white>{world}</white>!",
                        LanguageManager.ComponentPlaceholder.of("{world}", world.getName())));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> openRtpGUI(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            if (!player.hasPermission("serveressentials.command.rtp")) {
                return false;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                org.bukkit.inventory.Inventory gui = Bukkit.createInventory(null, 9,
                        langManager.getMessageFor(player, "rtp.gui.title", "RTP Menu"));

                gui.setItem(2, createGuiItem(player, Material.GRASS_BLOCK, "Overworld"));
                gui.setItem(4, createGuiItem(player, Material.NETHERRACK, "Nether"));
                gui.setItem(6, createGuiItem(player, Material.END_STONE, "The End"));

                player.openInventory(gui);
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<RtpLocation>> getLastRtpLocation(@NotNull UUID playerId) {
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
    public @NotNull CompletableFuture<Void> saveRtpLocation(@NotNull UUID playerId, @NotNull Location location) {
        return rtpLocationStorage.saveRtpLocation(
                playerId,
                "Unknown",
                location
        ).thenRun(() -> {
            Player player = plugin.getServer().getPlayer(playerId);
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
    public @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull UUID playerId) {
        // still need to implement this
        return CompletableFuture.completedFuture(0L);
    }

    @Override
    public @NotNull CompletableFuture<Optional<RtpWorldConfig>> getWorldConfig(@NotNull String worldName) {
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
    public @NotNull CompletableFuture<Boolean> isRtpEnabled(@NotNull String worldName) {
        return CompletableFuture.supplyAsync(() -> rtpConfig.isWorldEnabled(worldName));
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("rtp.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            rtpConfig.reload();
            plugin.getLogger().info("[ServerEssentials] RTP configuration reloaded");
        });
    }

    private @NotNull CompletableFuture<Location> findSafeLocation(@NotNull Player player, @NotNull World world,
                                                                  int minRadius, int maxRadius) {
        return CompletableFuture.supplyAsync(() -> {
            java.util.Random random = new java.util.Random();
            int maxAttempts = world.getEnvironment() == World.Environment.NETHER ? 50 : 25;

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int x = random.nextInt(maxRadius - minRadius + 1) + minRadius;
                int z = random.nextInt(maxRadius - minRadius + 1) + minRadius;
                if (random.nextBoolean()) x = -x;
                if (random.nextBoolean()) z = -z;

                Location testLoc = world.getEnvironment() == World.Environment.NETHER
                        ? findSafeNetherLocation(world, x, z, random)
                        : findSafeOverworldLocation(world, x, z);

                if (testLoc != null) {
                    return testLoc;
                }
            }

            return null;
        });
    }

    private @Nullable Location findSafeOverworldLocation(@NotNull World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z) + 1;
        Location test = new Location(world, x + 0.5, y, z + 0.5);

        return isLocationSafeSync(test) ? test : null;
    }

    private @Nullable Location findSafeNetherLocation(@NotNull World world, int x, int z, java.util.Random random) {
        for (int i = 0; i < 15; i++) {
            int y = i == 0 ? random.nextInt(90) + 30 : 30 + (i * 6);
            if (y > 120) y = 120 - (i % 15);

            Location test = new Location(world, x + 0.5, y, z + 0.5);
            if (isNetherLocationSafeSync(test)) {
                return test;
            }
        }
        return null;
    }

    private boolean isLocationSafeSync(@NotNull Location loc) {
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

            org.bukkit.Material g = ground.getType();
            if (!g.isSolid()) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNetherLocationSafeSync(@NotNull Location loc) {
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

            return ground.getType().isSolid();
        } catch (Exception e) {
            return false;
        }
    }

    private @NotNull ItemStack createGuiItem(@NotNull Player player, @NotNull org.bukkit.Material material,
                                             @NotNull String worldName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(langManager.getMessageFor(player, "rtp.gui." + worldName.toLowerCase() + ".name", worldName));
        meta.lore(langManager.getMessageList(player, "rtp.gui." + worldName.toLowerCase() + ".lore"));
        item.setItemMeta(meta);
        return item;
    }
}