package serveressentials.serveressentials.economy;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;

public class ShopCommand implements CommandExecutor {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static File shopFile;
    private static FileConfiguration shopMessages;

    static {
        // Load messages on class load
        shopFile = new File(ServerEssentials.getInstance().getDataFolder(), "messages/shop.yml");
        if (!shopFile.exists()) {
            shopFile.getParentFile().mkdirs();
            ServerEssentials.getInstance().saveResource("messages/shop.yml", false);
        }
        shopMessages = YamlConfiguration.loadConfiguration(shopFile);
    }

    private Component getMessage(String path, String defaultMsg) {
        String raw = shopMessages.getString(path, defaultMsg);
        return mm.deserialize(raw);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("errors.only-players", "<red>Only players can use this command."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            ShopGUIManager.loadShopConfigs(new File(ServerEssentials.getInstance().getDataFolder(), "Shop"));
            sender.sendMessage(getMessage("messages.reloaded", "<green>Shop reloaded."));
        } else {
            ShopGUIManager.openMainGUI(player);
        }

        return true;
    }

    public static void reloadMessages() {
        shopMessages = YamlConfiguration.loadConfiguration(shopFile);
    }
}
