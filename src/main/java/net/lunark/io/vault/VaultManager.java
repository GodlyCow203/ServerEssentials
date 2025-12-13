package net.lunark.io.vault;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultManager implements Listener {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final VaultStorage storage;
    private final Map<UUID, VaultSession> editingVaults = new HashMap<>();

    public VaultManager(JavaPlugin plugin, PlayerLanguageManager langManager, VaultStorage storage) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openVault(Player player, int number) {
        if (!isValidVaultNumber(number)) {
            player.sendMessage(langManager.getMessageFor(player, "vault.invalid-number",
                    "<red>Invalid vault number! Must be 1-10."));
            return;
        }

        String perm = "serveressentials.command.pv." + number;
        if (!player.hasPermission(perm)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", perm)));
            return;
        }

        Component title = langManager.getMessageFor(player, "vault.title",
                "<green>Vault <white>#{number}",
                LanguageManager.ComponentPlaceholder.of("{number}", String.valueOf(number)));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        storage.loadVaultData(player.getUniqueId(), number).thenAccept(optData -> {
            if (optData.isPresent()) {
                storage.deserializeInventory(inv, optData.get());
            }
        }).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(inv);
                player.setMetadata("vault_id", new FixedMetadataValue(plugin, number));
            });
        });
    }

    public void openVaultAsAdmin(Player admin, UUID targetUUID, String targetName, int number, boolean previewOnly) {
        if (!isValidVaultNumber(number)) {
            admin.sendMessage(langManager.getMessageFor(admin, "vault.invalid-number",
                    "<red>Invalid vault number! Must be 1-10."));
            return;
        }

        Component title = langManager.getMessageFor(admin, previewOnly ? "vault.admin-preview" : "vault.admin-edit",
                previewOnly ? "<yellow>Viewing <white>{player}'s <yellow>Vault #{number}" : "<red>Editing <white>{player}'s <red>Vault #{number}",
                LanguageManager.ComponentPlaceholder.of("{player}", targetName),
                LanguageManager.ComponentPlaceholder.of("{number}", String.valueOf(number)));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        storage.loadVaultData(targetUUID, number).thenAccept(optData -> {
            if (optData.isPresent()) {
                storage.deserializeInventory(inv, optData.get());
            }
        }).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                admin.openInventory(inv);
                if (!previewOnly) {
                    editingVaults.put(admin.getUniqueId(), new VaultSession(targetUUID, number));
                    admin.setMetadata("editing_vault", new FixedMetadataValue(plugin, true));
                }
            });
        });
    }

    @EventHandler
    public void onVaultClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Inventory inv = event.getInventory();

        if (player.hasMetadata("vault_id")) {
            int vaultId = player.getMetadata("vault_id").get(0).asInt();
            player.removeMetadata("vault_id", plugin);

            storage.saveVault(player.getUniqueId(), vaultId, inv).thenRun(() -> {
                player.sendMessage(langManager.getMessageFor(player, "vault.saved",
                        "<green>Vault <yellow>{vault}</yellow> saved!",
                        LanguageManager.ComponentPlaceholder.of("{vault}", String.valueOf(vaultId))));
            });
        } else if (player.hasMetadata("editing_vault")) {
            VaultSession session = editingVaults.remove(player.getUniqueId());
            if (session != null) {
                storage.saveVault(session.targetUUID, session.vaultNumber, inv).thenRun(() -> {
                    String targetName = Bukkit.getOfflinePlayer(session.targetUUID).getName();
                    player.sendMessage(langManager.getMessageFor(player, "vault.admin-saved",
                            "<green>Saved vault <yellow>{vault}</yellow> for <yellow>{player}</yellow>!",
                            LanguageManager.ComponentPlaceholder.of("{vault}", String.valueOf(session.vaultNumber)),
                            LanguageManager.ComponentPlaceholder.of("{player}", targetName != null ? targetName : "Unknown")));
                });
            }
            player.removeMetadata("editing_vault", plugin);
        }
    }

    public CompletableFuture<Void> clearVault(UUID uuid, int number) {
        return storage.clearVault(uuid, number);
    }

    public CompletableFuture<Boolean> hasVault(UUID uuid, int number) {
        return storage.hasVault(uuid, number);
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