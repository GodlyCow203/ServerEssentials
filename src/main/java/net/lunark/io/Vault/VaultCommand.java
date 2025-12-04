package net.lunark.io.Vault;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.util.VaultMessages;

public class VaultCommand implements CommandExecutor {

    private final VaultManager vaultManager;
    private final VaultMessages messages;

    public VaultCommand(VaultManager vaultManager, VaultMessages messages) {
        this.vaultManager = vaultManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Vault.only-players"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.get("Vault.usage"));
            return true;
        }

        try {
            int vaultNumber = Integer.parseInt(args[0]);
            if (vaultNumber < 1 || vaultNumber > 10) {
                player.sendMessage(messages.get("Vault.invalid-number"));
                return true;
            }
            vaultManager.openVault(player, vaultNumber);
        } catch (NumberFormatException e) {
            player.sendMessage(messages.get("Vault.invalid-number"));
        }

        return true;
    }
}
