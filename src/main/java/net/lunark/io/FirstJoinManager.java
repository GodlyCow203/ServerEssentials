package net.lunark.io;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Duration;
import java.util.*;

public final class FirstJoinManager implements Listener, CommandExecutor, TabExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey KEY_PROCESSED;
    private final NamespacedKey KEY_ITEM_CMD;
    private final MiniMessage MINI = MiniMessage.miniMessage();
    private final BukkitAudiences adventure;
    private final boolean adventurePresent;

    private File configFile;
    private FileConfiguration cfg;

    private final Map<String, GUI> guiCache = new HashMap<>();
    private final NamespacedKey KEY_ITEM_GUI;   // declare, don't instantiate

    public FirstJoinManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.KEY_PROCESSED = new NamespacedKey(plugin, "fj_done");
        this.KEY_ITEM_CMD = new NamespacedKey(plugin, "fj_item_cmd");
        this.KEY_ITEM_GUI = new NamespacedKey(plugin, "fj_open_gui");



        BukkitAudiences tempAdventure = null;
        boolean tempPresent = false;
        try {
            tempAdventure = BukkitAudiences.create(plugin);
            tempPresent = true;
        } catch (Throwable t) {
            plugin.getLogger().info("Adventure not found – falling back to legacy colours.");
        }
        this.adventure = tempAdventure;
        this.adventurePresent = tempPresent;

        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Objects.requireNonNull(plugin.getCommand("firstjoin")).setExecutor(this);
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "FJ.yml");
        if (!configFile.exists()) plugin.saveResource("FJ.yml", false);
        cfg = YamlConfiguration.loadConfiguration(configFile);
        guiCache.clear();
        ConfigurationSection guiSection = cfg.getConfigurationSection("guis");
        if (guiSection != null) {
            for (String key : guiSection.getKeys(false)) {
                guiCache.put(key.toLowerCase(Locale.ROOT), new GUI(key, guiSection.getConfigurationSection(key)));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.getPersistentDataContainer().has(KEY_PROCESSED, PersistentDataType.BYTE)) return;
        if (p.hasPlayedBefore()) return;

        p.getPersistentDataContainer().set(KEY_PROCESSED, PersistentDataType.BYTE, (byte) 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> runFirstJoin(p), 1L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) return false;
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("firstjoin.reload")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            loadConfig();
            sender.sendMessage("§aFJ.yml reloaded.");
            return true;
        }
        if (args[0].equalsIgnoreCase("gui")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cPlayer only.");
                return true;
            }
            if (!sender.hasPermission("firstjoin.gui")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /firstjoin gui <id>");
                return true;
            }
            GUI gui = guiCache.get(args[1].toLowerCase(Locale.ROOT));
            if (gui == null) {
                sender.sendMessage("§cUnknown GUI '" + args[1] + "'.");
                return true;
            }
            gui.open(p);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) return List.of("reload", "gui");
        if (args.length == 2 && args[0].equalsIgnoreCase("gui"))
            return new ArrayList<>(guiCache.keySet());
        return Collections.emptyList();
    }

    private void runFirstJoin(Player p) {
        if (cfg.isConfigurationSection("titles")) {
            ConfigurationSection t = cfg.getConfigurationSection("titles");
            sendTitle(p,
                    t.getString("title", ""),
                    t.getString("subtitle", ""),
                    t.getInt("fade-in", 10),
                    t.getInt("stay", 70),
                    t.getInt("fade-out", 20));
        }

        /* Boss bars */
        if (cfg.isList("bossbars")) {
            for (Map<?, ?> raw : cfg.getMapList("bossbars")) {
                new BossBarRunner(p, (Map<String, Object>) raw).start();
            }
        }

        /* Effects */
        applyEffects(p);

        /* Particles */
        if (cfg.isList("particles")) {
            for (Map<?, ?> raw : cfg.getMapList("particles")) {
                Map<String, Object> m = (Map<String, Object>) raw;
                spawnParticles(p, m);
            }
        }

        /* Commands */
        if (cfg.isList("commands")) {
            for (Map<?, ?> raw : cfg.getMapList("commands")) {
                Map<String, Object> m = (Map<String, Object>) raw;
                runCommand(p,
                        (String) m.getOrDefault("run_as", "console"),
                        (String) m.get("command"),
                        ((Number) m.getOrDefault("delay_ticks", 0)).intValue());
            }
        }

        /* Items */
        if (cfg.isList("items")) {
            for (Map<?, ?> raw : cfg.getMapList("items")) {
                giveItem(p, (Map<String, Object>) raw);
            }
        }

        /* Messages */
        if (cfg.isList("messages")) {
            for (String line : cfg.getStringList("messages")) {
                sendMessage(p, line);
            }
        }

        /* GUI */
        String guiId = cfg.getString("open_gui");
        if (guiId != null) {
            GUI gui = guiCache.get(guiId.toLowerCase(Locale.ROOT));
            if (gui != null) Bukkit.getScheduler().runTaskLater(plugin, () -> gui.open(p), 2L);
        }
    }

    /* ---------- EFFECTS ---------- */
    private void applyEffects(Player p) {
        if (!cfg.isList("effects")) return;
        for (Map<?, ?> raw : cfg.getMapList("effects")) {
            Map<String, Object> m = (Map<String, Object>) raw;
            String type = (String) m.getOrDefault("type", "SPEED");
            int amp = ((Number) m.getOrDefault("amplifier", 1)).intValue();
            int dur = ((Number) m.getOrDefault("duration_seconds", 10)).intValue();
            PotionEffectType pet = PotionEffectType.getByName(type.toUpperCase(Locale.ROOT));
            if (pet == null) {
                plugin.getLogger().warning("Invalid effect: " + type);
                continue;
            }
            p.addPotionEffect(new PotionEffect(pet, dur * 20, Math.max(0, amp - 1)));
        }
    }

    /* ---------- PARTICLES ---------- */
    private void spawnParticles(Player p, Map<String, Object> m) {
        String name = (String) m.getOrDefault("particle", "VILLAGER_HAPPY");
        Particle particle;
        try {
            particle = Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            particle = Particle.GLOW;
        }
        Location loc = p.getLocation().add(0, 1, 0);
        int count = ((Number) m.getOrDefault("count", 10)).intValue();
        double ox = ((Number) m.getOrDefault("offset_x", 0d)).doubleValue();
        double oy = ((Number) m.getOrDefault("offset_y", 0d)).doubleValue();
        double oz = ((Number) m.getOrDefault("offset_z", 0d)).doubleValue();
        int duration = ((Number) m.getOrDefault("duration_seconds", 4)).intValue();
        int runs = Math.max(1, duration * 4);
        Particle finalParticle = particle;
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                loc.getWorld().spawnParticle(finalParticle, loc, count, ox, oy, oz);
                if (++i >= runs) cancel();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    /* ---------- ITEM ---------- */
    private void giveItem(Player p, Map<String, Object> map) {
        /* 1. material */
        String matName = (String) map.getOrDefault("material", "STONE");
        Material mat;
        try {
            mat = Material.valueOf(matName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("FJ.yml unknown material: " + matName);
            return;
        }

        /* 2. amount */
        int amount = Math.max(1, ((Number) map.getOrDefault("amount", 1)).intValue());

        /* 3. build stack */
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {   // some materials have no meta
            p.getInventory().addItem(item);
            return;
        }

        /* 4. display name */
        if (map.containsKey("name")) meta.displayName(parse(p, (String) map.get("name")));

        /* 5. lore */
        if (map.containsKey("lore")) {
            List<String> lines = (List<String>) map.get("lore");
            meta.lore(lines.stream().map(s -> parse(p, s)).toList());
        }

        /* 6. enchants */
        ConfigurationSection enchSec = (ConfigurationSection) map.get("enchants");
        if (enchSec != null) {
            for (String key : enchSec.getKeys(false)) {
                Enchantment ench = Enchantment.getByName(key.toUpperCase(Locale.ROOT));
                if (ench != null) meta.addEnchant(ench, enchSec.getInt(key), true);
            }
        }

        /* 7. flags */
        List<String> flags = (List<String>) map.get("flags");
        if (flags != null) flags.forEach(f -> {
            try { meta.addItemFlags(ItemFlag.valueOf(f.toUpperCase(Locale.ROOT))); } catch (Exception ignored) {}
        });

        /* 8. unbreakable / cmd */
        meta.setUnbreakable((Boolean) map.getOrDefault("unbreakable", false));
        if (map.containsKey("custom_model_data")) {
            try { meta.setCustomModelData(((Number) map.get("custom_model_data")).intValue()); } catch (Exception ignored) {}
        }

        /* 9. on-use-commands */
        if (map.containsKey("on_use_commands")) {
            List<String> cmds = (List<String>) map.get("on_use_commands");
            meta.getPersistentDataContainer().set(KEY_ITEM_CMD, PersistentDataType.STRING, String.join("||", cmds));
        }

        /* 10. GUI bind */
        if (map.containsKey("open_gui")) {
            String guiId = ((String) map.get("open_gui")).toLowerCase(Locale.ROOT);
            meta.getPersistentDataContainer().set(KEY_ITEM_GUI, PersistentDataType.STRING, guiId);
        }

        item.setItemMeta(meta);

        /* 11. slot or add */
        int slot = ((Number) map.getOrDefault("slot", -1)).intValue();
        if (slot >= 0 && slot < p.getInventory().getSize()) p.getInventory().setItem(slot, item);
        else p.getInventory().addItem(item);
    }

    /* ---------- COMMANDS ---------- */
    private void runCommand(Player p, String runAs, String cmd, int delay) {
        if (cmd == null || cmd.isBlank()) return;
        cmd = applyPlaceholders(p, cmd);
        String finalCmd = cmd;
        BukkitRunnable run = new BukkitRunnable() {
            @Override
            public void run() {
                executeCommand(p, runAs, finalCmd);
            }
        };
        if (delay > 0) run.runTaskLater(plugin, delay);
        else run.run();
    }

    private void executeCommand(Player p, String runAs, String cmd) {
        cmd = cmd.replace("{player}", p.getName())
                .replace("{uuid}", p.getUniqueId().toString())
                .replace("{world}", p.getWorld().getName());

        if (cmd.toLowerCase().startsWith("give ")) {
            handleGive(p, cmd);
            return;
        }

        switch (runAs.toLowerCase()) {
            case "console" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            case "op" -> {
                boolean was = p.isOp();
                try {
                    p.setOp(true);
                    p.performCommand(cmd);
                } finally {
                    p.setOp(was);
                }
            }
            case "player" -> p.performCommand(cmd);
            default -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    private void handleGive(Player p, String cmd) {
        String[] split = cmd.split(" ");
        if (split.length < 3) return;
        if (!split[1].equalsIgnoreCase(p.getName())) return;
        Material mat = Material.matchMaterial(split[2]);
        if (mat == null) {
            p.sendMessage("§cUnknown material: " + split[2]);
            return;
        }
        int amt = 1;
        if (split.length >= 4) {
            try { amt = Integer.parseInt(split[3]); } catch (Exception ignored) {}
        }
        p.getInventory().addItem(new ItemStack(mat, amt));
    }

    /* ---------- INTERACT ---------- */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        /* 1. Commands attached to the item */
        String cmds = pdc.get(KEY_ITEM_CMD, PersistentDataType.STRING);

        /* 2. GUI bound to the item */
        String guiId = pdc.get(KEY_ITEM_GUI, PersistentDataType.STRING);

        /* nothing to do -> leave event alone */
        if (cmds == null && guiId == null) return;

        /* mark as handled */
        e.setCancelled(true);

        Player p = e.getPlayer();

        /* run commands first */
        if (cmds != null) {
            for (String part : cmds.split("\\|\\|")) {
                if (part.isBlank()) continue;
                String runAs = "player";
                String command = part;
                if (part.contains(":")) {
                    String[] kv = part.split(":", 2);
                    runAs = kv[0];
                    command = kv[1];
                }
                executeCommand(p, runAs, command);
            }
        }

        /* open chained GUI one tick later so commands finish first */
        if (guiId != null) {
            GUI gui = guiCache.get(guiId.toLowerCase(Locale.ROOT));
            if (gui != null) Bukkit.getScheduler().runTask(plugin, () -> gui.open(p));
        }
    }

    /* ---------- GUI ---------- */
    private static final NamespacedKey KEY_GUI_CMD = new NamespacedKey(JavaPlugin.getProvidingPlugin(FirstJoinManager.class), "gui_cmd");

    private final class GUI {
        private final String id;
        private final String title;
        private final int rows;
        private final Map<Integer, ItemStack> items = new HashMap<>();

        GUI(String id, ConfigurationSection sec) {
            this.id = id;
            this.title = sec.getString("title", "GUI");
            this.rows = Math.max(1, Math.min(6, sec.getInt("rows", 3)));
            ConfigurationSection cont = sec.getConfigurationSection("items");
            if (cont != null) {
                for (String key : cont.getKeys(false)) {
                    int slot = Integer.parseInt(key);
                    ItemStack item = buildItem(cont.getConfigurationSection(key));
                    if (item != null) items.put(slot, item);
                }
            }
        }

        void open(Player p) {
            Inventory inv = Bukkit.createInventory(new Holder(this), rows * 9, parse(p, title));
            items.forEach(inv::setItem);
            p.openInventory(inv);
        }
    }

    private static final class Holder implements InventoryHolder {
        private final GUI gui;
        Holder(GUI gui) { this.gui = gui; }
        @Override public Inventory getInventory() { return null; }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Holder h)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getInventory()) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String raw = item.getItemMeta().getPersistentDataContainer().get(KEY_GUI_CMD, PersistentDataType.STRING);
        if (raw == null) return;
        Player p = (Player) e.getWhoClicked();
        for (String part : raw.split("\\|\\|")) {
            if (part.isBlank()) continue;
            String runAs = "player";
            String cmd = part;
            if (part.contains(":")) {
                String[] kv = part.split(":", 2);
                runAs = kv[0];
                cmd = kv[1];
            }
            if (cmd.equalsIgnoreCase("close")) {
                p.closeInventory();
                continue;
            }
            executeCommand(p, runAs, cmd);
        }
    }

    private ItemStack buildItem(ConfigurationSection sec) {
        if (sec == null) return null;
        String matName = sec.getString("material", "STONE");
        Material mat;
        try { mat = Material.valueOf(matName.toUpperCase(Locale.ROOT)); }
        catch (Exception ex) { mat = Material.STONE; }
        ItemStack item = new ItemStack(mat, Math.max(1, sec.getInt("amount", 1)));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        /* name */
        if (sec.contains("name")) meta.displayName(parse(null, sec.getString("name")));
        /* lore */
        if (sec.contains("lore")) {
            List<String> lore = sec.getStringList("lore");
            meta.lore(lore.stream().map(s -> parse(null, s)).toList());
        }
        /* enchants */
        ConfigurationSection enchSec = sec.getConfigurationSection("enchants");
        if (enchSec != null) {
            for (String k : enchSec.getKeys(false)) {
                Enchantment e = Enchantment.getByName(k.toUpperCase(Locale.ROOT));
                if (e != null) meta.addEnchant(e, enchSec.getInt(k), true);
            }
        }
        /* flags */
        if (sec.contains("flags")) {
            sec.getStringList("flags").forEach(f -> {
                try { meta.addItemFlags(ItemFlag.valueOf(f.toUpperCase(Locale.ROOT))); } catch (Exception ignored) {}
            });
        }
        /* unbreakable */
        meta.setUnbreakable(sec.getBoolean("unbreakable", false));
        /* custom-model-data */
        if (sec.contains("custom_model_data")) {
            try { meta.setCustomModelData(sec.getInt("custom_model_data")); } catch (Exception ignored) {}
        }
        /* commands */
        if (sec.contains("commands")) {
            List<String> cmds = sec.getStringList("commands");
            meta.getPersistentDataContainer().set(KEY_GUI_CMD, PersistentDataType.STRING, String.join("||", cmds));
        }

        item.setItemMeta(meta);
        return item;
    }

    /* ---------- UTILS ---------- */
    private void sendTitle(Player p, String title, String subtitle, int in, int stay, int out) {
        if (adventurePresent) {
            Audience a = adventure.player(p);
            Title.Times times = Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L));
            a.showTitle(Title.title(parse(p, title), parse(p, subtitle), times));
        } else {
            p.sendTitle(colorize(applyPlaceholders(p, title)), colorize(applyPlaceholders(p, subtitle)), in, stay, out);
        }
    }

    private void sendMessage(Player p, String raw) {
        Component c = parse(p, raw);
        if (adventurePresent) adventure.player(p).sendMessage(c);
        else p.sendMessage(LegacyComponentSerializer.legacyAmpersand().serialize(c));
    }

    private Component parse(@Nullable Player p, String input) {
        if (input == null) return Component.empty();
        input = applyPlaceholders(p, input);
        if (adventurePresent) return MINI.deserialize(input);
        return Component.text(colorize(input));
    }

    private String applyPlaceholders(Player p, String input) {
        if (input == null) return "";
        if (p != null) {
            input = input.replace("{player}", p.getName())
                    .replace("{display_name}", p.getDisplayName())
                    .replace("{uuid}", p.getUniqueId().toString())
                    .replace("{world}", p.getWorld().getName());
        }
        input = input.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max_players}", String.valueOf(Bukkit.getMaxPlayers()));
        return input;
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /* ---------- BOSSBAR ---------- */
    private final class BossBarRunner {
        private final Player player;
        private final String text;
        private final double seconds;
        private final BarColor color;
        private final BarStyle style;
        private final BossBar bar;

        BossBarRunner(Player p, Map<String, Object> m) {
            this.player = p;
            this.text = (String) m.getOrDefault("text", "&6Welcome");
            this.seconds = ((Number) m.getOrDefault("duration_seconds", 8)).doubleValue();
            String c = ((String) m.getOrDefault("color", "GREEN")).toUpperCase(Locale.ROOT);
            String s = ((String) m.getOrDefault("style", "SOLID")).toUpperCase(Locale.ROOT);
            this.color = tryParse(BarColor.class, c, BarColor.GREEN);
            this.style = tryParse(BarStyle.class, s, BarStyle.SOLID);
            Component comp = parse(player, this.text);   // MiniMessage → Adventure component
            this.bar = Bukkit.createBossBar(LegacyComponentSerializer.legacySection().serialize(comp), color, style);
        }

        void start() {
            bar.addPlayer(player);
            long ticks = Math.max(1L, (long) (seconds * 20));
            new BukkitRunnable() {
                long i = 0;
                @Override
                public void run() {
                    i++;
                    double progress = 1.0 - (double) i / ticks;
                    bar.setProgress(Math.max(0, progress));
                    if (i >= ticks) {
                        bar.removePlayer(player);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private <E extends Enum<E>> E tryParse(Class<E> clazz, String name, E def) {
        try { return Enum.valueOf(clazz, name); } catch (Exception ex) { return def; }
    }

    private String legacy(Component c) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(c);
    }
}