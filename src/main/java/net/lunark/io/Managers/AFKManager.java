package net.lunark.io.Managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class AFKManager implements Listener {

    private static Plugin plugin;
    private static File configFile;
    private static YamlConfiguration config;
    private static final Map<UUID, Long> lastActivity = new HashMap<>();
    private static final Set<UUID> afkPlayers = new HashSet<>();
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public AFKManager(Plugin pl) {
        plugin = pl;
        loadConfig();

        Bukkit.getPluginManager().registerEvents(this, plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, AFKManager::checkAFK, 20L, 20L);
    }

    private static void loadConfig() {
        configFile = new File(plugin.getDataFolder() + "/config/afk", "afk.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config/afk/afk.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        Bukkit.getLogger().info("[ServerEssentials] AFK system reloaded.");
    }


    private static void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (afkPlayers.remove(player.getUniqueId())) {
            sendMessage(player, "messages.back", true);
            removeBossBar(player);
        }
    }

    private static void checkAFK() {
        if (config == null) return;
        long timeout = config.getLong("settings.timeout-seconds") * 1000L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!lastActivity.containsKey(player.getUniqueId())) {
                lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
                continue;
            }

            if (!afkPlayers.contains(player.getUniqueId()) &&
                    (System.currentTimeMillis() - lastActivity.get(player.getUniqueId())) >= timeout) {
                setAFK(player, true);
            }
        }
    }

    public static void setAFK(Player player, boolean afk) {
        if (afk) {
            afkPlayers.add(player.getUniqueId());
            sendMessage(player, "messages.afk", true);
            showBossBar(player);

            if (config.getBoolean("settings.kick-on-afk")) {
                player.kick(mm.deserialize(config.getString("messages.kick")));
            }

            if (config.getBoolean("settings.teleport.enabled")) {
                String worldName = config.getString("settings.teleport.world");
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble("settings.teleport.x");
                    double y = config.getDouble("settings.teleport.y");
                    double z = config.getDouble("settings.teleport.z");
                    player.teleport(new Location(world, x, y, z));
                }
            }

            if (config.getBoolean("settings.sounds.enabled")) {
                player.playSound(player.getLocation(),
                        Sound.valueOf(config.getString("settings.sounds.sound")),
                        1.0f, 1.0f);
            }

            if (config.getBoolean("settings.particles.enabled")) {
                player.getWorld().spawnParticle(
                        Particle.valueOf(config.getString("settings.particles.particle")),
                        player.getLocation(), 30, 0.5, 1, 0.5, 0.01
                );
            }

        } else {
            afkPlayers.remove(player.getUniqueId());
            sendMessage(player, "messages.back", true);
            removeBossBar(player);
        }
    }

    private static void sendMessage(Player player, String path, boolean broadcast) {
        String msg = config.getString(path);
        if (msg == null || msg.isEmpty()) return;

        Component component = mm.deserialize(msg.replace("%player%", player.getName()));

        if (broadcast && config.getBoolean("settings.broadcast.enabled")) {
            Bukkit.broadcast(component);
        } else {
            player.sendMessage(component);
        }
    }

    private static void showBossBar(Player player) {
        if (!config.getBoolean("settings.bossbar.enabled")) return;

        String text = config.getString("settings.bossbar.text")
                .replace("%player%", player.getName());

        BossBar.Color color = BossBar.Color.valueOf(config.getString("settings.bossbar.color"));
        BossBar.Overlay overlay = BossBar.Overlay.valueOf(config.getString("settings.bossbar.style"));

        BossBar bossBar = BossBar.bossBar(mm.deserialize(text), 1.0f, color, overlay);
        bossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
    }

    private static void removeBossBar(Player player) {
        if (bossBars.containsKey(player.getUniqueId())) {
            player.hideBossBar(bossBars.get(player.getUniqueId()));
            bossBars.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getFrom().toVector().equals(e.getTo().toVector())) {
            updateActivity(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastActivity.remove(e.getPlayer().getUniqueId());
        afkPlayers.remove(e.getPlayer().getUniqueId());
        removeBossBar(e.getPlayer());
    }

    public static boolean isAFK(Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }

    public static Set<String> getAFKPlayers() {
        Set<String> names = new HashSet<>();
        for (UUID id : afkPlayers) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) names.add(p.getName());
        }
        return names;
    }
}
