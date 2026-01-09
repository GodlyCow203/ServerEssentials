package net.godlycow.org.daily.trigger;

import net.godlycow.org.daily.DailyConfig;
import net.godlycow.org.daily.reward.DailyReward;
import net.godlycow.org.daily.storage.DailyStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DailyListener implements Listener {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final DailyStorage storage;
    private final DailyConfig config;
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    private static final String GUI_NAMESPACE = "daily_gui";
    private static final String REWARD_ID_KEY = "daily_reward_day";
    private static final String ITEM_TYPE_KEY = "daily_item_type";
    private static final String GUI_IDENTIFIER_KEY = "daily_gui_identifier";
    private static final String GUI_IDENTIFIER_VALUE = "daily_rewards_gui";

    public DailyListener(JavaPlugin plugin, PlayerLanguageManager langManager,
                         DailyStorage storage, DailyConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    public void openRewardsGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);

        Component title = langManager.getMessageFor(player, "commands.daily.gui.title",
                "<green><bold>Daily Rewards</bold> - Page {page}",
                LanguageManager.ComponentPlaceholder.of("{page}", page));

        Inventory gui = Bukkit.createInventory(null, config.guiRows * 9, title);

        CompletableFuture.allOf(
                storage.getClaimedDays(player.getUniqueId()),
                storage.hasClaimedToday(player.getUniqueId(), config.cooldownHours)
        ).thenAccept((results) -> {
            Set<Integer> claimedDays = new HashSet<>();
            boolean[] onCooldown = {false};

            storage.getClaimedDays(player.getUniqueId()).thenAccept(days -> {
                claimedDays.addAll(days);

                storage.getTimeUntilNextClaim(player.getUniqueId(), config.cooldownHours).thenAccept(duration -> {
                    onCooldown[0] = duration.onCooldown();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            buildGUIContents(gui, player, page, claimedDays, duration);
                            player.openInventory(gui);
                            player.playSound(player.getLocation(),
                                    org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                        }
                    }.runTask(plugin);
                });
            });
        });
    }

    private void buildGUIContents(Inventory gui, Player player, int page,
                                  Set<Integer> claimedDays, DailyStorage.DurationInfo duration) {
        gui.clear();

        config.rewards.values().stream()
                .filter(reward -> reward.page == page)
                .forEach(reward -> {
                    ItemStack item = createRewardDisplayItem(reward, claimedDays, player, duration);
                    gui.setItem(reward.slot, item);
                });

        if (page > 1) {
            gui.setItem(45, createNavItem(Material.ARROW, "commands.daily.gui.previous",
                    "<yellow>Previous Page", player, "NAV_PREVIOUS"));
        }
        if (hasNextPage(page)) {
            gui.setItem(53, createNavItem(Material.ARROW, "commands.daily.gui.next",
                    "<yellow>Next Page", player, "NAV_NEXT"));
        }

        gui.setItem(config.guiRows * 9 - 5, createNavItem(Material.BARRIER, "commands.daily.gui.close",
                "<red>Close", player, "CLOSE"));
    }

    private ItemStack createRewardDisplayItem(DailyReward reward, Set<Integer> claimedDays,
                                              Player player, DailyStorage.DurationInfo duration) {
        UUID uuid = player.getUniqueId();
        boolean isClaimed = claimedDays.contains(reward.day);
        boolean isUnlocked = canClaimDay(claimedDays, reward.day);
        boolean isOnCooldown = duration.onCooldown() && !isClaimed;

        ItemStack item;
        ItemMeta meta;

        if (isClaimed) {
            item = createActualRewardItem(reward.items.get(0), player);
            meta = item.getItemMeta();

            Component name = langManager.getMessageFor(player, "commands.daily.rewards.claimed.name",
                    "<green><bold>✓ Claimed</bold>");
            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(langManager.getMessageFor(player, "commands.daily.rewards.claimed.lore",
                    "<gray>You have claimed this reward."));

            meta.lore(lore);

            if (reward.items.get(0).glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        } else if (isOnCooldown) {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();

            Component name = langManager.getMessageFor(player, "commands.daily.rewards.cooldown.name",
                    "<gold><bold>⏱ Cooldown</bold>");
            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(langManager.getMessageFor(player, "commands.daily.rewards.cooldown.lore",
                    "<gray>Wait: <gold>{time}",
                    LanguageManager.ComponentPlaceholder.of("{time}", duration.format())));

            meta.lore(lore);
        } else if (!isUnlocked) {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            meta = item.getItemMeta();

            Component name = langManager.getMessageFor(player, "commands.daily.rewards.locked.name",
                    "<red><bold>🔒 Locked</bold>");
            meta.displayName(name);

            meta.lore(List.of(
                    langManager.getMessageFor(player, "commands.daily.rewards.locked.lore",
                            "<gray>Complete previous days first.")
            ));
        } else {
            item = createActualRewardItem(reward.items.get(0), player);
            meta = item.getItemMeta();

            Component name = langManager.getMessageFor(player, "commands.daily.rewards.available.name",
                    "<green>Day {day}",
                    LanguageManager.ComponentPlaceholder.of("{day}", reward.day));
            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());

            reward.items.get(0).lore.forEach(line ->
                    lore.add(langManager.getMessageFor(player, line, line))
            );

            lore.add(Component.empty());
            lore.add(langManager.getMessageFor(player, "commands.daily.gui.cooldown-display",
                    "<gray>Cooldown: <yellow>{time}",
                    LanguageManager.ComponentPlaceholder.of("{time}", config.cooldownHours + "h")));

            meta.lore(lore);

            if (reward.items.get(0).glow) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new org.bukkit.NamespacedKey(plugin, REWARD_ID_KEY),
                PersistentDataType.INTEGER, reward.day);
        container.set(new org.bukkit.NamespacedKey(plugin, ITEM_TYPE_KEY),
                PersistentDataType.STRING, "REWARD");

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createActualRewardItem(DailyReward.RewardItem rewardItem, Player player) {
        ItemStack item = new ItemStack(rewardItem.material, rewardItem.amount);
        ItemMeta meta = item.getItemMeta();

        if (!rewardItem.name.isEmpty()) {
            meta.displayName(langManager.getMessageFor(player, rewardItem.name, rewardItem.name));
        }

        if (!rewardItem.lore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            rewardItem.lore.forEach(line ->
                    lore.add(langManager.getMessageFor(player, line, line))
            );
            meta.lore(lore);
        }

        rewardItem.enchantments.forEach((enchantName, level) -> {
            org.bukkit.enchantments.Enchantment enchant =
                    org.bukkit.enchantments.Enchantment.getByName(enchantName.toUpperCase());
            if (enchant != null) {
                meta.addEnchant(enchant, level, true);
            }
        });

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(Material material, String messageKey, String def,
                                    Player player, String itemType) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(langManager.getMessageFor(player, messageKey, def));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new org.bukkit.NamespacedKey(plugin, ITEM_TYPE_KEY),
                PersistentDataType.STRING, itemType);

        if ("CLOSE".equals(itemType)) {
            container.set(new org.bukkit.NamespacedKey(plugin, GUI_IDENTIFIER_KEY),
                    PersistentDataType.STRING, GUI_IDENTIFIER_VALUE);
        }

        item.setItemMeta(meta);
        return item;
    }

    private boolean canClaimDay(Set<Integer> claimedDays, int day) {
        for (int i = 1; i < day; i++) {
            if (!claimedDays.contains(i)) return false;
        }
        return true;
    }

    private boolean hasNextPage(int currentPage) {
        return config.rewards.values().stream().anyMatch(r -> r.page > currentPage);
    }

    private boolean isDailyRewardsGUI(Inventory inventory) {
        if (inventory == null) return false;

        int closeButtonSlot = config.guiRows * 9 - 5;
        if (closeButtonSlot >= inventory.getSize()) return false;

        ItemStack closeButton = inventory.getItem(closeButtonSlot);
        if (closeButton == null || closeButton.getType() != Material.BARRIER) return false;

        ItemMeta meta = closeButton.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String identifier = container.get(
                new org.bukkit.NamespacedKey(plugin, GUI_IDENTIFIER_KEY),
                PersistentDataType.STRING
        );

        return GUI_IDENTIFIER_VALUE.equals(identifier);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (!isDailyRewardsGUI(e.getInventory())) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String itemType = container.get(
                new org.bukkit.NamespacedKey(plugin, ITEM_TYPE_KEY),
                PersistentDataType.STRING
        );

        if (itemType == null) return;

        switch (itemType) {
            case "REWARD":
                Integer rewardDay = container.get(
                        new org.bukkit.NamespacedKey(plugin, REWARD_ID_KEY),
                        PersistentDataType.INTEGER
                );
                if (rewardDay != null) {
                    handleRewardClaim(player, rewardDay, e.getSlot());
                }
                break;

            case "NAV_PREVIOUS":
                handleNavigation(player, -1);
                break;

            case "NAV_NEXT":
                handleNavigation(player, 1);
                break;

            case "CLOSE":
                player.closeInventory();
                break;
        }
    }

    private void handleNavigation(Player player, int direction) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        int newPage = currentPage + direction;

        if (newPage < 1) return;
        if (direction > 0 && !hasNextPage(currentPage)) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                openRewardsGUI(player, newPage);
            }
        }.runTaskLater(plugin, 1);
    }

    private void handleRewardClaim(Player player, int day, int slot) {
        UUID uuid = player.getUniqueId();

        CompletableFuture.allOf(
                storage.getClaimedDays(uuid),
                storage.hasClaimedToday(uuid, config.cooldownHours)
        ).thenCompose(results -> {
            Set<Integer> claimedDays = new HashSet<>();

            return storage.getClaimedDays(uuid).thenCompose(days -> {
                claimedDays.addAll(days);

                boolean isClaimed = claimedDays.contains(day);
                boolean canClaim = canClaimDay(claimedDays, day);

                if (isClaimed) {
                    return CompletableFuture.runAsync(() -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage(langManager.getMessageFor(player, "commands.daily.messages.claim-already",
                                        "<red>You already claimed this reward!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                        }.runTask(plugin);
                    });
                }

                if (!canClaim) {
                    return CompletableFuture.runAsync(() -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage(langManager.getMessageFor(player, "commands.daily.messages.claim-locked",
                                        "<red>You must claim previous days first!"));
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                        }.runTask(plugin);
                    });
                }

                return storage.hasClaimedToday(uuid, config.cooldownHours).thenCompose(onCooldown -> {
                    if (onCooldown) {
                        return storage.getTimeUntilNextClaim(uuid, config.cooldownHours).thenAccept(duration -> {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(langManager.getMessageFor(player, "commands.daily.messages.claim-cooldown",
                                            "<red>You must wait {time} before claiming again!",
                                            LanguageManager.ComponentPlaceholder.of("{time}", duration.format())));
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                }
                            }.runTask(plugin);
                        });
                    }

                    return claimReward(player, day, uuid);
                });
            });
        });
    }

    private CompletableFuture<Void> claimReward(Player player, int day, UUID uuid) {
        DailyReward reward = config.rewards.get(day);
        if (reward == null) return CompletableFuture.completedFuture(null);

        return storage.claimReward(uuid, day).thenAccept(v -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (DailyReward.RewardItem itemData : reward.items) {
                        ItemStack item = createActualRewardItem(itemData, player);
                        player.getInventory().addItem(item);
                    }

                    player.sendMessage(langManager.getMessageFor(player, "commands.daily.messages.claim-success",
                            "<green>You claimed Day {day} reward!",
                            LanguageManager.ComponentPlaceholder.of("{day}", day)));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            openRewardsGUI(player, playerPages.getOrDefault(uuid, 1));
                        }
                    }.runTaskLater(plugin, 1);
                }
            }.runTask(plugin);
        }).exceptionally(ex -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(langManager.getMessageFor(player, "commands.daily.messages.claim-error",
                            "<red>Failed to claim reward. Please try again."));
                }
            }.runTask(plugin);
            return null;
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        if (isDailyRewardsGUI(e.getInventory())) {
            playerPages.remove(player.getUniqueId());
        }
    }
}