package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.EnderSeeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class EnderSeeCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "essc.command.endersee";
    private static final String PERMISSION_OTHERS = "essc.command.endersee.others";

    private final PlayerLanguageManager langManager;
    private final EnderSeeConfig config;

    public EnderSeeCommand(PlayerLanguageManager langManager, EnderSeeConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.endersee.only-player",
                    "<red>Only players can use this command!"));
            return true;
        }

        boolean hasSelfPermission = player.hasPermission(PERMISSION);
        boolean hasOthersPermission = player.hasPermission(PERMISSION_OTHERS);

        if (args.length == 0) {
            if (!hasSelfPermission) {
                player.sendMessage(langManager.getMessageFor(player, "commands.endersee.no-permission",
                        "<red>You don't have permission to view your ender chest!"));
                return true;
            }
            openEnderChest(player, player, false);
            return true;
        }

        if (args.length == 1) {
            if (!hasOthersPermission) {
                player.sendMessage(langManager.getMessageFor(player, "commands.endersee.no-permission-others",
                        "<red>You don't have permission to view other players' ender chests!"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.endersee.player-not-found",
                        "<red>Player not found: <yellow>{player}</yellow>",
                        ComponentPlaceholder.of("{player}", args[0])));
                return true;
            }

            openEnderChest(player, target, true);
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(player, "commands.endersee.usage",
                "<red>Usage: /endersee [player]"));
        return true;
    }

    private void openEnderChest(Player viewer, Player target, boolean isOther) {
        Inventory enderChest = target.getEnderChest();

        viewer.openInventory(enderChest);

        if (isOther) {
            viewer.sendMessage(langManager.getMessageFor(viewer, "commands.endersee.opened-other",
                    "<green>Opened ender chest of <white>{player}</white>.",
                    ComponentPlaceholder.of("{player}", target.getName())));
        } else {
            viewer.sendMessage(langManager.getMessageFor(viewer, "commands.endersee.opened-self",
                    "<green>Opened your ender chest."));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission(PERMISSION_OTHERS)) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }

            String input = args[0].toLowerCase();
            suggestions.removeIf(name -> !name.toLowerCase().startsWith(input));
        }

        return suggestions;
    }
}