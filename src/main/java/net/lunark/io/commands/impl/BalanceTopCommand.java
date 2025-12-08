package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.BalanceTopConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class BalanceTopCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.balancetop";
    private static final String COMMAND_NAME = "balancetop";

    private final PlayerLanguageManager langManager;
    private final BalanceTopConfig config;
    private final ServerEssentialsEconomy economy;

    public BalanceTopCommand(PlayerLanguageManager langManager, BalanceTopConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Component message = langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            sender.sendMessage(message);
            return true;
        }

        int limit = config.getLimit();

        CompletableFuture.runAsync(() -> {
            List<PlayerBalance> balances = Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(p -> new PlayerBalance(p.getUniqueId(), p.getName(), economy.getBalance(p)))
                    .sorted(Comparator.comparingDouble(PlayerBalance::balance).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ServerEssentials"), () -> {
                sendTopList(sender, balances, limit);
            });
        });

        return true;
    }

    private void sendTopList(CommandSender sender, List<PlayerBalance> balances, int limit) {
        sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".header",
                "<gold>=== Top {limit} Richest Players ===",
                ComponentPlaceholder.of("{limit}", String.valueOf(limit))));

        for (int i = 0; i < balances.size(); i++) {
            PlayerBalance pb = balances.get(i);
            String playerName = pb.name() != null ? pb.name() : "Unknown";

            sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                    "commands." + COMMAND_NAME + ".entry",
                    "<yellow>#{rank} <white>{player} <gray>- <green>{balance}",
                    ComponentPlaceholder.of("{rank}", String.valueOf(i + 1)),
                    ComponentPlaceholder.of("{player}", playerName),
                    ComponentPlaceholder.of("{balance}", String.format("%.2f", pb.balance()))));
        }

        sender.sendMessage(langManager.getComponent(String.valueOf(sender instanceof Player p ? p : null),
                "commands." + COMMAND_NAME + ".footer",
                "<gold>========================="));
    }

    private record PlayerBalance(UUID uuid, String name, double balance) {}
}