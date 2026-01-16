package com.godlycow.testapi;

import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.economy.EconomyPaymentSettings;
import com.serveressentials.api.economy.EconomyResponse;
import com.serveressentials.api.economy.event.EconomyDepositEvent;
import com.serveressentials.api.economy.event.EconomyPaymentSettingsChangeEvent;
import com.serveressentials.api.economy.event.EconomyWithdrawEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class EconomyAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private EconomyAPI economyAPI;

    public EconomyAPITestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setEconomyAPI(EconomyAPI economyAPI) {
        this.economyAPI = economyAPI;
        plugin.getLogger().info("EconomyAPI test command received API instance: " + (economyAPI != null));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can test the Economy API");
            return true;
        }

        if (economyAPI == null) {
            player.sendMessage("§cEconomyAPI not available yet. Please wait for plugin to fully load.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance" -> testBalance(player, args);
            case "deposit" -> testDeposit(player, args);
            case "withdraw" -> testWithdraw(player, args);
            case "format" -> testFormat(player, args);
            case "settings" -> testSettings(player, args);
            case "reload" -> testReload(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e--- Economy API Test Commands ---");
        player.sendMessage("§7/economytest balance [player] §f- Check balance");
        player.sendMessage("§7/economytest deposit <amount> §f- Deposit money");
        player.sendMessage("§7/economytest withdraw <amount> §f- Withdraw money");
        player.sendMessage("§7/economytest format <amount> §f- Format currency");
        player.sendMessage("§7/economytest settings §f- Check settings");
        player.sendMessage("§7/economytest reload §f- Reload economy");
    }

    private void testBalance(Player player, String[] args) {
        OfflinePlayer target = args.length >= 2 ? Bukkit.getOfflinePlayer(args[1]) : player;
        player.sendMessage("§eChecking balance for " + target.getName() + "...");
        economyAPI.getBalance(target).thenAccept(balance -> {
            player.sendMessage("§a✓ Balance: " + economyAPI.format(balance));
        });
    }

    private void testDeposit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /economytest deposit <amount>");
            return;
        }
        double amount = Double.parseDouble(args[1]);
        player.sendMessage("§eDepositing " + economyAPI.format(amount) + "...");
        economyAPI.deposit(player, amount).thenAccept(response -> {
            if (response.isSuccess()) {
                player.sendMessage("§a✓ Deposit successful! New balance: " + economyAPI.format(response.getBalance()));
            } else {
                player.sendMessage("§c✗ Deposit failed: " + response.getErrorMessage());
            }
        });
    }

    private void testWithdraw(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /economytest withdraw <amount>");
            return;
        }
        double amount = Double.parseDouble(args[1]);
        player.sendMessage("§eWithdrawing " + economyAPI.format(amount) + "...");
        economyAPI.withdraw(player, amount).thenAccept(response -> {
            if (response.isSuccess()) {
                player.sendMessage("§a✓ Withdrawal successful! New balance: " + economyAPI.format(response.getBalance()));
            } else {
                player.sendMessage("§c✗ Withdrawal failed: " + response.getErrorMessage());
            }
        });
    }

    private void testFormat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /economytest format <amount>");
            return;
        }
        double amount = Double.parseDouble(args[1]);
        player.sendMessage("§a✓ Formatted: " + economyAPI.format(amount));
    }

    private void testSettings(Player player, String[] args) {
        player.sendMessage("§eChecking payment settings...");
        economyAPI.getPaymentSettings(player.getUniqueId().toString()).thenAccept(settings -> {
            player.sendMessage("§a✓ Payments disabled: " + settings.isPaymentsDisabled());
            player.sendMessage("§a✓ Confirm disabled: " + settings.isPayConfirmDisabled());
        });
    }

    private void testReload(Player player) {
        player.sendMessage("§eReloading economy...");
        economyAPI.reload().thenRun(() -> {
            player.sendMessage("§a✓ Economy reloaded!");
        });
    }

    @EventHandler
    public void onEconomyDeposit(EconomyDepositEvent event) {
        plugin.getLogger().info(String.format(
                "[EconomyTest] %s deposited %.2f: %s",
                event.getPlayer().getName(),
                event.getAmount(),
                event.getResponse().isSuccess() ? "SUCCESS" : "FAILED"
        ));
    }

    @EventHandler
    public void onEconomyWithdraw(EconomyWithdrawEvent event) {
        plugin.getLogger().info(String.format(
                "[EconomyTest] %s withdrew %.2f: %s",
                event.getPlayer().getName(),
                event.getAmount(),
                event.getResponse().isSuccess() ? "SUCCESS" : "FAILED"
        ));
    }

    @EventHandler
    public void onPaymentSettingsChange(EconomyPaymentSettingsChangeEvent event) {
        plugin.getLogger().info(String.format(
                "[EconomyTest] %s settings changed: payments=%s → %s, confirm=%s → %s",
                event.getPlayer().getName(),
                event.getOldSettings().isPaymentsDisabled(),
                event.getNewSettings().isPaymentsDisabled(),
                event.getOldSettings().isPayConfirmDisabled(),
                event.getNewSettings().isPayConfirmDisabled()
        ));
    }
}