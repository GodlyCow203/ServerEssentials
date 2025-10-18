package serveressentials.serveressentials.server;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.util.ServerMessages;

public class UptimeCommand implements CommandExecutor {

    private final long serverStartTime;
    private final ServerMessages messages;

    public UptimeCommand(long serverStartTime, ServerMessages messages) {
        this.serverStartTime = serverStartTime;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - serverStartTime;

        long seconds = uptimeMillis / 1000 % 60;
        long minutes = uptimeMillis / (1000 * 60) % 60;
        long hours = uptimeMillis / (1000 * 60 * 60) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        String uptimeStr = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);

        Component msg = messages.get("uptime.info", "<uptime>", uptimeStr);
        sender.sendMessage(msg);

        return true;
    }
}
