package serveressentials.serveressentials.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PayConfirmToggleCommand implements CommandExecutor {

    private static final Set<UUID> toggled = new HashSet<>();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private static File messagesFile;
    private static FileConfiguration messagesConfig;

    static {
        messagesFile = new File(ServerEssentials.getInstance().getDataFolder(), "messages/economy.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            ServerEssentials.getInstance().saveResource("messages/economy.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static boolean hasConfirmationsDisabled(Player player) {
        return toggled.contains(player.getUniqueId());
    }

    private Component getMessage(String path, Object... placeholders) {
        String raw = messagesConfig.getString(path, path);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            raw = raw.replace(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
        }
        return mm.deserialize(raw);
    }

    private String getPrefix() {
        return messagesConfig.getString("prefix", "<blue><bold>[SE]</bold></blue> ");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getMessage("errors.only-players", "%prefix%", getPrefix()));
            return true;
        }

        UUID uuid = player.getUniqueId();
        boolean disabled;

        if (toggled.contains(uuid)) {
            toggled.remove(uuid);
            disabled = false;
        } else {
            toggled.add(uuid);
            disabled = true;
        }

        String statusKey = disabled ? "messages.pay-confirm-disabled" : "messages.pay-confirm-enabled";
        player.sendMessage(getMessage(statusKey, "%prefix%", getPrefix()));

        return true;
    }

    public static void reloadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
