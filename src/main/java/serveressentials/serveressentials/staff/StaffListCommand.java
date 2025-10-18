package serveressentials.serveressentials.staff;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.ArrayList;
import java.util.List;

public class StaffListCommand implements CommandExecutor {

    private final MessagesManager messages;
    private final BukkitAudiences adventure;

    public StaffListCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);
        this.adventure = BukkitAudiences.create(plugin);

        // Add default messages for staff list (MiniMessage format)
        messages.addDefault("stafflist.header", "<#00ffff>--- Online Staff ---");
        messages.addDefault("stafflist.none", "<#aaaaaa>No staff members online.");
        messages.addDefault("stafflist.format", "<#00ff00>• <#ffff00><player>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> staffOnline = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("serveressentials.stafflist")) {
                staffOnline.add(player.getName());
            }
        }

        // Send header
        send(sender, messages.getMessageComponent("stafflist.header"));

        if (staffOnline.isEmpty()) {
            send(sender, messages.getMessageComponent("stafflist.none"));
        } else {
            // Send each staff member with placeholder replacement
            for (String name : staffOnline) {
                Component line = messages.getMessageComponent("stafflist.format", "<player>", name);
                send(sender, line);
            }
        }

        return true;
    }

    private void send(CommandSender sender, Component component) {
        adventure.sender(sender).sendMessage(component);
    }
}
