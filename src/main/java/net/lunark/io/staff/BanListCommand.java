package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class BanListCommand implements CommandExecutor {

    private final BanManager banManager;
    private final MessagesManager messages;

    public BanListCommand(ServerEssentials plugin, BanManager banManager) {
        this.banManager = banManager;
        this.messages = new MessagesManager(plugin);

        // Default messages for ban list
        messages.addDefault("banlist.no-permission", "<red>You don't have permission.");
        messages.addDefault("banlist.empty", "<yellow>There are no banned players.");
        messages.addDefault("banlist.header", "<gold>------[ Banned Players ]------");
        messages.addDefault("banlist.format", "<red>%player% <gray>- <white>%reason% <gray>(<aqua>%time%</aqua>)");
        messages.addDefault("banlist.footer", "<gold>-----------------------------");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.ban.use")) {
            sender.sendMessage(messages.getMessageComponent("banlist.no-permission"));
            return true;
        }

        Set<String> bannedUUIDs = banManager.getAllBannedUUIDs();
        if (bannedUUIDs.isEmpty()) {
            sender.sendMessage(messages.getMessageComponent("banlist.empty"));
            return true;
        }

        sender.sendMessage(messages.getMessageComponent("banlist.header"));

        for (String uuid : bannedUUIDs) {
            String name = banManager.getNameFromUUID(uuid);
            String reason = banManager.getReason(uuid);
            long until = banManager.getUntil(uuid);

            String time = (until == -1)
                    ? "Permanent"
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(until));

            Component line = messages.getMessageComponent(
                    "banlist.format",
                    "%player%", name,
                    "%reason%", reason,
                    "%time%", time
            );

            sender.sendMessage(line);
        }

        sender.sendMessage(messages.getMessageComponent("banlist.footer"));
        return true;
    }
}
