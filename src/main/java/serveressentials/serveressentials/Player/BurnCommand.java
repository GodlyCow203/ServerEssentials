package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

public class BurnCommand implements CommandExecutor {

    private final PlayerMessages playerMessages;

    public BurnCommand(PlayerMessages playerMessages) {
        this.playerMessages = playerMessages;

        // Add default messages if missing
        playerMessages.addDefault("burn.no_permission", "<red>You do not have permission.");
        playerMessages.addDefault("burn.usage", "<red>Usage: /burn <player> [seconds]");
        playerMessages.addDefault("burn.player_not_found", "<red>Player not found.");
        playerMessages.addDefault("burn.immune_gamemode", "<red>That player is in a game mode that is immune to fire.");
        playerMessages.addDefault("burn.invalid_number", "<red>Invalid number.");
        playerMessages.addDefault("burn.target_burned", "<red>You have been set on fire by <yellow>{sender}</yellow>!");
        playerMessages.addDefault("burn.sender_burned", "<yellow>{target}</yellow><red> has been set on fire for <yellow>{seconds}</yellow> seconds.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.burn")) {
            sender.sendMessage(playerMessages.get("burn.no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(playerMessages.get("burn.usage"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(playerMessages.get("burn.player_not_found"));
            return true;
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(playerMessages.get("burn.immune_gamemode"));
            return true;
        }

        int seconds = 5; // Default
        if (args.length == 2) {
            try {
                seconds = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(playerMessages.get("burn.invalid_number"));
                return true;
            }
        }

        target.setFireTicks(seconds * 20);

        // Send configurable messages with placeholders
        target.sendMessage(playerMessages.get("burn.target_burned", "{sender}", sender.getName()));
        sender.sendMessage(playerMessages.get("burn.sender_burned", "{target}", target.getName(), "{seconds}", String.valueOf(seconds)));

        return true;
    }
}
