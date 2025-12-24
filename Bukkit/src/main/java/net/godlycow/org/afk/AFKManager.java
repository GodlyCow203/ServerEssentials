package net.godlycow.org.afk;

import net.godlycow.org.commands.config.AFKConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AFKManager implements Listener {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private final Set<UUID> afkPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private BukkitTask checkTask;

    private final Plugin plugin;
    private final AFKConfig config;

    public AFKManager(Plugin plugin, AFKConfig config) {
        this.plugin = plugin;
        this.config = config;
        loadOnlinePlayers();
        startCheckTask();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player ->
                lastActivity.put(player.getUniqueId(), System.currentTimeMillis())
        );
    }

    private void startCheckTask() {
        if (checkTask != null) checkTask.cancel();
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkAFK, 20L, 20L);
    }

    private void checkAFK() {
        if (!config.enabled) return;
        long timeoutMillis = config.timeoutSeconds * 1000L; // Convert seconds to milliseconds
        long currentTime = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (config.exemptWorlds.contains(player.getWorld().getName())) continue;
            if (player.hasPermission("serveressentials.afk.exempt")) continue;

            long lastActive = lastActivity.getOrDefault(uuid, currentTime);
            long timeSinceLastActivity = currentTime - lastActive;

            if (!afkPlayers.contains(uuid) && timeSinceLastActivity >= timeoutMillis) {
                setAFK(player, true);
            }
        }
    }

    public void setAFK(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        if (afk) {
            afkPlayers.add(uuid);
            lastActivity.put(uuid, System.currentTimeMillis());
            sendMessage(player, config.broadcastEnabled,
                    mini.deserialize(config.enterMessage.replace("%player%", player.getName())));

            if (config.actionBarEnabled)
                player.sendActionBar(mini.deserialize(config.actionBarText.replace("%player%", player.getName())));

            if (config.bossbarEnabled) showBossBar(player);
            if (config.disableFlight) player.setFlying(false);
            applyTablistModification(player, true);

            if (config.kickOnAFK) {
                player.kick(mini.deserialize(config.kickMessage));
                return;
            }

            if (config.teleportOnAFK) {
                World w = Bukkit.getWorld(config.teleportWorld);
                if (w != null)
                    player.teleport(new Location(w, config.teleportX, config.teleportY, config.teleportZ));
            }

            if (config.soundsEnabled) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(config.soundName), 1f, 1f);
                } catch (Exception ignored) {}
            }

            if (config.particlesEnabled) {
                try {
                    player.getWorld().spawnParticle(Particle.valueOf(config.particleName),
                            player.getLocation(), 30, 0.5, 1, 0.5, 0.01);
                } catch (Exception ignored) {}
            }
        } else {
            afkPlayers.remove(uuid);
            removeBossBar(player);
            applyTablistModification(player, false);
            sendMessage(player, config.broadcastEnabled,
                    mini.deserialize(config.leaveMessage.replace("%player%", player.getName())));

            if (config.actionBarEnabled)
                player.sendActionBar(mini.deserialize(config.actionBarText.replace("%player%", player.getName())));
        }
    }

    public void toggleAFK(Player player) {
        setAFK(player, !isAFK(player));
    }

    private void applyTablistModification(Player player, boolean afk) {
        if (!config.modifyTablist) return;
        if (afk) {
            Component name = mini.deserialize(config.tablistPrefix + player.getName() + config.tablistSuffix);
            player.playerListName(name);
        } else {
            player.playerListName(Component.text(player.getName()));
        }
    }

    private void sendMessage(Player player, boolean broadcast, Component component) {
        if (broadcast) Bukkit.getServer().sendMessage(component);
        else player.sendMessage(component);
    }

    private void showBossBar(Player player) {
        if (!config.bossbarEnabled) return;
        try {
            BossBar bar = BossBar.bossBar(
                    mini.deserialize(config.bossbarText.replace("%player%", player.getName())),
                    1f,
                    BossBar.Color.valueOf(config.bossbarColor.toUpperCase()),
                    BossBar.Overlay.valueOf(config.bossbarStyle.toUpperCase())
            );
            bossBars.put(player.getUniqueId(), bar);
            player.showBossBar(bar);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create BossBar for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void removeBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) player.hideBossBar(bar);
    }

    public void reload() {
        lastActivity.clear();
        afkPlayers.clear();
        bossBars.values().forEach(bar -> Bukkit.getOnlinePlayers().forEach(p -> p.hideBossBar(bar)));
        bossBars.clear();
        loadOnlinePlayers();
        startCheckTask();
    }

    public boolean isAFK(Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        lastActivity.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        lastActivity.remove(u);
        afkPlayers.remove(u);
        removeBossBar(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!e.getFrom().toVector().equals(e.getTo().toVector()))
            updateActivity(e.getPlayer());
    }

    public void updateActivity(Player player) {
        if (player.hasPermission("serveressentials.afk.exempt")) return;
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (afkPlayers.contains(player.getUniqueId()))
            setAFK(player, false);
    }
}