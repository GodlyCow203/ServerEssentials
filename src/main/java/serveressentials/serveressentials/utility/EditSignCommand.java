package serveressentials.serveressentials.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class EditSignCommand implements CommandExecutor {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final PlayerMessages playerMessages;

    public EditSignCommand(ServerEssentials plugin) {
        this.playerMessages = new PlayerMessages(plugin);

        // Ensure defaults exist
        playerMessages.addDefault("EditSign.Messages.OnlyPlayers", "<red>Only players can use this command.");
        playerMessages.addDefault("EditSign.Messages.NotLookingAtSign", "<red>You are not looking at a sign.");
        playerMessages.addDefault("EditSign.Messages.Usage", "<yellow>Usage: /editsign <line1>|<line2>|<line3>|<line4>");
        playerMessages.addDefault("EditSign.Messages.Success", "<green>Sign updated successfully!");
    }

    private Component getMessage(String path, String... placeholders) {
        return playerMessages.get(path, placeholders);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("EditSign.Messages.OnlyPlayers"));
            return true;
        }

        Block target = player.getTargetBlockExact(5);

        if (target == null || !target.getType().name().contains("SIGN")) {
            player.sendMessage(getMessage("EditSign.Messages.NotLookingAtSign"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(getMessage("EditSign.Messages.Usage"));
            return true;
        }

        // Combine arguments into one string, then split by '|'
        String input = String.join(" ", args);
        String[] lines = input.split("\\|");

        Sign sign = (Sign) target.getState();
        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            // Deserialize MiniMessage -> Component -> legacy string with & codes
            String line = LegacyComponentSerializer.legacySection().serialize(mm.deserialize(lines[i].trim()));
            sign.setLine(i, line);
        }
        sign.update();

        player.sendMessage(getMessage("EditSign.Messages.Success"));
        return true;
    }
}
