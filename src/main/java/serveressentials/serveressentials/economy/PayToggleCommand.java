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

public class PayToggleCommand implements CommandExecutor {

    private static final Set<UUID> toggled = new HashSet<>();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static File messagesFile;
    private static org.bukkit.configuration.file.FileConfiguration messagesConfig;

    public PayToggleCommand() {
        loadMessages();
    }

    public static void loadMessages() {
        messagesFile = new File(ServerEssentials.getInstance().getDataFolder(), "messages/economy.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            ServerEssentials.getInstance().saveResource("messages/economy.yml", false);
        }
        messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(messagesFile);
    }

    private Component getPrefix() {
        return mm.deserialize(messagesConfig.getString("prefix", "<blue><bold>[SE]</bold> </blue>"));
    }

    public static boolean hasPaymentsDisabled(Player player) {
        return toggled.contains(player.getUniqueId());
    }

    private Component format(String path) {
        return mm.deserialize(messagesConfig.getString(path, path));
    }

    private Component format(String path, Object... placeholders) {
        String raw = messagesConfig.getString(path, path);
        if (placeholders.length % 2 != 0) return mm.deserialize(raw);
        for (int i = 0; i < placeholders.length; i += 2) {
            raw = raw.replace(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
        }
        return mm.deserialize(raw);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(format("pay.only-players"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (toggled.contains(uuid)) {
            toggled.remove(uuid);
            player.sendMessage(format("pay.toggle-enabled"));
        } else {
            toggled.add(uuid);
            player.sendMessage(format("pay.toggle-disabled"));
        }

        return true;
    }
}
