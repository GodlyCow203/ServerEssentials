package net.godlycow.org.commands.impl;

import net.godlycow.org.afk.AFKManager;
import net.godlycow.org.commands.config.AFKConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class AFKCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.afk";
    private static final String RELOAD_PERMISSION = "essc.command.afk.reload";

    private final PlayerLanguageManager langManager;
    private final AFKConfig config;
    private final AFKManager afkManager;

    public AFKCommand(PlayerLanguageManager langManager, AFKConfig config, AFKManager afkManager) {
        this.langManager = langManager;
        this.config = config;
        this.afkManager = afkManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!config.enabled) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.afk.disabled",
                    "<red>The AFK system is currently disabled."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        return handleAfkToggle(sender);
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                    "commands.afk.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", RELOAD_PERMISSION)));
            return true;
        }

        config.reload();
        afkManager.reload();
        sender.sendMessage(langManager.getMessageFor(sender instanceof Player p ? p : null,
                "commands.afk.reload",
                "<green>AFK system reloaded successfully."));
        return true;
    }

    private boolean handleAfkToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.afk.only-player",
                    "<red>Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.afk.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        afkManager.toggleAFK(player);
        return true;
    }
}