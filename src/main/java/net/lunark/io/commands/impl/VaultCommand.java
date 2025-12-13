package net.lunark.io.commands.impl;

import net.lunark.io.vault.VaultManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultCommand implements CommandExecutor {
    private final VaultManager vaultManager;
    private final PlayerLanguageManager langManager;

    public VaultCommand(VaultManager vaultManager, PlayerLanguageManager langManager) {
        this.vaultManager = vaultManager;
        this.langManager = langManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "vault.only-players",
                    org.bukkit.ChatColor.RED + "Only players can use this command!"));
            return true;
        }

        if (args.length == 0) {
            vaultManager.openSelector(player);
            return true;
        }

        try {
            int vaultNumber = Integer.parseInt(args[0]);
            if (vaultNumber < 1 || vaultNumber > 10) {
                player.sendMessage(langManager.getMessageFor(player, "vault.invalid-number",
                        org.bukkit.ChatColor.RED + "Vault number must be between 1-10!"));
                return true;
            }
            vaultManager.openVault(player, vaultNumber);
        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessageFor(player, "vault.invalid-number",
                    org.bukkit.ChatColor.RED + "Invalid vault number!"));
        }

        return true;
    }
}