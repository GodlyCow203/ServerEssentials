package net.lunark.io.mail;

import org.bukkit.plugin.Plugin;

public class MailConfig {
    private final Plugin plugin;

    public int cooldown;
    public int maxLength;
    public int joinNotifyDelay;

    public MailConfig(Plugin plugin) {
        this.plugin = plugin;
        plugin.getConfig().addDefault("mail.cooldown", 5);
        plugin.getConfig().addDefault("mail.max-length", 500);
        plugin.getConfig().addDefault("mail.join-notify-delay", 40);
        plugin.saveConfig();

        load();
    }

    public void load() {
        this.cooldown = plugin.getConfig().getInt("mail.cooldown", 5);
        this.maxLength = plugin.getConfig().getInt("mail.max-length", 500);
        this.joinNotifyDelay = plugin.getConfig().getInt("mail.join-notify-delay", 40);
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }
}