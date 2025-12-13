package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class VaultCommand implements CommandExecutor {
    private final VaultManager vaultManager;
    private final PlayerLanguageManager langManager;

    public VaultCommand(VaultManager vaultManager, PlayerLanguageManager langManager) {
        this.vaultManager = vaultManager;
        this.langManager = langManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "vault.only-players",
                    "<red>Only players can use this command!"));
            return true;
        }

        if (args.length == 1) {
            try {
                int vaultNumber = Integer.parseInt(args[0]);
                if (vaultNumber < 1 || vaultNumber > 10) {
                    player.sendMessage(langManager.getMessageFor(player, "vault.invalid-number",
                            "<red>Vault number must be between 1-10!"));
                    return true;
                }
                vaultManager.openVault(player, vaultNumber);
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(langManager.getMessageFor(player, "vault.invalid-number",
                        "<red>Invalid vault number!"));
                return true;
            }
        }

        openVaultSelector(player);
        return true;
    }

    private void openVaultSelector(Player player) {
        Component title = langManager.getMessageFor(player, "vault.selector.title",
                "<yellow>Select a Vault");
        Inventory gui = Bukkit.createInventory(null, 45, title);

        int[] slots = {10, 12, 14, 16, 28, 30, 32, 34};
        for (int i = 0; i < 8; i++) {
            gui.setItem(slots[i], createVaultItem(player, i + 1));
        }
        gui.setItem(33, createVaultItem(player, 9));
        gui.setItem(31, createVaultItem(player, 10));

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(langManager.getMessageFor(player, "vault.selector.close",
                "<red>Close"));
        close.setItemMeta(closeMeta);
        gui.setItem(40, close);

        player.openInventory(gui);
    }

    private ItemStack createVaultItem(Player player, int number) {
        String perm = "serveressentials.command.pv." + number;
        boolean has = player.hasPermission(perm);

        Material mat = has ? Material.BARREL : Material.BARRIER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        Component name = has
                ? langManager.getMessageFor(player, "vault.selector.unlocked",
                "<green>Vault <white>#" + number)
                : langManager.getMessageFor(player, "vault.selector.locked",
                "<red>Vault <white>#" + number + " <red>🔒");
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        lore.add(langManager.getMessageFor(player, has ? "vault.selector.click-to-open" : "vault.selector.no-permission",
                has ? "<gray>Click to open" : "<gray>No permission"));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}