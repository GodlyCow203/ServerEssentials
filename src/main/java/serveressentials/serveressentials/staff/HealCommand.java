package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

public class HealCommand implements CommandExecutor {

    private final MessagesManager messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public HealCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Add default messages for /heal
        messages.addDefault("heal.no-permission", "<red>You don't have permission.");
        messages.addDefault("heal.only-players", "<red>Only players can use this command.");
        messages.addDefault("heal.success", "<green>You have been healed!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("heal.only-players", null));
            return true;
        }

        if (!player.hasPermission("serveressentials.heal")) {
            player.sendMessage(getMessage("heal.no-permission", null));
            return true;
        }

        // Heal player
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);

        player.sendMessage(getMessage("heal.success", player.getName()));
        return true;
    }

    private Component getMessage(String path, String playerName) {
        String msg = messages.getConfig().getString(path, "<red>Missing message for " + path);
        if (playerName != null) {
            msg = msg.replace("%player%", playerName);
        }
        return miniMessage.deserialize(msg);
    }
}
