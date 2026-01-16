package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public class BanConfig {
    private final Plugin plugin;

    public final String serverName;
    public final String discordLink;

    public BanConfig(Plugin plugin) {
        this.plugin = plugin;
        this.serverName = plugin.getConfig().getString("bans.server-name", "MyCoolServer");
        this.discordLink = plugin.getConfig().getString("bans.discord-link", "https://discord.gg/example");
    }
}