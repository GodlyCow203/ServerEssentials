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

import java.util.Collections;

public class SetLoreLineCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SetLoreLineCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();

        // Default messages
        messages.addDefault("setlore.usage", "<red>Usage: /setloreline <text>");
        messages.addDefault("setlore.no-player", "<red>Only players can use this command.");
        messages.addDefault("setlore.no-item", "<red>You're not holding an item.");
        messages.addDefault("setlore.lore-set", "<green>Lore set to: <white>{lore}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            playerSend(sender, "setlore.no-player");
            return true;
        }

        if (args.length == 0) {
            playerSend(player, "setlore.usage");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            playerSend(player, "setlore.no-item");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        // Join args into a single string
        String input = String.join(" ", args);

        // Deserialize MiniMessage string into a Component
        Component loreComponent = miniMessage.deserialize(input);

        // Set lore as a Component
        meta.lore(Collections.singletonList(loreComponent));
        item.setItemMeta(meta);

        // Send confirmation message
        playerSend(player, "setlore.lore-set", "{lore}", input);
        return true;
    }

    private void playerSend(Player player, String path, String... placeholders) {
        Component message = messages.get(path, placeholders);
        player.sendMessage(message);
    }

    private void playerSend(CommandSender sender, String path, String... placeholders) {
        Component message = messages.get(path, placeholders);
        sender.sendMessage(message);
    }
}
