package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class SpeedCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public SpeedCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Speed.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length != 1) {
            Component usageMsg = messages.get("Speed.usage");
            player.sendMessage(usageMsg);
            return true;
        }

        try {
            int speed = Integer.parseInt(args[0]);
            if (speed < 1 || speed > 10) throw new NumberFormatException();

            float scaled = speed / 10.0f;

            if (player.isFlying()) {
                player.setFlySpeed(scaled);
                Component flyMsg = messages.get("Speed.fly-set", "{speed}", String.valueOf(speed));
                player.sendMessage(flyMsg);
            } else {
                player.setWalkSpeed(scaled);
                Component walkMsg = messages.get("Speed.walk-set", "{speed}", String.valueOf(speed));
                player.sendMessage(walkMsg);
            }

        } catch (NumberFormatException e) {
            Component invalidMsg = messages.get("Speed.invalid-number");
            player.sendMessage(invalidMsg);
        }

        return true;
    }
}
