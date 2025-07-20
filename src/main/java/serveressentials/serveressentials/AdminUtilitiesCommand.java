package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminUtilitiesCommand implements CommandExecutor, TabCompleter {

    private final Set<Player> vanished = new HashSet<>();
    private final Set<Player> godMode = new HashSet<>();

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "vanish" -> {
                if (vanished.contains(player)) {
                    vanished.remove(player);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.showPlayer(ServerEssentials.getInstance(), player);
                    }
                    player.sendMessage(getPrefix() + ChatColor.YELLOW + "You are now visible.");
                } else {
                    vanished.add(player);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.hidePlayer(ServerEssentials.getInstance(), player);
                    }
                    player.sendMessage(getPrefix() + ChatColor.GRAY + "You have vanished.");
                }
            }

            case "god" -> {
                if (godMode.contains(player)) {
                    godMode.remove(player);
                    player.sendMessage(getPrefix() + ChatColor.RED + "God mode disabled.");
                } else {
                    godMode.add(player);
                    player.sendMessage(getPrefix() + ChatColor.GREEN + "God mode enabled.");
                }
            }

            case "invsee" -> {
                if (args.length < 1) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /invsee <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
                    return true;
                }
                player.openInventory(target.getInventory());
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Opening " + target.getName() + "'s inventory.");
            }

            case "invclear" -> {
                Player target = player;
                if (args.length >= 1) {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
                        return true;
                    }
                }
                target.getInventory().clear();
                target.sendMessage(getPrefix() + ChatColor.RED + "Your inventory was cleared.");
                if (!target.equals(player)) {
                    player.sendMessage(getPrefix() + ChatColor.GREEN + "Cleared " + target.getName() + "'s inventory.");
                }
            }

            case "tp" -> {
                if (args.length < 1) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /tp <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
                    return true;
                }
                player.teleport(target);
                player.sendMessage(getPrefix() + ChatColor.AQUA + "Teleported to " + target.getName());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        String cmd = command.getName().toLowerCase();

        if ((cmd.equals("tp") || cmd.equals("invsee") || cmd.equals("invclear")) && args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(input)) {
                    matches.add(online.getName());
                }
            }
            return matches;
        }

        return Collections.emptyList();
    }

    public boolean isGodMode(Player player) {
        return godMode.contains(player);
    }
}
