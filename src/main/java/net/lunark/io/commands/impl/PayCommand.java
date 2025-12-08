package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.PayConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PayCommand implements CommandExecutor {
    // HARDCODED PERMISSION - NOT CONFIGURABLE
    private static final String PERMISSION = "serveressentials.command.pay";
    private static final String COMMAND_NAME = "pay";
    private static final long CONFIRMATION_TIMEOUT = 15_000; // 15 seconds
    private static final Map<UUID, PendingPayment> pendingPayments = new HashMap<>();

    private final PlayerLanguageManager langManager;
    private final PayConfig config;
    private final ServerEssentialsEconomy economy;
    private final Plugin plugin;

    public PayCommand(Plugin plugin, PlayerLanguageManager langManager, PayConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!senderPlayer.hasPermission(PERMISSION)) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 2) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".usage",
                    "<yellow>Usage: <white>{usage}",
                    ComponentPlaceholder.of("{usage}", "/" + label + " <player> <amount>")));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".player-not-found",
                    "<red>Player not found: <white>{player}",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        if (target.getUniqueId().equals(senderPlayer.getUniqueId())) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".self-payment",
                    "<red>You cannot pay yourself!"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".invalid-amount",
                    "<red>Invalid amount: <white>{amount}",
                    ComponentPlaceholder.of("{amount}", args[1])));
            return true;
        }

        if (amount <= 0) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".positive-amount",
                    "<red>Amount must be positive!"));
            return true;
        }

        if (economy.getBalance(senderPlayer) < amount) {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".not-enough",
                    "<red>You don't have enough money!"));
            return true;
        }

        // Check if target has payments disabled
        economy.hasPaymentsDisabled(target.getUniqueId()).thenAccept(disabled -> {
            if (disabled) {
                senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                        "commands." + COMMAND_NAME + ".target-disabled",
                        "<red>{player} has disabled payments!",
                        ComponentPlaceholder.of("{player}", target.getName())));
                return;
            }

            // Check if sender has pay-confirm disabled
            economy.hasPayConfirmDisabled(senderPlayer.getUniqueId()).thenAccept(confirmDisabled -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (confirmDisabled) {
                        processPayment(senderPlayer, target, amount); // Direct payment
                    } else {
                        processPaymentWithConfirmation(senderPlayer, target, amount); // Show confirmation
                    }
                });
            }).exceptionally(ex -> {
                senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                        "commands." + COMMAND_NAME + ".error",
                        "<red>Error checking confirmation status. Please try again."));
                return null;
            });
        }).exceptionally(ex -> {
            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".error",
                    "<red>Error checking payment status. Please try again."));
            return null;
        });

        return true;
    }

    private void processPaymentWithConfirmation(Player senderPlayer, OfflinePlayer target, double amount) {
        UUID senderUUID = senderPlayer.getUniqueId();

        if (pendingPayments.containsKey(senderUUID)) {
            PendingPayment pending = pendingPayments.get(senderUUID);

            if (System.currentTimeMillis() - pending.timestamp > CONFIRMATION_TIMEOUT) {
                pendingPayments.remove(senderUUID);
                senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                        "commands." + COMMAND_NAME + ".confirmation-expired",
                        "<red>Previous payment confirmation expired. Please try again."));
                return;
            }

            if (pending.target.equals(target.getUniqueId()) && pending.amount == amount) {
                pendingPayments.remove(senderUUID);
                processPayment(senderPlayer, target, amount);
                return;
            }

            senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                    "commands." + COMMAND_NAME + ".pending-other",
                    "<yellow>You already have a pending payment to <white>{player} <yellow>for <green>{amount}",
                    ComponentPlaceholder.of("{player}", Bukkit.getOfflinePlayer(pending.target).getName()),
                    ComponentPlaceholder.of("{amount}", economy.format(pending.amount))));
            return;
        }

        pendingPayments.put(senderUUID, new PendingPayment(target.getUniqueId(), amount));
        senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                "commands." + COMMAND_NAME + ".confirm-message",
                "<green>Click to confirm: Pay <white>{amount} <green>to <white>{player}",
                ComponentPlaceholder.of("{amount}", economy.format(amount)),
                ComponentPlaceholder.of("{player}", target.getName())));
        senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                "commands." + COMMAND_NAME + ".confirm-instruction",
                "<gray>Type <white>{command} <gray>again to confirm.",
                ComponentPlaceholder.of("{command}", "/" + COMMAND_NAME + " " + target.getName() + " " + amount)));
    }

    private void processPayment(Player senderPlayer, OfflinePlayer target, double amount) {
        economy.withdrawPlayer(senderPlayer, amount);
        economy.depositPlayer(target, amount);

        senderPlayer.sendMessage(langManager.getMessageFor(senderPlayer,
                "commands." + COMMAND_NAME + ".success-sender",
                "<green>You paid <white>{amount} <green>to <white>{player}",
                ComponentPlaceholder.of("{amount}", economy.format(amount)),
                ComponentPlaceholder.of("{player}", target.getName())));

        if (target.isOnline()) {
            ((Player) target).sendMessage(langManager.getMessageFor((Player) target,
                    "commands." + COMMAND_NAME + ".success-target",
                    "<green>You received <white>{amount} <green>from <white>{player}",
                    ComponentPlaceholder.of("{amount}", economy.format(amount)),
                    ComponentPlaceholder.of("{player}", senderPlayer.getName())));
        }
    }

    static class PendingPayment {
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