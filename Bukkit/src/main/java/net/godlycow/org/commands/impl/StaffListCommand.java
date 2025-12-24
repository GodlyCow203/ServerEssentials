package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.StaffListConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class StaffListCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.stafflist";
    private final PlayerLanguageManager langManager;
    private final StaffListConfig config;

    public StaffListCommand(PlayerLanguageManager langManager, StaffListConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player playerSender = (sender instanceof Player) ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.stafflist.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        List<String> staffOnline = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("serveressentials.stafflist")) {
                staffOnline.add(player.getName());
            }
        }

        sender.sendMessage(langManager.getMessageFor(playerSender, "commands.stafflist.header", "<#00ffff>--- Online Staff ---"));

        if (staffOnline.isEmpty()) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.stafflist.none", "<#aaaaaa>No staff members online."));
        } else {
            for (String name : staffOnline) {
                Component line = langManager.getMessageFor(playerSender, "commands.stafflist.format", "<#00ff00>• <#ffff00>{player}", ComponentPlaceholder.of("{player}", name));
                sender.sendMessage(line);
            }
        }

        return true;
    }
}