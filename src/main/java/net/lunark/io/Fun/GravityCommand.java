package net.lunark.io.Fun;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.lunark.io.util.FunMessages;

import java.util.List;
import java.util.stream.Collectors;

public final class GravityCommand implements CommandExecutor {

    private final FunMessages msg;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GravityCommand(FunMessages msg) {
        this.msg = msg;
        msg.addDefault("gravity.only-players", "<#55AA55>Only players can use this.");
        msg.addDefault("gravity.player-not-found", "<#CC6B6B>Player not found.");
        msg.addDefault("gravity.no-permission", "<#CC6B6B>Missing permission: <white>serveressentials.gravity");
        msg.addDefault("gravity.toggled-self", "<#88C488>Gravity <white>{state}<#88C488>.");
        msg.addDefault("gravity.toggled-other", "<#88C488>Gravity <white>{state}<#88C488> for <white>{target}<#88C488>.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        boolean silent = false;
        String targetName = null;
        for (String a : args) {
            if ("-s".equalsIgnoreCase(a)) silent = true;
            else if (targetName == null) targetName = a;
        }
        Player player;
        if (targetName == null) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(mm.deserialize(msg.getConfig().getString("gravity.only-players")));
                return true;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(targetName);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(mm.deserialize(msg.getConfig().getString("gravity.player-not-found")));
                return true;
            }
        }

        if (!sender.hasPermission("serveressentials.gravity")) {
            sender.sendMessage(mm.deserialize(msg.getConfig().getString("gravity.no-permission")));
            return true;
        }

        boolean newState = !player.hasGravity();
        player.setGravity(newState);

        if (!silent) {
            String key = sender.equals(player) ? "gravity.toggled-self" : "gravity.toggled-other";
            String raw = msg.getConfig().getString(key)
                    .replace("{state}", newState ? "enabled" : "disabled")
                    .replace("{target}", player.getName());
            sender.sendMessage(mm.deserialize(raw));
        }

        return true;
    }


    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "-s".startsWith(args[1].toLowerCase())) {
            return List.of("-s");
        }
        return List.of();
    }
}
