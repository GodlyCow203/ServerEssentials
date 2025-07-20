package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class KitCommand implements CommandExecutor {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final KitGUIListener guiListener;

    public KitCommand() {
        this.guiListener = new KitGUIListener(this); // Link the GUI to this command handler
        // Register listener
        ServerEssentials.getInstance().getServer().getPluginManager().registerEvents(guiListener, ServerEssentials.getInstance());
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.kit.reload")) {
                sender.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to reload kits.");
                return true;
            }

            KitConfigManager.reload();
            KitManager.loadKits(KitConfigManager.getConfig());
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Kit configuration reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // Open the new GUI
        guiListener.openKitGUI(player);
        return true;
    }

    public Map<UUID, Map<String, Long>> getCooldowns() {
        return cooldowns;
    }

    public KitGUIListener getGuiListener() {
        return guiListener;
    }
}
