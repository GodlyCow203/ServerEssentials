package net.lunark.io.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;

import java.io.File;

public class BalanceCommand implements CommandExecutor {

    private final Economy economy;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private FileConfiguration messages;

    public BalanceCommand(Economy economy) {
        this.economy = economy;
        loadMessages();
    }

    private void loadMessages() {
        try {
            File file = new File(ServerEssentials.getInstance().getDataFolder(), "messages/economy.yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                ServerEssentials.getInstance().saveResource("messages/economy.yml", false);
            }
            messages = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMessage(String path, String def) {
        return messages.getString(path, def);
    }

    private Component parse(String message) {
        return mini.deserialize(message.replace("&", "§"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(parse(getMessage("only-players", "<red>Only players can check their own balance.")));
                return true;
            }

            double balance = economy.getBalance(player);
            String msg = getMessage("balance-self", "<green>Your balance is: <gold>$%balance%</gold>");
            msg = msg.replace("%balance%", String.format("%.2f", balance));
            player.sendMessage(parse(msg));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(parse(getMessage("player-not-found", "<red>Player not found.")));
            return true;
        }

        double balance = economy.getBalance(target);
        String msg = getMessage("balance-other", "<green>%player%'s balance is: <gold>$%balance%</gold>");
        msg = msg.replace("%player%", target.getName())
                .replace("%balance%", String.format("%.2f", balance));
        sender.sendMessage(parse(msg));

        return true;
    }
}
