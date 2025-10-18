package serveressentials.serveressentials.Daily;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import serveressentials.serveressentials.util.DailyMessagesManager;
import serveressentials.serveressentials.util.DailyRewardsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class DailyRewards implements Listener {

    private final JavaPlugin plugin;
    private final DailyMessagesManager messagesManager;
    private final DailyRewardsManager rewardsManager;
    private final Map<UUID, Set<String>> claimedRewards = new HashMap<>();
    private final Map<UUID, LocalDateTime> lastClaimTime = new HashMap<>();
    private final int cooldownHours = 24;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DailyRewards(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messagesManager = new DailyMessagesManager(plugin);
        this.rewardsManager = new DailyRewardsManager(plugin);

        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) pluginFolder.mkdirs();

        File configFolder = new File(pluginFolder, "config/Daily");
        if (!configFolder.exists()) configFolder.mkdirs();

        File configFile = new File(configFolder, "daily.yml");
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("config/Daily/daily.yml")) {
                if (in != null) java.nio.file.Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy daily.yml!");
                e.printStackTrace();
            }
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public DailyMessagesManager getMessagesManager() {
        return messagesManager;
    }

    public void openRewardsGUI(Player player, int page) {
        Component guiTitle = miniMessage.deserialize(messagesManager.get("gui-title", "%page%", String.valueOf(page)));
        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

        List<String> keys = rewardsManager.getRewardDays();
        int totalPages = 1;
        for (String day : keys) {
            totalPages = Math.max(totalPages, rewardsManager.getRewardPage(day));
        }

        UUID uuid = player.getUniqueId();
        Set<String> claimed = claimedRewards.getOrDefault(uuid, new HashSet<>());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastClaim = lastClaimTime.getOrDefault(uuid, LocalDateTime.MIN);
        Duration cooldownLeft = Duration.ofHours(cooldownHours).minus(Duration.between(lastClaim, now));
        boolean onCooldown = !cooldownLeft.isNegative();

        for (String day : keys) {
            int rewardPage = rewardsManager.getRewardPage(day);
            if (rewardPage != page) continue;

            int slot = rewardsManager.getRewardSlot(day);
            if (slot < 0 || slot > 53) continue;

            List<Map<String, Object>> itemList = rewardsManager.getRewardItems(day);
            if (itemList.isEmpty()) continue;

            Map<String, Object> rewardItemData = itemList.get(0);
            Material material = Material.matchMaterial((String) rewardItemData.getOrDefault("type", "DIAMOND"));
            if (material == null) material = Material.DIAMOND;

            ItemStack displayItem;
            ItemMeta meta;

            if (claimed.contains(day)) {
                displayItem = new ItemStack(Material.LIME_CONCRETE);
                meta = displayItem.getItemMeta();
                meta.displayName(miniMessage.deserialize(messagesManager.get("reward-claimed-name")));
                meta.lore(Collections.singletonList(miniMessage.deserialize(messagesManager.get("reward-claimed-lore"))));
            } else if (onCooldown || !canClaimDay(claimed, Integer.parseInt(day))) {
                displayItem = new ItemStack(Material.RED_CONCRETE);
                meta = displayItem.getItemMeta();
                meta.displayName(miniMessage.deserialize(messagesManager.get("reward-locked-name")));
                String loreMsg = onCooldown
                        ? messagesManager.get("reward-locked-cooldown", "%time%", formatDuration(cooldownLeft))
                        : messagesManager.get("reward-locked-previous");
                meta.lore(Collections.singletonList(miniMessage.deserialize(loreMsg)));
            } else {
                displayItem = new ItemStack(material);
                meta = displayItem.getItemMeta();
                meta.displayName(miniMessage.deserialize(messagesManager.get("reward-name", "%day%", day)));
                meta.lore(Collections.singletonList(miniMessage.deserialize((String) rewardItemData.getOrDefault("lore", "&7Click to claim!"))));
            }

            displayItem.setItemMeta(meta);
            gui.setItem(slot, displayItem);
        }

        if (page > 1) gui.setItem(45, navItem(Material.ARROW, miniMessage.deserialize(messagesManager.get("previous-page"))));
        if (page < totalPages) gui.setItem(53, navItem(Material.ARROW, miniMessage.deserialize(messagesManager.get("next-page"))));
        gui.setItem(49, navItem(Material.BARRIER, miniMessage.deserialize(messagesManager.get("close-button"))));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }

    private boolean canClaimDay(Set<String> claimed, int day) {
        for (int i = 1; i < day; i++) {
            if (!claimed.contains(String.valueOf(i))) return false;
        }
        return true;
    }

    private ItemStack navItem(Material mat, Component name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private String formatDuration(Duration d) {
        return String.format("%02dh %02dm %02ds", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String guiTitleText = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        String expectedTitleText = PlainTextComponentSerializer.plainText().serialize(
                miniMessage.deserialize(messagesManager.get("gui-title-placeholder"))
        );
        if (!guiTitleText.contains(expectedTitleText)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.6f);
            return;
        }

        if (clicked.getType() == Material.ARROW) {
            String clickedName = PlainTextComponentSerializer.plainText().serialize(clicked.getItemMeta().displayName());
            String prevText = PlainTextComponentSerializer.plainText().serialize(
                    miniMessage.deserialize(messagesManager.get("previous-page"))
            );
            int newPage = clickedName.contains(prevText) ? 1 : 2;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    openRewardsGUI(player, newPage);
                }
            }.runTaskLater(plugin, 1);
            return;
        }

        UUID uuid = player.getUniqueId();
        Set<String> claimed = claimedRewards.computeIfAbsent(uuid, k -> new HashSet<>());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastClaim = lastClaimTime.getOrDefault(uuid, LocalDateTime.MIN);
        Duration cooldownLeft = Duration.ofHours(cooldownHours).minus(Duration.between(lastClaim, now));

        if (!cooldownLeft.isNegative()) {
            player.sendMessage(miniMessage.deserialize(messagesManager.get("claim-cooldown", "%time%", formatDuration(cooldownLeft))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        int clickedSlot = e.getSlot();
        String clickedDay = null;
        for (String day : rewardsManager.getRewardDays()) {
            if (rewardsManager.getRewardSlot(day) == clickedSlot) {
                clickedDay = day;
                break;
            }
        }
        if (clickedDay == null) return;

        if (claimed.contains(clickedDay)) {
            player.sendMessage(miniMessage.deserialize(messagesManager.get("claim-already")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!canClaimDay(claimed, Integer.parseInt(clickedDay))) {
            player.sendMessage(miniMessage.deserialize(messagesManager.get("claim-previous")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        List<Map<String, Object>> itemList = rewardsManager.getRewardItems(clickedDay);
        for (Map<String, Object> itemData : itemList) {
            String typeName = itemData.getOrDefault("type", "DIAMOND").toString().toUpperCase();
            Material mat = Material.matchMaterial(typeName);
            if (mat == null) continue;

            int amount = 1;
            Object amtObj = itemData.get("amount");
            if (amtObj instanceof Number) amount = ((Number) amtObj).intValue();
            else if (amtObj != null) {
                try { amount = Integer.parseInt(amtObj.toString()); } catch (NumberFormatException ignored) {}
            }

            String lore = itemData.getOrDefault("lore", "&7Daily Reward").toString();

            ItemStack item = new ItemStack(mat, amount);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(miniMessage.deserialize(messagesManager.get("reward-name", "%day%", clickedDay)));
            meta.lore(Collections.singletonList(miniMessage.deserialize(lore)));
            item.setItemMeta(meta);

            player.getInventory().addItem(item);
        }

        claimed.add(clickedDay);
        lastClaimTime.put(uuid, now);

        player.sendMessage(miniMessage.deserialize(messagesManager.get("claim-success", "%day%", clickedDay)));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String guiTitleText = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        String expectedTitleText = PlainTextComponentSerializer.plainText().serialize(miniMessage.deserialize(messagesManager.get("gui-title-placeholder")));
        if (guiTitleText.contains(expectedTitleText)) {
        }
    }
}
