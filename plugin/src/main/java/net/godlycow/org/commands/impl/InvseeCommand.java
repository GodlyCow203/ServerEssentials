package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.InvseeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class InvseeCommand implements TabExecutor {
    private static final String PERMISSION = "serveressentials.command.invsee";

    private final PlayerLanguageManager langManager;
    private final InvseeConfig config;

    public InvseeCommand(PlayerLanguageManager langManager, InvseeConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.invsee.only-player", "<#B22222>❌ Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.invsee.no-permission", "<#B22222>❌ You need permission <#FFD900>{permission}</#FFD900>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        if (args.length > 1 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.invsee.player-not-found", "<#B22222>❌ Player not found: <white>{player}</white>", ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        player.openInventory(target.getInventory());
        player.sendMessage(langManager.getMessageFor(player, "commands.invsee.success", "<#50DB00>✔ Viewing <white>{player}</white>'s inventory.", ComponentPlaceholder.of("{player}", target.getName())));
        return true;
    }

    private void sendHelpMessage(Player player) {
        Component help = Component.empty()
                .append(langManager.getMessageFor(player, "commands.invsee.help.header", "<#FFD900><bold>=== Invsee Command Help ===</bold></#FFD900>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.invsee.help.description", "<#FFD900>Description: <white>View another player's inventory.</white>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.invsee.help.usage", "<#FFD900>Usage:</#FFD900> <white>/invsee <player></white>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.invsee.help.permission", "<#FFD900>Required Permission:</#FFD900> <white>{permission}</white>",
                        ComponentPlaceholder.of("{permission}", PERMISSION)))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.invsee.help.examples.header", "<#FFD900>Examples:</#FFD900>"))
                .append(Component.newline())
                .append(langManager.getMessageFor(player, "commands.invsee.help.examples.example1", "  <white>/invsee {player}</white> <gray>- View player's inventory</gray>",
                        ComponentPlaceholder.of("{player}", "Steve")))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.invsee.help.footer", "<gray>Use <white>/invsee help</white> to see this message again.</gray>"));

        player.sendMessage(help);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (!player.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if ("help".startsWith(input)) {
                completions.add("help");
            }

            completions.addAll(
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList())
            );

            return completions;
        }

        return Collections.emptyList();
    }
}