package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaultManager implements Listener {

    private final JavaPlugin plugin;
    private final File vaultsFolder;
    private final Map<UUID, VaultSession> editingVaults = new HashMap<>();

    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.vaultsFolder = new File(plugin.getDataFolder(), "vaults");
        if (!vaultsFolder.exists()) vaultsFolder.mkdirs();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Dynamic prefix getter (adjust based on your main plugin class)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    public void openVault(Player player, int number) {
        if (!isValidVaultNumber(number)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Vault number must be between 1 and 10.");
            return;
        }

        if (!player.hasPermission("serveressentials.vault" + number)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to open vault " + number + ".");
            return;
        }

        Inventory inv = loadVault(player.getUniqueId(), number, "Vault " + number);
        player.openInventory(inv);
        player.setMetadata("vault_id", new FixedMetadataValue(plugin, number));
    }

    public void openVaultAsAdmin(Player admin, UUID targetUUID, String targetName, int number, boolean previewOnly) {
        if (!isValidVaultNumber(number)) {
            admin.sendMessage(getPrefix() + ChatColor.RED + "Vault number must be between 1 and 10.");
            return;
        }

        String title = (previewOnly ? "Viewing " : "Editing ") + targetName + "'s Vault " + number;
        Inventory inv = loadVault(targetUUID, number, title);
        admin.openInventory(inv);

        if (!previewOnly) {
            editingVaults.put(admin.getUniqueId(), new VaultSession(targetUUID, number));
            admin.setMetadata("editing_vault", new FixedMetadataValue(plugin, true));
        }
    }

    private Inventory loadVault(UUID uuid, int number, String title) {
        Inventory inv = Bukkit.createInventory(null, 54, title);
        File file = getVaultFile(uuid, number);
        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

        for (int i = 0; i < 54; i++) {
            if (data.contains("slot." + i)) {
                inv.setItem(i, data.getItemStack("slot." + i));
            }
        }
        return inv;
    }

    @EventHandler
    public void onVaultClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();

        if (player.hasMetadata("vault_id")) {
            int vaultId = player.getMetadata("vault_id").get(0).asInt();
            player.removeMetadata("vault_id", plugin);
            saveVault(player.getUniqueId(), vaultId, inv);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Vault " + vaultId + " saved.");
        } else if (player.hasMetadata("editing_vault")) {
            VaultSession session = editingVaults.remove(player.getUniqueId());
            if (session != null) {
                saveVault(session.targetUUID, session.vaultNumber, inv);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Saved edits to " + session.vaultNumber + "'s vault.");
            }
            player.removeMetadata("editing_vault", plugin);
        }
    }

    private void saveVault(UUID uuid, int number, Inventory inv) {
        File file = getVaultFile(uuid, number);
        YamlConfiguration data = new YamlConfiguration();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                data.set("slot." + i, item);
            }
        }

        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save vault " + number + " for UUID " + uuid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearVault(UUID uuid, int number) {
        File file = getVaultFile(uuid, number);
        if (file.exists()) {
            if (file.delete()) {
                Bukkit.getLogger().info(getPrefix() + "Vault " + number + " for UUID " + uuid + " cleared.");
            } else {
                Bukkit.getLogger().warning(getPrefix() + "Failed to clear vault " + number + " for UUID " + uuid + ".");
            }
        }
    }

    public File getVaultFile(UUID uuid, int number) {
        return new File(vaultsFolder, uuid + "_vault" + number + ".yml");
    }

    private boolean isValidVaultNumber(int number) {
        return number >= 1 && number <= 10;
    }

    private static class VaultSession {
        public final UUID targetUUID;
        public final int vaultNumber;

        public VaultSession(UUID targetUUID, int vaultNumber) {
            this.targetUUID = targetUUID;
            this.vaultNumber = vaultNumber;
        }
    }
}
