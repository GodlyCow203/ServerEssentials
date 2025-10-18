package serveressentials.serveressentials.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import serveressentials.serveressentials.Managers.RulesManager;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RulesCommand implements CommandExecutor, TabCompleter {

    private final RulesManager rulesManager;
    private final PlayerMessages messages;

    public RulesCommand(RulesManager rulesManager, PlayerMessages messages) {
        this.rulesManager = rulesManager;
        this.messages = messages;

        // Add default messages if missing
        messages.addDefault("Rules.no-permission", "<red>You don't have permission to perform this action.");
        messages.addDefault("Rules.reloaded", "<green>Rules reloaded.");
        messages.addDefault("Rules.header", "<yellow>---- Server Rules ----");
        messages.addDefault("Rules.rule-line", "<gray>{rule}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload subcommand
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.rules.reload")) {
                sender.sendMessage(messages.get("Rules.no-permission"));
                return true;
            }

            rulesManager.reloadRules();
            sender.sendMessage(messages.get("Rules.reloaded"));
            return true;
        }

        // View rules permission check
        if (!sender.hasPermission("serveressentials.rules")) {
            sender.sendMessage(messages.get("Rules.no-permission"));
            return true;
        }

        // Display rules
        sender.sendMessage(messages.get("Rules.header"));
        for (String rule : rulesManager.getRules()) {
            sender.sendMessage(messages.get("Rules.rule-line", "{rule}", rule));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("serveressentials.rules.reload")) {
                return Collections.singletonList("reload");
            }
        }
        return new ArrayList<>();
    }
}
