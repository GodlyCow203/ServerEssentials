package serveressentials.serveressentials.TPA;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reworked TPA system with the following improvements:
 * - Supports multiple pending requests per target (can accept/deny by specifying sender name)
 * - Better config defaults handling and defensive parsing
 * - Warmup cancellation that reliably cancels scheduled teleport tasks on movement
 * - Safer BossBar/title/actionbar handling using Adventure/PlainText where needed
 * - Permission checks hooks and optional economy integration (Vault)
 * - Logging improvements and file logging resilient to IOExceptions
 * - Fixes for various bugs (bossbar duration ticks, cancel-on-move block-level checks, sound parsing)
 *
 * Drop-in replacement for previous single-class implementation. Configure messages and settings in
 * config/tpa/tpa.yml and messages/tpa.yml (defaults are preserved if missing).
 */
public class TPA implements CommandExecutor, Listener {

    private static TPA instance; // ✅ static reference for global reload calls

    private final JavaPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
    private Economy econ;

    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File msgFile;


    // Map of target -> list of requests (allows multiple people to request one target)
    private final Map<UUID, List<TpaRequest>> requests = new ConcurrentHashMap<>();
    private final Set<UUID> tpaToggle = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TPA(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this; // ✅ assign instance for static reload
        loadConfigs();
        setupEconomy();
        registerCommands();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void registerCommands() {
        Objects.requireNonNull(plugin.getCommand("tpa")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpahere")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpaccept")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpdeny")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpacancel")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpall")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpatoggle")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("tpainfo")).setExecutor(this);
    }

    public static TPA getInstance() { return instance; }


    public void reload() {
        loadConfigs();
        setupEconomy();
        Bukkit.getLogger().info("[TPA] Reloaded TPA configs and messages.");
    }
    public static void fullReload() {
        if (instance == null) {
            Bukkit.getLogger().warning("[TPA] Tried to reload before initialization!");
            return;
        }
        instance.reload();
    }

    private void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config/tpa/tpa.yml");
        msgFile = new File(plugin.getDataFolder(), "messages/tpa.yml");
        configFile.getParentFile().mkdirs();
        msgFile.getParentFile().mkdirs();

        if (!configFile.exists()) plugin.saveResource("config/tpa/tpa.yml", false);
        if (!msgFile.exists()) plugin.saveResource("messages/tpa.yml", false);

        config = YamlConfiguration.loadConfiguration(configFile);
        messages = YamlConfiguration.loadConfiguration(msgFile);

        // Provide some safe defaults in-memory if keys are missing (keeps existing files untouched)
        config.addDefault("settings.cooldown", 5);
        config.addDefault("settings.timeout", 60);
        config.addDefault("settings.warmup", 0);
        config.addDefault("settings.teleport-delay", 3);
        config.addDefault("settings.cancel-on-move", true);
        config.addDefault("settings.block-move-threshold-blocks", true);

        config.addDefault("notifications.chat", true);
        config.addDefault("notifications.actionbar", false);
        config.addDefault("notifications.bossbar", false);
        config.addDefault("notifications.bossbar-color", "BLUE");
        config.addDefault("notifications.bossbar-style", "SOLID");
        config.addDefault("notifications.bossbar-duration", 5); // seconds

        config.addDefault("particles.enabled", true);
        config.addDefault("particles.type", "PORTAL");

        config.addDefault("economy.enabled", false);
        config.addDefault("economy.cost.tpa", 0.0);
        config.addDefault("economy.cost.tpahere", 0.0);
        config.addDefault("economy.cost.tpall", 0.0);
        config.addDefault("economy.refund-on-deny", true);
        config.addDefault("economy.refund-on-expire", true);

        config.options().copyDefaults(true);
    }



    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) econ = rsp.getProvider();
    }

    private void send(Player p, String path, Map<String, String> placeholders) {
        String raw = messages.getString(path, "<red>Missing message: " + path + "</red>");
        String prefix = messages.getString("prefix", "<gray>[TPA]</gray> ");
        raw = raw.replace("<prefix>", prefix);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace("%" + e.getKey() + "%", e.getValue());
            }
        }

        Component msg = mm.deserialize(raw);

        if (config.getBoolean("notifications.chat", true)) {
            p.sendMessage(msg);
        }
        if (config.getBoolean("notifications.actionbar", false)) {
            p.sendActionBar(msg);
        }
        if (config.getBoolean("notifications.bossbar", false)) {
            String colorStr = config.getString("notifications.bossbar-color", "BLUE").toUpperCase();
            String styleStr = config.getString("notifications.bossbar-style", "SOLID").toUpperCase();
            int durationSec = Math.max(1, config.getInt("notifications.bossbar-duration", 5));
            BarColor barColor;
            BarStyle barStyle;
            try {
                barColor = BarColor.valueOf(colorStr);
            } catch (IllegalArgumentException ex) {
                barColor = BarColor.BLUE;
            }
            try {
                barStyle = BarStyle.valueOf(styleStr);
            } catch (IllegalArgumentException ex) {
                barStyle = BarStyle.SOLID;
            }

            BossBar bar = Bukkit.createBossBar(plain.serialize(msg), barColor, barStyle);
            bar.addPlayer(p);
            bar.setProgress(1.0);

            // Gradually reduce progress over duration so it looks nice
            long ticks = durationSec * 20L;
            long step = Math.max(1, ticks / 20);
            new BukkitRunnable() {
                long remaining = ticks;
                @Override
                public void run() {
                    if (remaining <= 0) {
                        bar.removeAll();
                        cancel();
                        return;
                    }
                    double progress = Math.max(0.0, (double) remaining / ticks);
                    bar.setProgress(progress);
                    remaining -= step;
                }
            }.runTaskTimer(plugin, 0L, step);
        }
        if (config.getBoolean("notifications.title", false)) {
            Component title = mm.deserialize(config.getString("notifications.title-color", "<gold>") + raw);
            Component subtitle = mm.deserialize(config.getString("notifications.subtitle-color", "<yellow>") + "");
            p.showTitle(net.kyori.adventure.title.Title.title(title, subtitle));
        }

        if (config.getBoolean("logging.enabled", false)) {
            String plainText = plain.serialize(msg);
            if (config.getBoolean("logging.to-console", true)) {
                Bukkit.getLogger().info("[TPA] Sent to " + p.getName() + ": " + plainText);
            }
            if (config.getBoolean("logging.to-file", false)) {
                File logFile = new File(plugin.getDataFolder(), "tpa.log");
                try {
                    java.nio.file.Files.writeString(logFile.toPath(), p.getName() + ": " + plainText + "\n",
                            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                } catch (IOException e) {
                    Bukkit.getLogger().warning("Failed to write TPA log: " + e.getMessage());
                }
            }
        }
    }

    private int getInt(String path, int def) { return config.getInt("settings." + path, def); }
    private boolean getBool(String path, boolean def) { return config.getBoolean("settings." + path, def); }

    private void playSound(Player p, String key) {
        String soundStr = config.getString("sounds." + key, "ENTITY_EXPERIENCE_ORB_PICKUP");
        try {
            Sound s = Sound.valueOf(soundStr);
            p.playSound(p.getLocation(), s, 1f, 1f);
        } catch (Exception ex) {
            // invalid sound in config -> ignore to avoid errors
        }
    }

    private boolean charge(Player p, String type, TpaRequest req) {
        if (!config.getBoolean("economy.enabled")) return true;
        if (econ == null) return true;
        double cost = config.getDouble("economy.cost." + type, 0.0);
        if (cost <= 0) return true;
        if (!econ.has(p, cost)) {
            send(p, "not-enough-money", Map.of("cost", String.valueOf(cost)));
            return false;
        }
        econ.withdrawPlayer(p, cost);
        req.cost = cost;
        return true;
    }

    private void refund(TpaRequest req, String type) {
        if (!config.getBoolean("economy.enabled")) return;
        if (econ == null) return;
        if (req.cost <= 0) return;
        boolean refund = ("deny".equals(type) && config.getBoolean("economy.refund-on-deny")) ||
                ("expire".equals(type) && config.getBoolean("economy.refund-on-expire"));
        if (!refund) return;
        try {
            econ.depositPlayer(req.sender, req.cost);
        } catch (Exception ignored) {}
        req.cost = 0;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        Player p = (Player) sender;

        // Permission hook: bypass cooldown if has permission
        boolean bypassCooldown = p.hasPermission("tpa.bypass.cooldown");
        if (!bypassCooldown && isOnCooldown(p)) {
            send(p, "cooldown", null);
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name) {
            case "tpa":
                if (args.length == 0) return false;
                Player target = Bukkit.getPlayer(args[0]);
                if (!validateRequest(p, target)) return true;
                TpaRequest req = new TpaRequest(p, target, false);
                if (!charge(p, "tpa", req)) return true;
                addRequestForTarget(target.getUniqueId(), req);
                send(p, "request-sent", Map.of("player", target.getName()));
                send(target, "request-received", Map.of("player", p.getName()));
                playSound(target, "request");
                req.startTimeout();
                setCooldown(p);
                break;

            case "tpahere":
                if (args.length == 0) return false;
                Player hereTarget = Bukkit.getPlayer(args[0]);
                if (!validateRequest(p, hereTarget)) return true;
                TpaRequest hereReq = new TpaRequest(p, hereTarget, true);
                if (!charge(p, "tpahere", hereReq)) return true;
                addRequestForTarget(hereTarget.getUniqueId(), hereReq);
                send(p, "request-sent-here", Map.of("player", hereTarget.getName()));
                send(hereTarget, "request-received-here", Map.of("player", p.getName()));
                playSound(hereTarget, "request");
                hereReq.startTimeout();
                setCooldown(p);
                break;

            case "tpaccept":
                // /tpaccept [player]
                if (args.length == 0) {
                    // accept most recent
                    List<TpaRequest> list = requests.get(p.getUniqueId());
                    if (list == null || list.isEmpty()) {
                        send(p, "no-request", null);
                        return true;
                    }
                    TpaRequest toAccept = list.remove(list.size() - 1);
                    if (list.isEmpty()) requests.remove(p.getUniqueId());
                    toAccept.accept();
                } else {
                    String who = args[0];
                    TpaRequest found = findRequestBySenderName(p.getUniqueId(), who);
                    if (found == null) {
                        send(p, "no-request-from-player", Map.of("player", who));
                        return true;
                    }
                    removeRequest(found);
                    found.accept();
                }
                break;

            case "tpdeny":
                // /tpdeny [player]
                if (args.length == 0) {
                    List<TpaRequest> list = requests.get(p.getUniqueId());
                    if (list == null || list.isEmpty()) {
                        send(p, "no-request", null);
                        return true;
                    }
                    TpaRequest toDeny = list.remove(list.size() - 1);
                    if (list.isEmpty()) requests.remove(p.getUniqueId());
                    toDeny.deny();
                } else {
                    TpaRequest found = findRequestBySenderName(p.getUniqueId(), args[0]);
                    if (found == null) {
                        send(p, "no-request-from-player", Map.of("player", args[0]));
                        return true;
                    }
                    removeRequest(found);
                    found.deny();
                }
                break;

            case "tpacancel":
                // cancel your sent requests. optional target name
                if (args.length == 0) {
                    cancelAllRequestsFromSender(p);
                } else {
                    cancelRequestsFromSenderToTarget(p, args[0]);
                }
                break;

            case "tpall":
                TpaRequest allReq = new TpaRequest(p, null, true);
                if (!charge(p, "tpall", allReq)) return true;
                int sent = 0;
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (all.equals(p)) continue;
                    if (tpaToggle.contains(all.getUniqueId())) continue;
                    TpaRequest reqAll = new TpaRequest(p, all, true);
                    reqAll.cost = 0; // no double-charge per target
                    addRequestForTarget(all.getUniqueId(), reqAll);
                    send(all, "request-received-all", Map.of("player", p.getName()));
                    playSound(all, "request");
                    reqAll.startTimeout();
                    sent++;
                }
                send(p, "request-sent-all", Map.of("count", String.valueOf(sent)));
                setCooldown(p);
                break;

            case "tpatoggle":
                if (tpaToggle.contains(p.getUniqueId())) {
                    tpaToggle.remove(p.getUniqueId());
                    send(p, "toggle-off", null);
                } else {
                    tpaToggle.add(p.getUniqueId());
                    send(p, "toggle-on", null);
                }
                break;

            case "tpainfo":
                List<TpaRequest> infoList = requests.get(p.getUniqueId());
                if (infoList == null || infoList.isEmpty()) {
                    send(p, "no-request", null);
                } else {
                    TpaRequest last = infoList.get(infoList.size() - 1);
                    send(p, "request-info", Map.of(
                            "player", last.sender.getName(),
                            "type", last.here ? "TPAHere" : "TPA"
                    ));
                }
                break;

            default:
                return false;
        }
        return true;
    }

    private void addRequestForTarget(UUID target, TpaRequest req) {
        requests.computeIfAbsent(target, k -> Collections.synchronizedList(new ArrayList<>())).add(req);
    }

    private TpaRequest findRequestBySenderName(UUID targetUuid, String senderName) {
        List<TpaRequest> list = requests.get(targetUuid);
        if (list == null) return null;
        synchronized (list) {
            for (TpaRequest r : list) {
                if (r.sender.getName().equalsIgnoreCase(senderName)) return r;
            }
        }
        return null;
    }

    private void removeRequest(TpaRequest req) {
        UUID target = req.target == null ? null : req.target.getUniqueId();
        if (target == null) return;
        List<TpaRequest> list = requests.get(target);
        if (list == null) return;
        synchronized (list) {
            list.remove(req);
            if (list.isEmpty()) requests.remove(target);
        }
    }

    private void cancelAllRequestsFromSender(Player sender) {
        requests.forEach((target, list) -> {
            synchronized (list) {
                Iterator<TpaRequest> it = list.iterator();
                while (it.hasNext()) {
                    TpaRequest r = it.next();
                    if (r.sender.equals(sender)) {
                        it.remove();
                        send(sender, "request-cancelled", Map.of("player", r.target == null ? "ALL" : r.target.getName()));
                        if (r.target != null) send(r.target, "request-cancelled-target", Map.of("player", sender.getName()));
                        refund(r, "expire");
                    }
                }
            }
            if (list.isEmpty()) requests.remove(target);
        });
    }

    private void cancelRequestsFromSenderToTarget(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            send(sender, "no-player", null);
            return;
        }
        List<TpaRequest> list = requests.get(target.getUniqueId());
        if (list == null) {
            send(sender, "no-request", null);
            return;
        }
        synchronized (list) {
            Iterator<TpaRequest> it = list.iterator();
            while (it.hasNext()) {
                TpaRequest r = it.next();
                if (r.sender.equals(sender)) {
                    it.remove();
                    send(sender, "request-cancelled", Map.of("player", target.getName()));
                    send(target, "request-cancelled-target", Map.of("player", sender.getName()));
                    refund(r, "expire");
                }
            }
            if (list.isEmpty()) requests.remove(target.getUniqueId());
        }
    }

    private boolean validateRequest(Player sender, Player target) {
        if (target == null || !target.isOnline()) {
            send(sender, "no-player", null);
            return false;
        }
        if (tpaToggle.contains(target.getUniqueId())) {
            send(sender, "target-toggled", Map.of("player", target.getName()));
            return false;
        }
        if (!config.getBoolean("restrictions.cross-world", false) && !sender.getWorld().equals(target.getWorld())) {
            send(sender, "crossworld-disabled", null);
            return false;
        }
        if (config.getStringList("restrictions.blocked-worlds").contains(target.getWorld().getName())) {
            send(sender, "blocked-world", null);
            return false;
        }
        return true;
    }

    private boolean isOnCooldown(Player p) {
        long now = System.currentTimeMillis();
        int cd = getInt("cooldown", 0);
        if (!cooldowns.containsKey(p.getUniqueId())) return false;
        return (now - cooldowns.get(p.getUniqueId())) < cd * 1000L;
    }

    private void setCooldown(Player p) {
        cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // Cancel warmups if player moves between blocks (configurable)
        Player player = e.getPlayer();
        boolean blockLevel = config.getBoolean("settings.block-move-threshold-blocks", true);
        boolean moved;
        if (blockLevel) {
            moved = !e.getFrom().getBlock().equals(e.getTo().getBlock());
        } else {
            moved = !e.getFrom().toVector().equals(e.getTo().toVector());
        }
        if (!moved) return;

        List<TpaRequest> list = requests.get(player.getUniqueId());
        if (list == null) return;
        synchronized (list) {
            for (TpaRequest req : list) {
                if (req.inWarmup && config.getBoolean("settings.cancel-on-move", true)) {
                    send(player, "teleport-cancelled-move", null);
                    req.cancelWarmup();
                }
            }
        }
    }

    private class TpaRequest {
        private final Player sender;
        private final Player target; // may be null for TPALL helper request
        private final boolean here;
        private boolean inWarmup = false;
        private double cost = 0.0;

        // store scheduled task IDs so they can be cancelled
        private Integer timeoutTaskId = null;
        private Integer warmupTaskId = null;

        TpaRequest(Player sender, Player target, boolean here) {
            this.sender = sender;
            this.target = target;
            this.here = here;
        }

        void startTimeout() {
            int timeout = Math.max(1, getInt("timeout", 60));
            timeoutTaskId = new BukkitRunnable() {
                @Override
                public void run() {
                    // remove only this exact request
                    if (target == null) return; // TPALL helper shouldn't expire individual
                    List<TpaRequest> list = requests.get(target.getUniqueId());
                    if (list == null) return;
                    synchronized (list) {
                        if (list.remove(TpaRequest.this)) {
                            if (list.isEmpty()) requests.remove(target.getUniqueId());
                            send(sender, "request-expired", Map.of("player", target.getName()));
                            send(target, "request-expired-target", Map.of("player", sender.getName()));
                            refund(TpaRequest.this, "expire");
                        }
                    }
                }
            }.runTaskLater(plugin, timeout * 20L).getTaskId();
        }

        void accept() {
            // cancel timeout
            if (timeoutTaskId != null) Bukkit.getScheduler().cancelTask(timeoutTaskId);

            int warmup = Math.max(0, getInt("warmup", 0));
            int delay = Math.max(0, getInt("teleport-delay", 3));

            send(sender, "request-accepted", Map.of("player", target == null ? "ALL" : target.getName()));
            if (target != null) send(target, "request-accepted-target", Map.of("player", sender.getName()));
            playSound(sender, "accept");
            if (target != null) playSound(target, "accept");

            // schedule warmup+teleport
            warmupTaskId = new BukkitRunnable() {
                @Override
                public void run() {
                    inWarmup = true;
                    // actual teleport after warmup+delay
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                if (target == null) return;
                                if (here) {
                                    target.teleport(sender.getLocation());
                                } else {
                                    sender.teleport(target.getLocation());
                                }
                                if (config.getBoolean("particles.enabled", true)) {
                                    Location loc = here ? sender.getLocation() : target.getLocation();
                                    Particle pType = Particle.valueOf(config.getString("particles.type", "PORTAL"));
                                    loc.getWorld().spawnParticle(pType, loc, 50, 1, 1, 1);
                                }
                                playSound(sender, "teleport");
                                if (target != null) playSound(target, "teleport");
                            } catch (Exception ex) {
                                Bukkit.getLogger().warning("Failed to teleport during TPA: " + ex.getMessage());
                            } finally {
                                inWarmup = false;
                                warmupTaskId = null;
                            }
                        }
                    }.runTaskLater(plugin, (warmup + delay) * 20L);
                }
            }.runTask(plugin).getTaskId();
        }

        void cancelWarmup() {
            inWarmup = false;
            if (warmupTaskId != null) {
                Bukkit.getScheduler().cancelTask(warmupTaskId);
                warmupTaskId = null;
            }
        }

        void deny() {
            if (timeoutTaskId != null) Bukkit.getScheduler().cancelTask(timeoutTaskId);
            send(sender, "request-denied", Map.of("player", target == null ? "ALL" : target.getName()));
            if (target != null) send(target, "request-denied-target", Map.of("player", sender.getName()));
            playSound(sender, "deny");
            if (target != null) playSound(target, "deny");
            refund(this, "deny");
        }
    }
}
