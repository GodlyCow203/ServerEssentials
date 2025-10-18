package serveressentials.serveressentials.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EcoCommand implements CommandExecutor, TabCompleter {

    private final Economy economy;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public EcoCommand(Economy economy) {
        this.economy = economy;
    }

    private OfflinePlayer getTargetPlayer(String name) {
        Player online = Bukkit.getPlayer(name);
        return online != null ? online : Bukkit.getOfflinePlayerIfCached(name);
    }

    private Component getMessage(String key, Map<String, String> placeholders) {
        return EconomyMessagesManager.getMessage(key, placeholders);
    }

    private Component getMessage(String key) {
        return EconomyMessagesManager.getMessage(key);
    }

    private String getPrefix() {
        return EconomyMessagesManager.getMessage("prefix").toString(); // optional, or keep as placeholder
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMessage("usage", Map.of("%prefix%", getPrefix())));
            return true;
        }

        OfflinePlayer target = getTargetPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(getMessage("player-not-found", Map.of("%prefix%", getPrefix())));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give" -> {
                if (args.length < 3) return sendError(sender, "give-usage");
                double amount = parseAmount(args[2], sender, "invalid-amount");
                if (amount < 0) return true;

                economy.depositPlayer(target, amount);
                sender.sendMessage(getMessage("give-success", Map.of(
                        "%player%", target.getName(),
                        "%amount%", String.valueOf(amount)
                )));
            }
            case "take" -> {
                if (args.length < 3) return sendError(sender, "take-usage");
                double amount = parseAmount(args[2], sender, "invalid-amount");
                if (amount < 0) return true;

                economy.withdrawPlayer(target, amount);
                sender.sendMessage(getMessage("take-success", Map.of(
                        "%player%", target.getName(),
                        "%amount%", String.valueOf(amount)
                )));
            }
            case "reset" -> {
                double bal = economy.getBalance(target);
                economy.withdrawPlayer(target, bal);
                sender.sendMessage(getMessage("reset-success", Map.of(
                        "%player%", target.getName()
                )));
            }
            default -> sendError(sender, "unknown-subcommand");
        }

        return true;
    }

    private boolean sendError(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path, Map.of("%prefix%", getPrefix())));
        return true;
    }

    private double parseAmount(String input, CommandSender sender, String errorPath) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            sender.sendMessage(getMessage(errorPath, Map.of("%prefix%", getPrefix())));
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("give", "take", "reset")) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2) {
            String input = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) completions.add(p.getName());
            }
        }
        return completions;
    }
}
