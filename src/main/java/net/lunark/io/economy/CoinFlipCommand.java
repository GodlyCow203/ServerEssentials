package net.lunark.io.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

public class CoinFlipCommand implements CommandExecutor, TabCompleter {

    private final Random random = new Random();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static File messagesFile;
    private static org.bukkit.configuration.file.FileConfiguration messagesConfig;
    private final Economy economy;

    public CoinFlipCommand(Economy economy) {
        this.economy = economy;
        loadMessages();
    }

    public static void loadMessages() {
        messagesFile = new File(ServerEssentials.getInstance().getDataFolder(), "messages/economy.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            ServerEssentials.getInstance().saveResource("messages/economy.yml", false);
        }
        messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(messagesFile);
    }

    private Component prefix() {
        return mm.deserialize(messagesConfig.getString("prefix", "<blue><bold>[SE]</bold> </blue>"));
    }

    private Component getMessage(String path, Object... placeholders) {
        String raw = messagesConfig.getString(path, path);
        if (placeholders.length % 2 != 0) return mm.deserialize(raw);
        for (int i = 0; i < placeholders.length; i += 2) {
            raw = raw.replace(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
        }
        return mm.deserialize(raw);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize("<red>Only players can use this command!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix().append(getMessage("coinflip.usage")));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(prefix().append(getMessage("coinflip.invalid-amount")));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(prefix().append(getMessage("coinflip.amount-positive")));
            return true;
        }

        double balance = economy.getBalance(player);
        if (balance < amount) {
            player.sendMessage(prefix().append(getMessage("coinflip.not-enough-money",
                    "{balance}", String.format("%.2f", balance),
                    "{amount}", String.format("%.2f", amount))));
            return true;
        }

        boolean win = random.nextBoolean();
        if (win) {
            economy.depositPlayer(player, amount);
            player.sendMessage(prefix().append(getMessage("coinflip.win", "{amount}", String.format("%.2f", amount))));
        } else {
            economy.withdrawPlayer(player, amount);
            player.sendMessage(prefix().append(getMessage("coinflip.lose", "{amount}", String.format("%.2f", amount))));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("10", "50", "100", "500", "1000");
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String s : suggestions) {
                if (s.startsWith(input)) completions.add(s);
            }
            return completions;
        }
        return List.of();
    }
}
