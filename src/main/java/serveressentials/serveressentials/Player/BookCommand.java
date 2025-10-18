package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.List;

public class BookCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BookCommand(PlayerMessages messages) {
        this.messages = messages;

        // Add default messages (only sets if missing)
        messages.addDefault("Book.Messages.PlayerOnly", "<red>Only players can use this command.");
        messages.addDefault("Book.Messages.NoPermission", "<red>You do not have permission to use this command.");
        messages.addDefault("Book.Messages.Success", "<green>Book added to your inventory!");
        messages.addDefault("Book.Title", "ServerEssentials");
        messages.addDefault("Book.Author", "SE Plugin");
        messages.addDefault("Book.Pages", String.valueOf(List.of("This is a sample book!")));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Book.Messages.PlayerOnly"));
            return true;
        }

        if (!player.hasPermission("serveressentials.book")) {
            player.sendMessage(messages.get("Book.Messages.NoPermission"));
            return true;
        }

        // Create written book
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // Get values from config/messages
        String title = messages.getConfig().getString("Book.Title", "ServerEssentials");
        String author = messages.getConfig().getString("Book.Author", "SE Plugin");
        List<String> pages = messages.getConfig().getStringList("Book.Pages");

        meta.setTitle(title);
        meta.setAuthor(author);

        // Add pages with MiniMessage parsing
        for (String page : pages) {
            meta.addPage(miniMessage.deserialize(page).toString());
        }

        book.setItemMeta(meta);
        player.getInventory().addItem(book);

        player.sendMessage(messages.get("Book.Messages.Success"));
        return true;
    }
}
