package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RulesCommand implements CommandExecutor {

    private final RulesManager rulesManager;

    public RulesCommand(RulesManager rulesManager) {
        this.rulesManager = rulesManager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.rules.reload")) {
                sender.sendMessage(prefix + ChatColor.RED + "You don't have permission to reload the rules.");
                return true;
            }

            rulesManager.reloadRules();
            sender.sendMessage(prefix + ChatColor.GREEN + "Rules reloaded.");
            return true;
        }

        sender.sendMessage(prefix + ChatColor.YELLOW + "---- Server Rules ----");
        for (String rule : rulesManager.getRules()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', rule));
        }

        return true;
    }
}
