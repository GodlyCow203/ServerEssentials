package net.lunark.io.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

public class SleepCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public SleepCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Sleep.only-players");
            sender.sendMessage(msg);
            return true;
        }

        World world = player.getWorld();
        world.setTime(0);
        world.setStorm(false);
        world.setThundering(false);

        Component broadcastMsg = messages.get("Sleep.broadcast", "{player}", player.getName());
        Bukkit.broadcast(broadcastMsg);

        return true;
    }
}
