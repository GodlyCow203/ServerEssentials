package net.godlycow.org.kit.trigger;

import net.godlycow.org.commands.config.KitConfig;
import net.godlycow.org.kit.KitConfigManager;
import net.godlycow.org.kit.KitManager;
import net.godlycow.org.kit.model.Kit;
import net.godlycow.org.kit.permission.KitPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.kit.storage.KitStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class KitGUIListener implements Listener {

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final KitStorage kitStorage;
    private final KitConfig kitConfig;
    private static final String GUI_TITLE_KEY = "kits.gui.title";
    private static final String PREVIEW_TITLE_KEY = "kits.gui.preview-title";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public KitGUIListener(Plugin plugin, PlayerLanguageManager langManager, KitStorage kitStorage, KitConfig kitConfig) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.kitStorage = kitStorage;
        this.kitConfig = kitConfig;
    }

    public void openKitGUI(Player player) {
        kitStorage.loadIntoCache(player.getUniqueId());

        Component title = langManager.getMessageFor(player, GUI_TITLE_KEY, kitConfig.getGuiTitle());
        Inventory gui = Bukkit.createInventory(null, kitConfig.getGuiSize(), title);

        ConfigurationSection kitsSec = KitConfigManager.getConfig().getConfigurationSection("kits");
        if (kitsSec == null) {
            player.sendMessage(langManager.getMessageFor(player, "kits.no-kits-defined",
                    "<red>No kits are currently defined!"));
            return;
        }

        int slotIndex = 0;
        for (String kitId : kitsSec.getKeys(false)) {
            Kit kit = KitManager.getKit(kitId);
            if (kit == null) continue;

            boolean hasPermission = kit.getPermission() == null ||
                    kit.getPermission().isEmpty() ||
                    player.hasPermission(kit.getPermission());

            int slot = kit.getSlot() > 0 ? kit.getSlot() : slotIndex;
            if (slot >= gui.getSize()) slot = slotIndex;
            slotIndex = Math.max(slot + 1, slotIndex + 1);

            ItemStack icon = createKitIcon(kit, player, hasPermission);
            gui.setItem(slot, icon);
        }

        player.openInventory(gui);
    }

    private ItemStack createKitIcon(Kit kit, Player player, boolean hasPermission) {
        ItemStack icon = new ItemStack(hasPermission ? kit.getDisplayMaterial() : Material.BARRIER);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        Component name = hasPermission
                ? miniMessage.deserialize(kit.getDisplayName())
                : langManager.getMessageFor(player, "kits.locked-name",
                "<red>Locked: <gray>{kit}",
                LanguageManager.ComponentPlaceholder.of("{kit}", kit.getName()));
        meta.displayName(name);
        List<Component> lore = new ArrayList<>();
        for (String line : kit.getDisplayLore()) {
            lore.add(miniMessage.deserialize(line));
        }

        if (!hasPermission) {
            lore.add(langManager.getMessageFor(player, "kits.requires-permission",
                    "<gray>Requires: <white>{permission}",
                    LanguageManager.ComponentPlaceholder.of("{permission}",
                            KitPermission.node(kit.getName()))));
        }

        long remaining = kitStorage.getRemainingCooldown(player.getUniqueId(), kit.getName(),
                KitConfigManager.getConfig().getInt("kits." + kit.getName() + ".cooldown", 0));
        if (remaining > 0) {
            lore.add(langManager.getMessageFor(player, "kits.cooldown-info",
                    "<gray>Cooldown: <red>{time}s",
                    LanguageManager.ComponentPlaceholder.of("{time}", remaining)));
        }

        meta.lore(lore);

        NamespacedKey key = new NamespacedKey(plugin, "kit_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, kit.getName());

        icon.setItemMeta(meta);
        return icon;
    }

    public void openKitPreview(Player player, String kitId) {
        Kit kit = KitManager.getKit(kitId);
        if (kit == null) {
            player.sendMessage(langManager.getMessageFor(player, "kits.kit-not-found",
                    kitConfig.getKitNotFoundMessage(),
                    LanguageManager.ComponentPlaceholder.of("{kit}", kitId)));
            return;
        }

        Component title = langManager.getMessageFor(player, PREVIEW_TITLE_KEY, kitConfig.getPreviewTitle(),
                LanguageManager.ComponentPlaceholder.of("{kit}", kitId));

        Inventory preview = Bukkit.createInventory(null, 54, title);

        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 45; i++) {
            preview.setItem(i, items.get(i));
        }

        ItemStack claimButton = createClaimButton(player, kitId);
        preview.setItem(53, claimButton);

        ItemStack backButton = createBackButton(player);
        preview.setItem(45, backButton);

        player.openInventory(preview);
    }

    private ItemStack createClaimButton(Player player, String kitId) {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(langManager.getMessageFor(player, "kits.claim-button",
                "<green>✓ Claim Kit"));

        int cooldown = KitConfigManager.getConfig().getInt("kits." + kitId + ".cooldown", 0);
        if (kitStorage.isOnCooldown(player.getUniqueId(), kitId, cooldown)) {
            item.setType(Material.RED_CONCRETE);
            meta.displayName(langManager.getMessageFor(player, "kits.cooldown-button",
                    "<red>⏱ On Cooldown"));
        }

        NamespacedKey key = new NamespacedKey(plugin, "kit_action");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "claim:" + kitId);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackButton(Player player) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(langManager.getMessageFor(player, "kits.back-button",
                    "<red>← Back"));

            NamespacedKey key = new NamespacedKey(plugin, "kit_action");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "back");

            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String mainTitle = langManager.getMessageFor(player, GUI_TITLE_KEY, kitConfig.getGuiTitle()).toString();
        String previewTitle = langManager.getMessageFor(player, PREVIEW_TITLE_KEY, kitConfig.getPreviewTitle()).toString();

        String clickedTitle = event.getView().title().toString();
        boolean isMainGui = clickedTitle.equals(mainTitle);
        boolean isPreviewGui = clickedTitle.contains("Preview Kit:") || clickedTitle.equals(previewTitle);

        if (!isMainGui && !isPreviewGui) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        NamespacedKey actionKey = new NamespacedKey(plugin, "kit_action");
        String action = clicked.getItemMeta().getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);

        if (action != null) {
            if (action.startsWith("claim:")) {
                String kitId = action.substring(6);
                claimKit(player, kitId);
                player.closeInventory();
            } else if (action.equals("back")) {
                openKitGUI(player);
            }
            return;
        }

        NamespacedKey kitKey = new NamespacedKey(plugin, "kit_id");
        String kitId = clicked.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);

        if (kitId != null) {
            openKitPreview(player, kitId);
        }
    }

    private void claimKit(Player player, String kitId) {
        Kit kit = KitManager.getKit(kitId);
        if (kit == null) {
            player.sendMessage(langManager.getMessageFor(player, "kits.kit-not-found",
                    kitConfig.getKitNotFoundMessage(),
                    LanguageManager.ComponentPlaceholder.of("{kit}", kitId)));
            return;
        }

        if (!player.hasPermission(KitPermission.node(kitId))) {
            player.sendMessage(langManager.getMessageFor(player, "kits.no-permission",
                    kitConfig.getNoPermissionMessage(),
                    LanguageManager.ComponentPlaceholder.of("{kit}", kitId),
                    LanguageManager.ComponentPlaceholder.of("{permission}", KitPermission.node(kitId))));
            return;
        }

        int cooldown = KitConfigManager.getConfig().getInt("kits." + kitId + ".cooldown", 0);
        if (kitStorage.isOnCooldown(player.getUniqueId(), kitId, cooldown)) {
            long remaining = kitStorage.getRemainingCooldown(player.getUniqueId(), kitId, cooldown);
            player.sendMessage(langManager.getMessageFor(player, "kits.cooldown",
                    kitConfig.getCooldownMessage(),
                    LanguageManager.ComponentPlaceholder.of("{kit}", kitId),
                    LanguageManager.ComponentPlaceholder.of("{time}", remaining)));
            return;
        }

        for (ItemStack item : kit.getItems()) {
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }

        kitStorage.saveKitClaim(player.getUniqueId(), kitId);

        player.sendMessage(langManager.getMessageFor(player, "kits.claim-success",
                kitConfig.getClaimSuccessMessage(),
                LanguageManager.ComponentPlaceholder.of("{kit}", kitId)));
    }
}