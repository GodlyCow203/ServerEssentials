package serveressentials.serveressentials;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class DailyRewards implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<String>> claimedRewards = new HashMap<>();
    private final Map<UUID, LocalDateTime> lastClaimTime = new HashMap<>();
    private final FileConfiguration config;
    private final File configFile;
    private final int cooldownHours = 24;

    public DailyRewards(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "daily.yml");
        if (!configFile.exists()) plugin.saveResource("daily.yml", false);
        this.config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openRewardsGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.of("#66ffff") + "Daily Rewards");

        List<String> keys = new ArrayList<>(config.getConfigurationSection("rewards").getKeys(false));
        int totalPages = 1;
        for (String day : keys) {
            int rewardPage = config.getInt("rewards." + day + ".page", 1);
            totalPages = Math.max(totalPages, rewardPage);
        }

        UUID uuid = player.getUniqueId();
        Set<String> claimed = claimedRewards.getOrDefault(uuid, new HashSet<>());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastClaim = lastClaimTime.getOrDefault(uuid, LocalDateTime.MIN);
        Duration cooldownLeft = Duration.ofHours(cooldownHours).minus(Duration.between(lastClaim, now));
        boolean onCooldown = !cooldownLeft.isNegative();

        for (String day : keys) {
            int rewardPage = config.getInt("rewards." + day + ".page", 1);
            if (rewardPage != page) continue;

            int slot = config.getInt("rewards." + day + ".slot", -1);
            if (slot < 0 || slot > 53) continue;

            List<Map<?, ?>> itemList = config.getMapList("rewards." + day + ".items");
            if (itemList.isEmpty()) continue;

            Map<?, ?> firstItem = itemList.get(0);
            Object matObj = firstItem.get("type");
            String materialName = (matObj != null) ? matObj.toString() : "DIAMOND";
            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;

            ItemStack displayItem;
            ItemMeta meta;

            if (claimed.contains(day)) {
                displayItem = new ItemStack(Material.LIME_CONCRETE);
                meta = displayItem.getItemMeta();
                meta.setDisplayName(ChatColor.of("#00ff00") + "Reward Claimed");
                meta.setLore(Collections.singletonList(ChatColor.of("#aaaaaa") + "You've already claimed this reward."));
            } else if (onCooldown || !canClaimDay(claimed, Integer.parseInt(day))) {
                displayItem = new ItemStack(Material.RED_CONCRETE);
                meta = displayItem.getItemMeta();
                meta.setDisplayName(ChatColor.of("#ff0000") + "Reward Locked");
                if (onCooldown) {
                    String timeLeft = String.format("%02dh %02dm %02ds", cooldownLeft.toHoursPart(), cooldownLeft.toMinutesPart(), cooldownLeft.toSecondsPart());
                    meta.setLore(Collections.singletonList(ChatColor.of("#aaaaaa") + "Next reward in: " + timeLeft));
                } else {
                    meta.setLore(Collections.singletonList(ChatColor.of("#aaaaaa") + "Claim previous rewards first."));
                }
            } else {
                displayItem = new ItemStack(material);
                meta = displayItem.getItemMeta();
                Object loreObj = firstItem.get("lore");
                String lore = (loreObj != null) ? loreObj.toString() : "&7Click to claim!";
                meta.setDisplayName(ChatColor.of("#00ffaa") + "Day " + day);
                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', lore)));
            }

            displayItem.setItemMeta(meta);
            gui.setItem(slot, displayItem);
        }

        if (page > 1) gui.setItem(45, navItem(Material.ARROW, ChatColor.of("#ffaa00") + "Previous Page"));
        if (page < totalPages) gui.setItem(53, navItem(Material.ARROW, ChatColor.of("#ffaa00") + "Next Page"));

        gui.setItem(49, navItem(Material.BARRIER, ChatColor.of("#ff5555") + "Close"));
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    private boolean canClaimDay(Set<String> claimed, int day) {
        for (int i = 1; i < day; i++) {
            if (!claimed.contains(String.valueOf(i))) return false;
        }
        return true;
    }

    private ItemStack navItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!e.getView().getTitle().contains("Daily Rewards")) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.6f);
            return;
        }

        if (clicked.getType() == Material.ARROW) {
            int newPage = clicked.getItemMeta().getDisplayName().contains("Previous") ? 1 : 2;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    openRewardsGUI(player, newPage);
                }
            }.runTaskLater(plugin, 1);
            return;
        }

        String clickedDay = null;
        for (String key : config.getConfigurationSection("rewards").getKeys(false)) {
            List<Map<?, ?>> itemList = config.getMapList("rewards." + key + ".items");
            if (!itemList.isEmpty()) {
                Object matObj = itemList.get(0).get("type");
                String matName = (matObj != null) ? matObj.toString() : "DIAMOND";
                Material mat = Material.matchMaterial(matName);
                if (mat != null && clicked.getType() == mat) {
                    clickedDay = key;
                    break;
                }
            }
        }

        if (clickedDay == null) return;

        UUID uuid = player.getUniqueId();
        Set<String> claimed = claimedRewards.computeIfAbsent(uuid, k -> new HashSet<>());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastClaim = lastClaimTime.getOrDefault(uuid, LocalDateTime.MIN);
        Duration cooldownLeft = Duration.ofHours(cooldownHours).minus(Duration.between(lastClaim, now));

        if (!cooldownLeft.isNegative()) {
            String timeLeft = String.format("%02dh %02dm %02ds", cooldownLeft.toHoursPart(), cooldownLeft.toMinutesPart(), cooldownLeft.toSecondsPart());
            player.sendMessage(ChatColor.of("#ff5555") + "You can claim your next reward in: " + timeLeft);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (claimed.contains(clickedDay)) {
            player.sendMessage(ChatColor.of("#ff5555") + "You have already claimed this reward.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!canClaimDay(claimed, Integer.parseInt(clickedDay))) {
            player.sendMessage(ChatColor.of("#ff5555") + "Please claim previous days first.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        List<Map<?, ?>> itemList = config.getMapList("rewards." + clickedDay + ".items");
        for (Map<?, ?> itemData : itemList) {
            Object matObj = itemData.get("type");
            Object amountObj = itemData.get("amount");
            Object loreObj = itemData.get("lore");

            String typeName = (matObj != null) ? matObj.toString() : "DIAMOND";
            int amount = (amountObj != null) ? Integer.parseInt(amountObj.toString()) : 1;
            String lore = (loreObj != null) ? loreObj.toString() : "&7Daily Reward";

            Material mat = Material.matchMaterial(typeName);
            if (mat == null) continue;

            ItemStack rewardItem = new ItemStack(mat, amount);
            ItemMeta meta = rewardItem.getItemMeta();
            meta.setDisplayName(ChatColor.of("#00ffaa") + "Daily Reward");
            meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', lore)));
            rewardItem.setItemMeta(meta);

            player.getInventory().addItem(rewardItem);
        }

        claimed.add(clickedDay);
        lastClaimTime.put(uuid, now);
        player.sendMessage(ChatColor.of("#00ff00") + "You claimed your Day " + clickedDay + " reward!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().contains("Daily Rewards")) {
            // Optional cleanup
        }
    }
}
