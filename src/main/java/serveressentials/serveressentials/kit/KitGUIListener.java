
package serveressentials.serveressentials.kit;

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
import serveressentials.serveressentials.ServerEssentials;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KitGUIListener implements Listener {

    private final KitCommand kitCommand;
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public KitGUIListener(KitCommand command) {
        this.kitCommand = command;
    }
    private void openKitPreview(Player player, String kitId) {
        FileConfiguration config = KitConfigManager.getConfig();
        List<String> itemList = config.getStringList("kits." + kitId + ".items");

        Inventory preview = Bukkit.createInventory(null, 54, formatColors("<#FFFFFF>Preview Kit: " + kitId));

        int slot = 0;
        for (String itemStr : itemList) {
            ItemStack item = KitManager.parseItemString(itemStr);
            if (item != null && slot < 54) {
                preview.setItem(slot++, item);
            }
        }

        // Add claim button
        ItemStack claim = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta claimMeta = claim.getItemMeta();
        if (claimMeta != null) {
            claimMeta.setDisplayName(formatColors("<#1BFF00>Claim Kit"));
            claimMeta.getPersistentDataContainer().set(
                    new NamespacedKey(ServerEssentials.getInstance(), "kitName"),
                    PersistentDataType.STRING, kitId.toLowerCase(Locale.ROOT)
            );
            claim.setItemMeta(claimMeta);
        }
        preview.setItem(53, claim); // last slot

        // Add back button
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(formatColors("<#FF0000>Back"));
            back.setItemMeta(backMeta);
        }
        preview.setItem(45, back); // first slot of bottom row

        player.openInventory(preview);
    }


    public void openKitGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ServerEssentials.getInstance().getKitMessages().get("gui-title", "%player%", player.getName()));

        FileConfiguration config = KitConfigManager.getConfig();
        ConfigurationSection kitsSec = config.getConfigurationSection("kits");
        if (kitsSec == null) {
            player.sendMessage(ServerEssentials.getInstance().getKitMessages().get("no-kits-defined", "%player%", player.getName()));
            return;
        }

        int auto = 0;

        for (String kitId : kitsSec.getKeys(false)) {
            String permission = kitsSec.getString(kitId + ".permission");
            boolean hasPermission = permission == null || permission.isEmpty() || player.hasPermission(permission);

            int slot = kitsSec.getInt(kitId + ".display.slot", kitsSec.getInt(kitId + ".slot", auto));
            if (slot < 0 || slot >= 54) {
                slot = auto;
            }
            auto = nextEmpty(gui, slot);

            String matName = kitsSec.getString(kitId + ".display.material", "SHULKER_BOX");
            Material mat = Material.matchMaterial(matName.toUpperCase(Locale.ROOT));
            if (mat == null) mat = Material.SHULKER_BOX;

            ItemStack icon;
            ItemMeta meta;

            if (hasPermission) {
                icon = new ItemStack(mat);
                meta = icon.getItemMeta();
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
            } else {
                icon = new ItemStack(Material.BARRIER);
                meta = icon.getItemMeta();
                if (meta != null) {
                    String rawName = kitsSec.getString(kitId + ".display.name", kitId);
                    meta.setDisplayName(formatColors("<#FF0000>Locked: <#AAAAAA>" + rawName));

                    List<String> lore = new ArrayList<>();
                    lore.add(formatColors("<#AAAAAA>You don't have permission to use this kit."));
                    if (permission != null && !permission.isEmpty()) {
                        lore.add(formatColors("<#AAAAAA>Required: <#FFFFFF>" + permission));
                    }

                    long now = System.currentTimeMillis();
                    Map<String, Long> cooldowns = kitCommand.getCooldowns().getOrDefault(player.getUniqueId(), new HashMap<>());
                    long lastUsed = cooldowns.getOrDefault(kitId.toLowerCase(Locale.ROOT), 0L);
                    long cooldown = kitsSec.getLong(kitId + ".cooldown", 0L);
                    long remaining = cooldown - ((now - lastUsed) / 1000);
                    if (remaining > 0) {
                        lore.add(formatColors("<#AAAAAA>Cooldown: <#FF0000>" + remaining + "s remaining"));
                    }
                    meta.setLore(lore);
                    icon.setItemMeta(meta);
                }
            }

            icon = applyCustomTag(icon, "kitName", kitId);
            gui.setItem(slot, icon);
        }

        player.openInventory(gui);
    }

    private int nextEmpty(Inventory inv, int start) {
        for (int i = start; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) return i;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) return i;
        }
        return 0;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player player)) return;

        Inventory clickedInv = event.getClickedInventory();
        Inventory topInv = event.getView().getTopInventory(); // GUI inventory

        if (clickedInv == null || clickedInv != topInv) return; // Only process GUI clicks

        String title = event.getView().getTitle();
        boolean kitsMenu = title.contains("Available Kits");
        boolean previewMenu = title.contains("Preview Kit: ");

        if (!kitsMenu && !previewMenu) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        if (kitsMenu) {
            String kitId = getCustomTag(clicked, "kitName");
            if (kitId == null) return;

            String permission = KitConfigManager.getConfig().getString("kits." + kitId + ".permission");
            if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
                player.sendMessage(ServerEssentials.getInstance().getKitMessages()
                        .get("claim.no-permission", "%kit%", kitId, "%player%", player.getName()));
                return;
            }

            openKitPreview(player, kitId);
            return;
        }

        if (previewMenu) {
            if (clicked.getType() == Material.LIME_CONCRETE) {
                String kitId = getCustomTag(clicked, "kitName");
                if (kitId != null) claimKit(player, kitId);
                player.closeInventory();
            } else if (clicked.getType() == Material.BARRIER) {
                openKitGUI(player);
            }
        }
    }



    private void claimKit(Player player, String kitId) {
        FileConfiguration config = KitConfigManager.getConfig();
        String perm = config.getString("kits." + kitId + ".permission");
        int cooldown = config.getInt("kits." + kitId + ".cooldown", 0);

        if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
            player.sendMessage(ServerEssentials.getInstance().getKitMessages()
                    .get("claim.no-permission", "%kit%", kitId, "%player%", player.getName(), "%permission%", perm));
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Long> playerCooldowns = kitCommand.getCooldowns()
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        long lastUsed = playerCooldowns.getOrDefault(kitId.toLowerCase(Locale.ROOT), 0L);
        long diff = (now - lastUsed) / 1000;

        if (diff < cooldown) {
            player.sendMessage(ServerEssentials.getInstance().getKitMessages()
                    .get("claim.cooldown", "%kit%", kitId, "%player%", player.getName(), "%time%", String.valueOf(cooldown - diff)));
            return;
        }

        List<String> itemList = config.getStringList("kits." + kitId + ".items");
        for (String s : itemList) {
            ItemStack it = KitManager.parseItemString(s);
            if (it != null) player.getInventory().addItem(it);
        }

        playerCooldowns.put(kitId.toLowerCase(Locale.ROOT), now);
        kitCommand.getCooldowns().put(player.getUniqueId(), playerCooldowns);

        player.sendMessage(ServerEssentials.getInstance().getKitMessages()
                .get("claim.success", "%kit%", kitId, "%player%", player.getName()));
    }


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

