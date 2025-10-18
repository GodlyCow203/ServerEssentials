package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class RenameItemCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RenameItemCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();

        // Add default messages if missing
        messages.addDefault("rename.usage", "<red>Usage: /rename <name>");
        messages.addDefault("rename.no-item", "<red>You're not holding an item.");
        messages.addDefault("rename.no-meta", "<red>Could not get item meta.");
        messages.addDefault("rename.renamed", "<green>Item renamed to: <white>{item}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("rename.usage"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messages.get("rename.usage"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(messages.get("rename.no-item"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(messages.get("rename.no-meta"));
            return true;
        }

        // Join args into one string and deserialize MiniMessage
        String input = String.join(" ", args);
        Component displayName = miniMessage.deserialize(input);

        // Set display name as a Component (modern Paper API)
        meta.displayName(displayName);
        item.setItemMeta(meta);

        // Send feedback message
        player.sendMessage(messages.get("rename.renamed", "{item}", input));

        return true;
    }
}
