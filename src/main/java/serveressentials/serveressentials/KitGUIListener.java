package serveressentials.serveressentials;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KitGUIListener implements Listener {

    private final KitCommand kitCommand;
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public KitGUIListener(KitCommand command) {
        this.kitCommand = command;
    }

    /* ------------------------------------------------------------------
     * MAIN KITS MENU
     * ------------------------------------------------------------------ */
    public void openKitGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, formatColors("<#00bfff>Available Kits"));

        FileConfiguration config = KitConfigManager.getConfig();
        ConfigurationSection kitsSec = config.getConfigurationSection("kits");
        if (kitsSec == null) {
            player.sendMessage(formatColors("<#ff5555>No kits are defined in the config."));
            return;
        }

        int auto = 0;

        for (String kitId : kitsSec.getKeys(false)) {

            // Permission gate
            String permission = kitsSec.getString(kitId + ".permission");
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) continue;

            // Resolve slot: prefer display.slot, then top-level slot, else auto
            int slot = kitsSec.getInt(kitId + ".display.slot",
                    kitsSec.getInt(kitId + ".slot", auto));
            if (slot < 0 || slot >= 54) {
                slot = auto;
            }
            auto = nextEmpty(gui, slot);

            // Resolve icon material (default shulker)
            String matName = kitsSec.getString(kitId + ".display.material", "SHULKER_BOX");
            Material mat = Material.matchMaterial(matName.toUpperCase(Locale.ROOT));
            if (mat == null) mat = Material.SHULKER_BOX;

            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                String rawName = kitsSec.getString(kitId + ".display.name", kitId);
                meta.setDisplayName(formatColors(rawName));

                List<String> lore = new ArrayList<>();
                for (String raw : kitsSec.getStringList(kitId + ".display.lore")) {
                    lore.add(formatColors(raw));
                }
                meta.setLore(lore);

                icon.setItemMeta(meta);
            }

            // tag the item with kit id
            icon = applyCustomTag(icon, "kitName", kitId);

            gui.setItem(slot, icon);
        }

        player.openInventory(gui);
    }

    /* Find the next free slot >= start, or wrap to next free from 0. */
    private int nextEmpty(Inventory inv, int start) {
        for (int i = start; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) return i;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) return i;
        }
        return 0; // full, overwrite 0 (shouldn't happen)
    }

    /* ------------------------------------------------------------------
     * CLICK HANDLER
     * ------------------------------------------------------------------ */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = event.getView().getTitle();
        boolean kitsMenu = title.contains("Available Kits");
        boolean previewMenu = title.contains("Preview Kit: ");

        if (!kitsMenu && !previewMenu) return; // not our GUI

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        if (kitsMenu) {
            String kitId = getCustomTag(clicked, "kitName");
            if (kitId == null) return;
            openKitPreview(player, kitId);
            return;
        }

        // preview menu
        if (previewMenu) {
            if (clicked.getType() == Material.LIME_CONCRETE) {
                String kitId = getCustomTag(clicked, "kitName");
                if (kitId != null) claimKit(player, kitId);
                player.closeInventory();
            } else if (clicked.getType() == Material.BARRIER) {
                // reopen main menu
                openKitGUI(player);
            }
        }
    }

    /* ------------------------------------------------------------------
     * PREVIEW MENU
     * ------------------------------------------------------------------ */
    private void openKitPreview(Player player, String kitId) {
        FileConfiguration config = KitConfigManager.getConfig();
        ConfigurationSection kitSec = config.getConfigurationSection("kits." + kitId);
        if (kitSec == null) {
            player.sendMessage(formatColors("<#ff5555>That kit no longer exists."));
            return;
        }

        Inventory preview = Bukkit.createInventory(null, 54, formatColors("<#ffdd00>Preview Kit: " + kitId));

        // If preview section exists, use it (slot -> itemString)
        ConfigurationSection prevSec = kitSec.getConfigurationSection("preview");
        if (prevSec != null) {
            for (String slotKey : prevSec.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotKey);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (slot < 0 || slot >= 54) continue;
                String itemString = prevSec.getString(slotKey);
                ItemStack item = KitManager.parseItemString(itemString);
                if (item != null) preview.setItem(slot, item);
            }
        } else {
            // Fallback: show claimable items list sequentially
            List<String> itemsList = kitSec.getStringList("items");
            int idx = 0;
            for (String s : itemsList) {
                ItemStack item = KitManager.parseItemString(s);
                if (item != null && idx < 54) {
                    preview.setItem(idx++, item);
                }
            }
        }

        // Accept button
        ItemStack accept = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(formatColors("<#55ff55>Claim Kit"));
        acceptMeta.setLore(Collections.singletonList(formatColors("<#aaaaaa>Click to claim this kit")));
        accept.setItemMeta(acceptMeta);
        accept = applyCustomTag(accept, "kitName", kitId);
        preview.setItem(45, accept);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(formatColors("<#ff5555>Back"));
        cancelMeta.setLore(Collections.singletonList(formatColors("<#aaaaaa>Return to kits menu")));
        cancel.setItemMeta(cancelMeta);
        preview.setItem(53, cancel);

        player.openInventory(preview);
    }

    /* ------------------------------------------------------------------
     * CLAIM LOGIC
     * ------------------------------------------------------------------ */
    private void claimKit(Player player, String kitId) {
        FileConfiguration config = KitConfigManager.getConfig();
        String perm = config.getString("kits." + kitId + ".permission");
        int cooldown = config.getInt("kits." + kitId + ".cooldown", 0);

        if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
            player.sendMessage(formatColors("<#ff5555>You don't have permission to claim this kit."));
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Long> playerCooldowns = kitCommand.getCooldowns()
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long lastUsed = playerCooldowns.getOrDefault(kitId.toLowerCase(Locale.ROOT), 0L);
        long diff = (now - lastUsed) / 1000;

        if (diff < cooldown) {
            player.sendMessage(formatColors("<#ffaa00>You must wait " + (cooldown - diff) + " seconds."));
            return;
        }

        // Give items
        List<String> itemList = config.getStringList("kits." + kitId + ".items");
        for (String s : itemList) {
            ItemStack it = KitManager.parseItemString(s);
            if (it != null) player.getInventory().addItem(it);
        }

        // Update cooldown
        playerCooldowns.put(kitId.toLowerCase(Locale.ROOT), now);
        kitCommand.getCooldowns().put(player.getUniqueId(), playerCooldowns);

        player.sendMessage(formatColors("<#55ff55>You claimed kit: <#00ffff>" + kitId));
    }

    /* ------------------------------------------------------------------
     * COLOR UTIL
     * ------------------------------------------------------------------ */
    private String formatColors(String input) {
        if (input == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            ChatColor hexColor = ChatColor.of("#" + hexCode);
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /* ------------------------------------------------------------------
     * PERSISTENT DATA HELPERS
     * ------------------------------------------------------------------ */
    private ItemStack applyCustomTag(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        NamespacedKey namespacedKey = new NamespacedKey(ServerEssentials.getInstance(), key.toLowerCase(Locale.ROOT));
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value.toLowerCase(Locale.ROOT));
        item.setItemMeta(meta);
        return item;
    }

    private String getCustomTag(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(ServerEssentials.getInstance(), key.toLowerCase(Locale.ROOT));
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }
}