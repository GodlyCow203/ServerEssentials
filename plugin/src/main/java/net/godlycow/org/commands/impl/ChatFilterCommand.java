package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.ChatFilterConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class ChatFilterCommand implements Listener {
    private static final String BYPASS_PERM   = "serveressentials.command.chatfilter.bypass";
    private static final String SEE_BAD_PERM  = "serveressentials.command.chatfilter.see-bad-words";

    private final ChatFilterConfig config;
    private final Plugin plugin;
    private final PlayerLanguageManager lang;

    public ChatFilterCommand(Plugin plugin,
                             ChatFilterConfig config,
                             PlayerLanguageManager lang) {
        this.plugin = plugin;
        this.config = config;
        this.lang   = lang;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getPlayer().hasPermission(BYPASS_PERM)) return;

        String msg = e.getMessage();
        for (String word : config.blacklist) {
            if (Pattern.compile(Pattern.quote(word), Pattern.CASE_INSENSITIVE)
                    .matcher(msg).find()) {

                e.setCancelled(true);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Component formatted = lang.getMessageFor(null,
                            "chatfilter.staff Notice",
                            "<gray>[Filter] <white>{player}: {message}",
                            ComponentPlaceholder.of("{player}", e.getPlayer().getName()),
                            ComponentPlaceholder.of("{message}", msg));

                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission(SEE_BAD_PERM))
                            .forEach(p -> p.sendMessage(formatted));
                });

                e.getPlayer().sendMessage(
                        lang.getMessageFor(e.getPlayer(),
                                "chatfilter.blocked",
                                "<red>Your message contained inappropriate language."));

                return;
            }
        }
    }
}