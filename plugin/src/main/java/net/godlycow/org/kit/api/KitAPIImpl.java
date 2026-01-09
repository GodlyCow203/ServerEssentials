package net.godlycow.org.kit.api;

import com.serveressentials.api.kit.KitAPI;
import com.serveressentials.api.kit.KitClaimData;
import com.serveressentials.api.kit.KitInfo;
import com.serveressentials.api.kit.KitItem;
import com.serveressentials.api.kit.event.KitClaimEvent;
import com.serveressentials.api.kit.event.KitOpenGUIEvent;
import com.serveressentials.api.kit.event.KitOpenPreviewEvent;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.commands.config.KitConfig;
import net.godlycow.org.kit.KitConfigManager;
import net.godlycow.org.kit.KitManager;
import net.godlycow.org.kit.model.Kit;
import net.godlycow.org.kit.permission.KitPermission;
import net.godlycow.org.kit.storage.KitStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class KitAPIImpl implements KitAPI {
    private final @NotNull ServerEssentials plugin;
    private final @NotNull KitManager kitManager;
    private final @NotNull KitConfigManager kitConfigManager;
    private final @NotNull KitStorage kitStorage;
    private final @NotNull KitConfig kitConfig;
    private final @NotNull PlayerLanguageManager langManager;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public KitAPIImpl(
            @NotNull ServerEssentials plugin,
            @NotNull KitManager kitManager,
            @NotNull KitConfigManager kitConfigManager,
            @NotNull KitStorage kitStorage,
            @NotNull KitConfig kitConfig,
            @NotNull PlayerLanguageManager langManager
    ) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.kitConfigManager = kitConfigManager;
        this.kitStorage = kitStorage;
        this.kitConfig = kitConfig;
        this.langManager = langManager;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> claimKit(@NotNull Player player, @NotNull String kitId) {
        return CompletableFuture.supplyAsync(() -> {
            String normalizedKitId = kitId.toLowerCase();
            Kit kit = kitManager.getKit(normalizedKitId);

            if (kit == null) {
                return false;
            }

            String permission = KitPermission.node(normalizedKitId);
            if (!player.hasPermission(permission)) {
                return false;
            }

            int cooldownSeconds = kit.getCooldown();
            if (kitStorage.isOnCooldown(player.getUniqueId(), normalizedKitId, cooldownSeconds)) {
                return false;
            }

            for (org.bukkit.inventory.ItemStack item : kit.getItems()) {
                if (item != null) {
                    player.getInventory().addItem(item);
                }
            }

            kitStorage.saveKitClaim(player.getUniqueId(), normalizedKitId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new KitClaimEvent(player, normalizedKitId, kit.getDisplayName()));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> openKitGUI(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            kitStorage.loadIntoCache(player.getUniqueId());

            List<Kit> kits = kitManager.getKits().values().stream()
                    .filter(kit -> {
                        String perm = KitPermission.node(kit.getName());
                        return kit.getPermission() == null ||
                                kit.getPermission().isEmpty() ||
                                player.hasPermission(perm);
                    })
                    .collect(Collectors.toList());

            Bukkit.getScheduler().runTask(plugin, () -> {
                Component titleComponent = langManager.getMessageFor(player, "kits.gui.title", kitConfig.getGuiTitle());
                String legacyTitle = LEGACY_SERIALIZER.serialize(titleComponent);
                Inventory gui = Bukkit.createInventory(null, kitConfig.getGuiSize(), legacyTitle);

                int slotIndex = 0;
                for (Kit kit : kits) {
                    int slot = kit.getSlot() > 0 ? kit.getSlot() : slotIndex;
                    if (slot >= gui.getSize()) slot = slotIndex;
                    slotIndex = Math.max(slot + 1, slotIndex + 1);

                    ItemStack icon = createKitIcon(kit, player);
                    gui.setItem(slot, icon);
                }

                player.openInventory(gui);

                Bukkit.getPluginManager().callEvent(new KitOpenGUIEvent(player, kits.size()));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> openKitPreview(@NotNull Player player, @NotNull String kitId) {
        return CompletableFuture.supplyAsync(() -> {
            String normalizedKitId = kitId.toLowerCase();
            Kit kit = kitManager.getKit(normalizedKitId);

            if (kit == null) {
                return false;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Component titleComponent = langManager.getMessageFor(player, "kits.gui.preview-title",
                        kitConfig.getPreviewTitle(),
                        LanguageManager.ComponentPlaceholder.of("{kit}", kitId));
                String legacyTitle = LEGACY_SERIALIZER.serialize(titleComponent);
                Inventory preview = Bukkit.createInventory(null, 54, legacyTitle);

                List<org.bukkit.inventory.ItemStack> items = kit.getItems();
                for (int i = 0; i < items.size() && i < 45; i++) {
                    preview.setItem(i, items.get(i));
                }

                preview.setItem(53, createClaimButton(player, normalizedKitId));
                preview.setItem(45, createBackButton(player));

                player.openInventory(preview);

                Bukkit.getPluginManager().callEvent(new KitOpenPreviewEvent(player, normalizedKitId, kit.getDisplayName()));
            });

            return true;
        });
    }

    @Override
    public @NotNull CompletableFuture<List<KitInfo>> getAvailableKits(@NotNull Player player) {
        return CompletableFuture.supplyAsync(() -> {
            return kitManager.getKits().values().stream()
                    .map(this::convertToKitInfo)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public @NotNull CompletableFuture<Long> getRemainingCooldown(@NotNull Player player, @NotNull String kitId) {
        return CompletableFuture.supplyAsync(() -> {
            String normalizedKitId = kitId.toLowerCase();
            Kit kit = kitManager.getKit(normalizedKitId);

            if (kit == null) {
                return 0L;
            }

            int cooldownSeconds = kit.getCooldown();
            return kitStorage.getRemainingCooldown(player.getUniqueId(), normalizedKitId, cooldownSeconds);
        });
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("kits.enabled", true);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
            plugin.reloadConfig();
            kitConfig.load();
            kitConfigManager.reload();
            plugin.getLogger().info("[ServerEssentials] Kit configuration reloaded: " +
                    kitManager.getKits().size() + " kits loaded.");
        });
    }

    private @NotNull ItemStack createKitIcon(@NotNull Kit kit, @NotNull Player player) {
        String permission = KitPermission.node(kit.getName());
        boolean hasPermission = kit.getPermission() == null ||
                kit.getPermission().isEmpty() ||
                player.hasPermission(permission);

        Material material = hasPermission ? kit.getDisplayMaterial() : Material.BARRIER;
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        Component name = hasPermission
                ? miniMessage.deserialize(kit.getDisplayName())
                : langManager.getMessageFor(player, "kits.locked-name",
                "<red>Locked: <gray>{kit}",
                LanguageManager.ComponentPlaceholder.of("{kit}", kit.getName()));
        meta.setDisplayName(LEGACY_SERIALIZER.serialize(name));

        List<Component> lore = new ArrayList<>();
        for (String line : kit.getDisplayLore()) {
            lore.add(miniMessage.deserialize(line));
        }

        if (!hasPermission) {
            lore.add(langManager.getMessageFor(player, "kits.requires-permission",
                    "<gray>Requires: <white>{permission}",
                    LanguageManager.ComponentPlaceholder.of("{permission}", permission)));
        }

        int cooldown = kit.getCooldown();
        if (kitStorage.isOnCooldown(player.getUniqueId(), kit.getName(), cooldown)) {
            long remaining = kitStorage.getRemainingCooldown(player.getUniqueId(), kit.getName(), cooldown);
            lore.add(langManager.getMessageFor(player, "kits.cooldown-info",
                    "<gray>Cooldown: <red>{time}s",
                    LanguageManager.ComponentPlaceholder.of("{time}", remaining)));
        }

        meta.setLore(lore.stream().map(LEGACY_SERIALIZER::serialize).collect(Collectors.toList()));

        NamespacedKey key = new NamespacedKey(plugin, "kit_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kit.getName());

        icon.setItemMeta(meta);
        return icon;
    }

    private @NotNull ItemStack createClaimButton(@NotNull Player player, @NotNull String kitId) {
        Kit kit = kitManager.getKit(kitId);
        int cooldown = kit != null ? kit.getCooldown() : 0;

        ItemStack item;
        ItemMeta meta;

        if (kitStorage.isOnCooldown(player.getUniqueId(), kitId, cooldown)) {
            item = new ItemStack(kitConfig.getCooldownButtonMaterial());
            meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(LEGACY_SERIALIZER.serialize(
                        langManager.getMessageFor(player, "kits.cooldown-button", "<red>⏱ On Cooldown")
                ));
            }
        } else {
            item = new ItemStack(kitConfig.getClaimButtonMaterial());
            meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(LEGACY_SERIALIZER.serialize(
                        langManager.getMessageFor(player, "kits.claim-button", "<green>✓ Claim Kit")
                ));
            }
        }

        if (meta != null) {
            NamespacedKey key = new NamespacedKey(plugin, "kit_action");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "claim:" + kitId);
            item.setItemMeta(meta);
        }

        return item;
    }

    private @NotNull ItemStack createBackButton(@NotNull Player player) {
        ItemStack item = new ItemStack(kitConfig.getBackButtonMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LEGACY_SERIALIZER.serialize(
                    langManager.getMessageFor(player, "kits.back-button", "<red>← Back")
            ));

            NamespacedKey key = new NamespacedKey(plugin, "kit_action");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "back");

            item.setItemMeta(meta);
        }
        return item;
    }

    private @NotNull KitInfo convertToKitInfo(@NotNull Kit kit) {
        return new KitInfo(
                kit.getName(),
                kit.getDisplayName(),
                kit.getPermission(),
                kit.getDisplayName(),
                kit.getDisplayMaterial().name(),
                kit.getDisplayLore(),
                kit.getSlot(),
                kit.getCooldown(),
                kit.getItems().stream()
                        .map(this::convertToKitItem)
                        .collect(Collectors.toList())
        );
    }

    private @NotNull KitItem convertToKitItem(@NotNull ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        return new KitItem(
                itemStack.getType().name(),
                itemStack.getAmount(),
                meta != null && meta.hasDisplayName() ? meta.getDisplayName() : null,
                meta != null && meta.hasLore() ? meta.getLore() : null,
                null
        );
    }
}