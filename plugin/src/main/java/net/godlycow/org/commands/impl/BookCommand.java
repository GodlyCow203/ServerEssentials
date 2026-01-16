package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.godlycow.org.commands.config.BookConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BookCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final BookConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final GsonComponentSerializer gsonSerializer = GsonComponentSerializer.gson();

    public BookCommand(Plugin plugin, PlayerLanguageManager langManager, BookConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.book.command.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(BookConfig.PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.book.command.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", BookConfig.PERMISSION)));
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        List<String> pageStrings = plugin.getConfig().getStringList("book.pages");

        if (pageStrings.isEmpty()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.book.command.empty-pages",
                    "<yellow>No book pages configured in config.yml!"));
            return true;
        }

        meta.setTitle(config.title);
        meta.setAuthor(config.author);

        for (String pageContent : pageStrings) {
            Component pageComponent = miniMessage.deserialize(pageContent);
            String jsonPage = gsonSerializer.serialize(pageComponent);
            meta.addPage(jsonPage);
        }

        book.setItemMeta(meta);
        player.getInventory().addItem(book);

        player.sendMessage(langManager.getMessageFor(player, "commands.book.command.success",
                "<green>Book added to your inventory!"));
        return true;
    }
}