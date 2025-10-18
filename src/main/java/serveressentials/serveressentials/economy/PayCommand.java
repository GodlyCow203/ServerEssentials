package serveressentials.serveressentials.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private static final HashMap<UUID, PendingPayment> pendingPayments = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 15_000; // 15 seconds

    private final FileConfiguration messagesConfig;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Economy economy;

    public PayCommand(Economy economy) {
        this.economy = economy;

        File messagesFile = new File(ServerEssentials.getInstance().getDataFolder(), "messages/economy.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            ServerEssentials.getInstance().saveResource("messages/economy.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String formatWithPlaceholders(String path, Object... placeholders) {
        String raw = messagesConfig.getString(path, path);
        if (placeholders.length % 2 != 0) return raw;
        for (int i = 0; i < placeholders.length; i += 2) {
            raw = raw.replace(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
        }
        return raw;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(mm.deserialize(formatWithPlaceholders("pay.only-players")));
            return true;
        }

        if (args.length != 2) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.usage", "%command%", "/pay")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.player-not-found")));
            return true;
        }

        if (target.getUniqueId().equals(senderPlayer.getUniqueId())) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.self-payment")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.invalid-amount")));
            return true;
        }

        if (amount <= 0) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.positive-amount")));
            return true;
        }

        if (economy.getBalance(senderPlayer) < amount) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.not-enough")));
            return true;
        }

        if (target.isOnline() && PayConfirmToggleCommand.hasConfirmationsDisabled((Player) target)) {
            senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.target-disabled")));
            return true;
        }

        UUID senderUUID = senderPlayer.getUniqueId();

        // Confirmations enabled
        if (!PayConfirmToggleCommand.hasConfirmationsDisabled(senderPlayer)) {

            if (pendingPayments.containsKey(senderUUID)) {
                PendingPayment pending = pendingPayments.get(senderUUID);

                if (System.currentTimeMillis() - pending.timestamp > CONFIRMATION_TIMEOUT) {
                    pendingPayments.remove(senderUUID);
                    senderPlayer.sendMessage(mm.deserialize(formatWithPlaceholders("pay.confirmation-expired")));
                    return true;
                }

                if (pending.target.equals(target.getUniqueId()) && pending.amount == amount) {
                    pendingPayments.remove(senderUUID);
                    processPayment(senderPlayer, target, amount);
                    return true;
                }

                senderPlayer.sendMessage(mm.deserialize(
                        formatWithPlaceholders(
                                "pay.pending-other",
                                "%player%", Bukkit.getOfflinePlayer(pending.target).getName(),
                                "%amount%", economy.format(pending.amount)
                        )));
                return true;
            }

            pendingPayments.put(senderUUID, new PendingPayment(target.getUniqueId(), amount));
            senderPlayer.sendMessage(mm.deserialize(
                    formatWithPlaceholders(
                            "pay.confirm-message",
                            "%player%", target.getName(),
                            "%amount%", economy.format(amount)
                    )));
            senderPlayer.sendMessage(mm.deserialize(
                    formatWithPlaceholders(
                            "pay.confirm-instruction",
                            "%player%", target.getName(),
                            "%amount%", economy.format(amount)
                    )));
            return true;
        }

        // Confirmations disabled → instant payment
        processPayment(senderPlayer, target, amount);
        return true;
    }

    private void processPayment(Player senderPlayer, OfflinePlayer target, double amount) {
        economy.withdrawPlayer(senderPlayer, amount);
        economy.depositPlayer(target, amount);

        senderPlayer.sendMessage(mm.deserialize(
                formatWithPlaceholders(
                        "pay.success-sender",
                        "%player%", target.getName(),
                        "%amount%", economy.format(amount)
                )));

        if (target.isOnline()) {
            ((Player) target).sendMessage(mm.deserialize(
                    formatWithPlaceholders(
                            "pay.success-target",
                            "%player%", senderPlayer.getName(),
                            "%amount%", economy.format(amount)
                    )));
        }
    }

    private static class PendingPayment {
        private final UUID target;
        private final double amount;
        private final long timestamp;

        public PendingPayment(UUID target, double amount) {
            this.target = target;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
