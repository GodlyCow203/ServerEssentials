package net.godlycow.org.daily.api;

import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.daily.DailyCooldownInfo;
import com.serveressentials.api.daily.DailyReward;
import com.serveressentials.api.daily.DailyRewardItem;
import com.serveressentials.api.daily.event.DailyGUIOpenEvent;
import com.serveressentials.api.daily.event.DailyRewardClaimEvent;
import net.godlycow.org.EssC;
import net.godlycow.org.daily.DailyConfig;
import net.godlycow.org.daily.reward.DailyReward.RewardItem;
import net.godlycow.org.daily.storage.DailyStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class DailyAPIImpl implements DailyAPI {
    private static final String PERMISSION = "essc.command.daily";

    private final @NotNull EssC plugin;
    private final @NotNull DailyConfig config;
    private final @NotNull DailyStorage storage;
    private final @NotNull PlayerLanguageManager langManager;
    private final @NotNull Map<java.util.UUID, Integer> playerPages = new HashMap<>();

    // NEW: GUI identifier for language-agnostic detection
    private static final String GUI_IDENTIFIER_KEY = "daily_gui_identifier";
    private static final String GUI_IDENTIFIER_VALUE = "daily_rewards_gui";
    private static final String ITEM_TYPE_KEY = "daily_item_type";

    public DailyAPIImpl(@NotNull EssC plugin,
                        @NotNull DailyConfig config,
                        @NotNull DailyStorage storage,
                        @NotNull PlayerLanguageManager langManager) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.langManager = langManager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> openDailyGUI(@NotNull Player player, int page) {
        if (!player.hasPermission(PERMISSION)) {
            sendMessage(player, "commands.daily.command.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            return CompletableFuture.completedFuture(false);
        }

        playerPages.put(player.getUniqueId(), page);

        Component title = langManager.getMessageFor(player, "commands.daily.gui.title",
                "<green><bold>Daily Rewards</bold> - Page {page}",
                ComponentPlaceholder.of("{page}", String.valueOf(page)));

        Inventory gui = Bukkit.createInventory(null, config.guiRows * 9, title);

        return CompletableFuture.allOf(
                storage.getClaimedDays(player.getUniqueId()),
                storage.hasClaimedToday(player.getUniqueId(), config.cooldownHours)
        ).thenCompose(results -> {
            return storage.getClaimedDays(player.getUniqueId()).thenCompose(claimedDays -> {
                return storage.getTimeUntilNextClaim(player.getUniqueId(), config.cooldownHours)
                        .thenApply(duration -> {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    buildGUIContents(gui, player, page, claimedDays, duration);
                                    player.openInventory(gui);
                                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                                    Bukkit.getPluginManager().callEvent(new DailyGUIOpenEvent(player, page));
                                }
                            }.runTask(plugin);
                            return true;
                        });
            });
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to open daily GUI: " + ex.getMessage());
            return false;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> claimReward(@NotNull Player player, int day) {
        if (!player.hasPermission(PERMISSION)) {
            return CompletableFuture.completedFuture(false);
        }

        java.util.UUID uuid = player.getUniqueId();

        return storage.getClaimedDays(uuid).thenCompose(claimedDays -> {
            boolean isClaimed = claimedDays.contains(day);
            boolean canClaim = canClaimDay(claimedDays, day);

            if (isClaimed) {
                sendMessage(player, "commands.daily.messages.claim-already", "<red>You already claimed this reward!");
                return CompletableFuture.completedFuture(false);
            }

            if (!canClaim) {
                sendMessage(player, "commands.daily.messages.claim-locked", "<red>You must claim previous days first!");
                return CompletableFuture.completedFuture(false);
            }

            return storage.hasClaimedToday(uuid, config.cooldownHours).thenCompose(onCooldown -> {
                if (onCooldown) {
                    return storage.getTimeUntilNextClaim(uuid, config.cooldownHours).thenApply(duration -> {
                        sendMessage(player, "commands.daily.messages.claim-cooldown",
                                "<red>You must wait {time} before claiming again!",
                                ComponentPlaceholder.of("{time}", duration.format()));
                        return false;
                    });
                }

                RewardItem rewardItem = getRewardItem(day);
                if (rewardItem == null) {
                    return CompletableFuture.completedFuture(false);
                }

                return storage.claimReward(uuid, day).thenApply(v -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStack item = createRewardItem(rewardItem, player);
                            player.getInventory().addItem(item);

                            sendMessage(player, "commands.daily.messages.claim-success",
                                    "<green>You claimed Day {day} reward!",
                                    ComponentPlaceholder.of("{day}", String.valueOf(day)));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

                            DailyRewardItem dtoItem = new DailyRewardItem(
                                    rewardItem.material, rewardItem.amount, rewardItem.name,
                                    rewardItem.lore, rewardItem.enchantments, rewardItem.glow, rewardItem.nbt
                            );
                            DailyReward dtoReward = new DailyReward(day, 0, 0, List.of(dtoItem));
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                Bukkit.getPluginManager().callEvent(
                                        new DailyRewardClaimEvent(player, day, dtoReward)
                                );
                            });
                        }
                    }.runTask(plugin);
                    return true;
                }).exceptionally(ex -> {
                    sendMessage(player, "commands.daily.messages.claim-error", "<red>Failed to claim reward.");
                    return false;
                });
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<LocalDateTime>> getLastClaimTime(@NotNull java.util.UUID playerId) {
        return storage.getLastClaimTime(playerId);
    }

    @Override
    public @NotNull CompletableFuture<Set<Integer>> getClaimedDays(@NotNull java.util.UUID playerId) {
        return storage.getClaimedDays(playerId);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> hasClaimedToday(@NotNull java.util.UUID playerId) {
        return storage.hasClaimedToday(playerId, config.cooldownHours);
    }

    @Override
    public @NotNull CompletableFuture<DailyCooldownInfo> getTimeUntilNextClaim(@NotNull java.util.UUID playerId) {
        return storage.getTimeUntilNextClaim(playerId, config.cooldownHours)
                .thenApply(dto -> new DailyCooldownInfo(dto.hours(), dto.minutes(), dto.seconds(), dto.onCooldown()));
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            config.reload();
            plugin.getLogger().info("[ServerEssentials] Daily rewards configuration reloaded");
        });
    }

    @Override
    public boolean isDailyEnabled() {
        return plugin.getConfig().getBoolean("daily.enabled", true);
    }

    private void buildGUIContents(Inventory gui, Player player, int page,
                                  Set<Integer> claimedDays, DailyStorage.DurationInfo duration) {
        gui.clear();

        config.rewards.values().stream()
                .filter(reward -> reward.page == page)
                .forEach(reward -> {
                    ItemStack item = createRewardDisplayItem(reward.items.get(0), reward.day, claimedDays, player, duration);
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

    private ItemStack createRewardDisplayItem(RewardItem reward, int day, Set<Integer> claimedDays,
                                              Player player, DailyStorage.DurationInfo duration) {
        boolean isClaimed = claimedDays.contains(day);
        boolean canClaim = canClaimDay(claimedDays, day);
        boolean isOnCooldown = duration.onCooldown() && !isClaimed;

        ItemStack item;
        if (isClaimed) {
            item = createRewardItem(reward, player);
            applyMeta(item, player, "commands.daily.rewards.claimed.name", "<green><bold>✓ Claimed</bold>",
                    ComponentPlaceholder.of("lore", "daily.rewards.locked.lore|<gray>Complete previous days first."));
        } else if (isOnCooldown) {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            applyMeta(item, player, "commands.daily.rewards.cooldown.name", "<gold><bold>⏱ Cooldown</bold>",
                    ComponentPlaceholder.of("{time}", duration.format()));
        } else if (!canClaim) {
            item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            applyMeta(item, player, "commands.daily.rewards.locked.name", "<red><bold>🔒 Locked</bold>",
                    ComponentPlaceholder.of("lore", "commands.daily.rewards.locked.lore|<gray>Complete previous days first."));
        } else {
            item = createRewardItem(reward, player);
            applyMeta(item, player, "commands.daily.rewards.available.name", "<green>Day {day}",
                    ComponentPlaceholder.of("{day}", String.valueOf(day)),
                    ComponentPlaceholder.of("{time}", config.cooldownHours + "h"));
        }

        return item;
    }

    private ItemStack createRewardItem(RewardItem itemData, Player player) {
        ItemStack item = new ItemStack(itemData.material, itemData.amount);
        ItemMeta meta = item.getItemMeta();

        if (!itemData.name.isEmpty()) {
            meta.displayName(langManager.getMessageFor(player, itemData.name, itemData.name));
        }

        if (!itemData.lore.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            itemData.lore.forEach(line ->
                    lore.add(langManager.getMessageFor(player, line, line))
            );
            meta.lore(lore);
        }

        itemData.enchantments.forEach((enchantName, level) -> {
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

    private void applyMeta(ItemStack item, Player player, String key, String def,
                           ComponentPlaceholder... placeholders) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(langManager.getMessageFor(player, key, def, placeholders));

        for (ComponentPlaceholder ph : placeholders) {
            if ("lore".equals(ph.placeholder())) {
                String[] parts = ph.value().split("\\|", 2);
                if (parts.length == 2) {
                    meta.lore(List.of(langManager.getMessageFor(player, parts[0], parts[1])));
                }
                break;
            }
        }

        item.setItemMeta(meta);
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

    private RewardItem getRewardItem(int day) {
        net.godlycow.org.daily.reward.DailyReward reward = config.rewards.get(day);
        return reward != null && !reward.items.isEmpty() ? reward.items.get(0) : null;
    }

    private void sendMessage(Player player, String key, String def, ComponentPlaceholder... placeholders) {
        player.sendMessage(langManager.getMessageFor(player, key, def, placeholders));
    }
}