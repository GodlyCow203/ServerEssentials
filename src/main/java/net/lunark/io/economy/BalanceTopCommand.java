package net.lunark.io.economy;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.util.*;

public class BalanceTopCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final Economy economy;
    private FileConfiguration messages;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BalanceTopCommand(ServerEssentials plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder() + "/messages", "economy.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("messages/economy.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    private Component msg(String path, Map<String, String> placeholders) {
        String raw = messages.getString(path, path);
        if (raw == null) return Component.text(path);

        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace("%" + e.getKey() + "%", e.getValue());
            }
        }

        return mm.deserialize(raw);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<UUID, Double> balances = new HashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            balances.put(p.getUniqueId(), economy.getBalance(p));
        }

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int limit = messages.getInt("balancetop.limit", 5);

        sender.sendMessage(msg("balancetop.header", Map.of("limit", String.valueOf(limit))));
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : sorted.subList(0, Math.min(limit, sorted.size()))) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            String name = player.getName() != null ? player.getName() : "Unknown";
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("rank", String.valueOf(rank));
            placeholders.put("player", name);
            placeholders.put("balance", String.format("%.2f", entry.getValue()));

            sender.sendMessage(msg("balancetop.entry", placeholders));
            rank++;
        }

        sender.sendMessage(msg("balancetop.footer", null));
        return true;
    }
}