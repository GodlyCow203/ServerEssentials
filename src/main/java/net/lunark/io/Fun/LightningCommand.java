package net.lunark.io.Fun;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.lunark.io.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

public class LightningCommand implements CommandExecutor {

    private final FunMessages funMessages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LightningCommand(FunMessages funMessages) {
        this.funMessages = funMessages;

        funMessages.addDefault("lightning.only-players", "<red>Only players can use this command!</red>");
        funMessages.addDefault("lightning.player-not-found", "<red>Player <white><target></white> not found.</red>");
        funMessages.addDefault("lightning.no-permission", "<red>You need the <white>serveressentials.lightning</white> permission.</red>");
        funMessages.addDefault("lightning.struck-self", "<green>Lightning struck!</green>");
        funMessages.addDefault("lightning.struck-other", "<green>Lightning struck <white><target></white>!</green>");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        List<String> argv = new ArrayList<>(List.of(args));
        boolean silent = argv.remove("-s");
        String targetName = argv.isEmpty() ? null : argv.get(0);

        Player player;
        if (targetName == null) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(miniMessage.deserialize(
                        funMessages.getConfig().getString("lightning.only-players")));
                return true;
            }
            player = p;
        } else {
            player = sender.getServer().getPlayer(targetName);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(miniMessage.deserialize(
                        funMessages.getConfig().getString("lightning.player-not-found")
                                .replace("<target>", targetName)));
                return true;
            }
        }

        if (!sender.hasPermission("serveressentials.lightning")) {
            sender.sendMessage(miniMessage.deserialize(
                    funMessages.getConfig().getString("lightning.no-permission")));
            return true;
        }

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return true;

        Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager().getPlugin("ServerEssentials"),
                () -> world.strikeLightning(loc)
        );

        if (!silent) {
            String key = sender.equals(player)
                    ? "lightning.struck-self"
                    : "lightning.struck-other";
            String msg = funMessages.getConfig().getString(key)
                    .replace("<target>", player.getName());
            sender.sendMessage(miniMessage.deserialize(msg));
        }
        return true;
    }
}
