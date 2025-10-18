package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.*;

public class AdminUtilitiesCommand implements CommandExecutor, TabCompleter {

    private final Set<Player> vanished = new HashSet<>();
    private final Set<Player> godMode = new HashSet<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        MessagesManager messages = ServerEssentials.getInstance().getMessagesManager();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessageComponent("adminutilities.only-players"));
            return true;
        }


        switch (label.toLowerCase()) {
            case "vanish" -> {
                if (vanished.contains(player)) {
                    vanished.remove(player);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.showPlayer(ServerEssentials.getInstance(), player);
                    }
                    player.sendMessage(messages.getMessageComponent("adminutilities.vanish.visible"));
                } else {
                    vanished.add(player);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.hidePlayer(ServerEssentials.getInstance(), player);
                    }
                    player.sendMessage(messages.getMessageComponent("adminutilities.vanish.vanished"));
                }
            }

            case "god" -> {
                if (godMode.contains(player)) {
                    godMode.remove(player);
                    player.sendMessage(messages.getMessageComponent("adminutilities.god.disabled"));
                } else {
                    godMode.add(player);
                    player.sendMessage(messages.getMessageComponent("adminutilities.god.enabled"));
                }
            }

            case "invsee" -> {
                if (args.length < 1) {
                    player.sendMessage(messages.getMessageComponent("adminutilities.invsee.usage"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(messages.getMessageComponent("adminutilities.player-not-found"));
                    return true;
                }
                player.openInventory(target.getInventory());
                player.sendMessage(messages.getMessageComponent(
                        "adminutilities.invsee.success",
                        "%target%", target.getName()
                ));
            }

            case "invclear" -> {
                Player target = player;
                if (args.length >= 1) {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(messages.getMessageComponent("adminutilities.player-not-found"));
                        return true;
                    }
                }
                target.getInventory().clear();
                target.sendMessage(messages.getMessageComponent("adminutilities.invclear.cleared"));
                if (!target.equals(player)) {
                    player.sendMessage(messages.getMessageComponent(
                            "adminutilities.invclear.cleared-other",
                            "%target%", target.getName()
                    ));
                }
            }

            case "tp" -> {
                if (args.length < 1) {
                    player.sendMessage(messages.getMessageComponent("adminutilities.tp.usage"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(messages.getMessageComponent("adminutilities.player-not-found"));
                    return true;
                }
                player.teleport(target);
                player.sendMessage(messages.getMessageComponent(
                        "adminutilities.tp.success",
                        "%target%", target.getName()
                ));
            }
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if ((alias.equalsIgnoreCase("tp") || alias.equalsIgnoreCase("invsee") || alias.equalsIgnoreCase("invclear")) && args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(input)) matches.add(online.getName());
            }
            return matches;
        }

        return Collections.emptyList();
    }

    public boolean isGodMode(Player player) {
        return godMode.contains(player);
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player);
    }
}
