package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.HashSet;
import java.util.Set;

public class AdminChatCommand implements CommandExecutor {

    private static final Set<Player> toggled = new HashSet<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        MessagesManager messages = ServerEssentials.getInstance().getMessagesManager();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.getMessageComponent("adminchat.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.adminchat")) {
            player.sendMessage(messages.getMessageComponent("adminchat.no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Toggle admin chat mode
            boolean isNowToggled;
            if (toggled.contains(player)) {
                toggled.remove(player);
                isNowToggled = false;
            } else {
                toggled.add(player);
                isNowToggled = true;
            }

            Component toggleMessage = messages.getMessageComponent(
                    "adminchat.toggle",
                    "%state%", isNowToggled ? "ON" : "OFF"
            );

            player.sendMessage(toggleMessage);

        } else {
            // Send a message to admin chat
            String message = String.join(" ", args);
            AdminChatUtils.sendAdminMessage(player, message);
        }

        return true;
    }

    public static boolean isInAdminChat(Player player) {
        return toggled.contains(player);
    }
}
