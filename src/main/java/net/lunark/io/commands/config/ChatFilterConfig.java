package net.lunark.io.commands.config;

import org.bukkit.plugin.Plugin;
import java.util.List;

public final class ChatFilterConfig {
    public final List<String> blacklist;

    public ChatFilterConfig(Plugin plugin) {
        blacklist = plugin.getConfig().getStringList("chat-filter.blacklist");
    }
}