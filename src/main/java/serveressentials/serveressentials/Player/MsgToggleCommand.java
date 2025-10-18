package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MsgToggleCommand implements CommandExecutor {

    private static final Set<UUID> toggled = new HashSet<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ServerEssentials plugin;
    private final File file;
    private FileConfiguration config;

    public MsgToggleCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "storage/msgtoggle.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create msgtoggle.yml!");
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        loadToggled();
    }

    private void loadToggled() {
        List<String> uuids = config.getStringList("toggled");
        for (String s : uuids) {
            try {
                toggled.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveToggled() {
        List<String> uuids = toggled.stream().map(UUID::toString).toList();
        config.set("toggled", uuids);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save msgtoggle.yml!");
            e.printStackTrace();
        }
    }

    public static boolean hasMessagesDisabled(Player player) {
        return toggled.contains(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!"));
            return true;
        }

        if (toggled.contains(player.getUniqueId())) {
            toggled.remove(player.getUniqueId());
            saveToggled();
            player.sendMessage(miniMessage.deserialize("<green>Private messages enabled!"));
        } else {
            toggled.add(player.getUniqueId());
            saveToggled();
            player.sendMessage(miniMessage.deserialize("<yellow>Private messages disabled!"));
        }

        return true;
    }

    /**
     * Save on plugin shutdown
     */
    public void saveOnDisable() {
        saveToggled();
    }
}
