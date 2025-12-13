package net.lunark.io.vault;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lunark.io.ServerEssentials;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VaultManager {
    private final ServerEssentials plugin;
    private final PlayerLanguageManager langManager;
    private final VaultStorage storage;
    private static final int MAX_VAULTS = 10;

    public VaultManager(ServerEssentials plugin, PlayerLanguageManager langManager, VaultStorage storage) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
    }

    public void openVault(Player player, int number) {
        if (!isValidVaultNumber(number)) {
            player.sendMessage(getMessage(player, "vault.invalid",
                    Component.text("Invalid vault number! Must be 1-10", NamedTextColor.RED)));
            return;
        }

        if (!hasVaultPermission(player, number)) {
            Component msg = getMessage(player, "vault.no-permission",
                    Component.text("You need permission: serveressentials.command.pv.", NamedTextColor.RED)
                            .append(Component.text(number, NamedTextColor.YELLOW)));
            player.sendMessage(msg);
            return;
        }

        Component title = getMessage(player, "vault.title",
                Component.text("Vault #", NamedTextColor.GREEN)
                        .append(Component.text(number, NamedTextColor.WHITE)));

        Inventory inv = Bukkit.createInventory(null, 54, title);

        storage.load(player.getUniqueId(), number).thenAccept(optData -> {
            if (optData.isPresent()) {
                storage.deserializeInto(optData.get(), inv);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(inv);
                player.setMetadata("vault_id", new org.bukkit.metadata.FixedMetadataValue(plugin, number));
            });
        });
    }

    public void openSelector(Player player) {
        Component title = getMessage(player, "vault.selector.title",
                Component.text("Select a Vault", NamedTextColor.YELLOW));

        Inventory gui = Bukkit.createInventory(null, 45, title);

        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25, 28, 30};

        for (int i = 0; i < MAX_VAULTS; i++) {
            gui.setItem(slots[i], createVaultIcon(player, i + 1));
        }

        ItemStack close = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BARRIER);
        org.bukkit.inventory.meta.ItemMeta meta = close.getItemMeta();
        meta.displayName(getMessage(player, "vault.selector.close",
                Component.text("Close", NamedTextColor.RED)));
        close.setItemMeta(meta);
        gui.setItem(40, close);

        player.openInventory(gui);
    }

    private org.bukkit.inventory.ItemStack createVaultIcon(Player player, int number) {
        boolean hasPerm = hasVaultPermission(player, number);
        org.bukkit.Material mat = hasPerm ? org.bukkit.Material.BARREL : org.bukkit.Material.BARRIER;

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        Component name = hasPerm
                ? getMessage(player, "vault.selector.unlocked",
                Component.text("Vault #", NamedTextColor.GREEN)
                        .append(Component.text(number, NamedTextColor.WHITE)))
                : getMessage(player, "vault.selector.locked",
                Component.text("Vault #", NamedTextColor.RED)
                        .append(Component.text(number, NamedTextColor.WHITE))
                        .append(Component.text(" 🔒", NamedTextColor.RED)));

        meta.displayName(name);

        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(getMessage(player, hasPerm ? "vault.selector.open" : "vault.selector.no-perm",
                Component.text(hasPerm ? "Click to open" : "No permission", NamedTextColor.GRAY)));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void onVaultClose(Player player, Inventory inv) {
        if (player.hasMetadata("vault_id")) {
            int vaultId = player.getMetadata("vault_id").get(0).asInt();
            player.removeMetadata("vault_id", plugin);

            storage.save(player.getUniqueId(), vaultId, inv).thenAccept(v -> {
                player.sendMessage(getMessage(player, "vault.saved",
                        Component.text("Vault ", NamedTextColor.GREEN)
                                .append(Component.text(vaultId, NamedTextColor.YELLOW))
                                .append(Component.text(" saved!", NamedTextColor.GREEN))));
            });
        }
    }

    private boolean isValidVaultNumber(int number) {
        return number >= 1 && number <= MAX_VAULTS;
    }

    private boolean hasVaultPermission(Player player, int number) {
        return player.hasPermission("serveressentials.command.pv." + number);
    }

    private Component getMessage(Player player, String key, Component fallback) {
        return langManager.getMessageFor(player, key, String.valueOf(fallback));
    }
}