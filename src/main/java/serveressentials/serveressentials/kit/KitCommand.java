

package serveressentials.serveressentials.kit;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
        import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.util.*;

public class KitCommand implements CommandExecutor {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final KitGUIListener guiListener;

    public KitCommand() {
        this.guiListener = new KitGUIListener(this); // Link the GUI to this command handler
        // Register listener
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.kit.reload")) {
                sender.sendMessage(ServerEssentials.getInstance().getKitMessages().get("reload-no-permission", "%player%", sender.getName()));
                return true;
            }

            KitConfigManager.reload();
            KitManager.loadKits(KitConfigManager.getConfig());
            sender.sendMessage(ServerEssentials.getInstance().getKitMessages().get("reload-success", "%player%", sender.getName()));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ServerEssentials.getInstance().getKitMessages().get("only-players", "%player%", sender.getName()));
            return true;
        }

        guiListener.openKitGUI(player);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Example: return a list of available kit names
        List<String> suggestions = new ArrayList<>();
        FileConfiguration config = KitConfigManager.getConfig();
        ConfigurationSection kits = config.getConfigurationSection("kits");
        if (kits != null) {
            for (String kit : kits.getKeys(false)) {
                if (args.length == 1 && kit.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(kit);
                }
            }
        }
        return suggestions;
    }


    public Map<UUID, Map<String, Long>> getCooldowns() {
        return cooldowns;
    }

    public KitGUIListener getGuiListener() {
        return guiListener;
    }
}









