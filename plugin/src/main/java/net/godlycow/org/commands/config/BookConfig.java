package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public final class BookConfig {
    public static final String PERMISSION = "serveressentials.command.book";
    public final String title;
    public final String author;

    public BookConfig(Plugin plugin) {
        this.title = plugin.getConfig().getString("book.title", "ServerEssentials");
        this.author = plugin.getConfig().getString("book.author", "SE Plugin");
    }
}