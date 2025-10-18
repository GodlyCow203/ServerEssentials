package serveressentials.serveressentials.nick;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NickManager implements CommandExecutor {

    private static NickManager instance; // Static instance for global access

    private final MiniMessage mini = MiniMessage.miniMessage();

    private final File nickConfigFile;
    private final File messagesFile;
    private final File storageFile;

    private FileConfiguration nickConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration storageConfig;

    private final Map<Player, String> nickCache = new HashMap<>();

    // Constructor
    public NickManager(File dataFolder) {
        instance = this;
        this.nickConfigFile = new File(dataFolder, "config/nick/nick.yml");
        this.messagesFile = new File(dataFolder, "messages/player.yml");
        this.storageFile = new File(dataFolder, "storage/nicks.yml");
        loadConfigs();
        loadNicks();
    }

    // Static reload method
    public static void reload() {
        if (instance != null) {
            instance.reloadConfigs();
        } else {
            Bukkit.getLogger().warning("[NickManager] Reload called before initialization!");
        }
    }

    // Load configuration files from disk
    private void loadConfigs() {
        if (!nickConfigFile.exists()) nickConfigFile.getParentFile().mkdirs();
        if (!messagesFile.exists()) messagesFile.getParentFile().mkdirs();
        if (!storageFile.exists()) storageFile.getParentFile().mkdirs();

        nickConfig = YamlConfiguration.loadConfiguration(nickConfigFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
    }

    // Save nick storage to disk
    public void saveStorage() {
        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load saved nicknames for online players
    private void loadNicks() {
        for (String uuid : storageConfig.getKeys(false)) {
            String nick = storageConfig.getString(uuid);
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null && nick != null) {
                applyNick(p, nick);
            }
        }
    }

    // Apply nickname to a player
    private void applyNick(Player player, String nick) {
        Component displayName = mini.deserialize(nick);
        player.displayName(displayName);
        player.playerListName(displayName);
        nickCache.put(player, nick);
    }

    // Reset nickname to default
    private void resetNick(Player player) {
        player.displayName(Component.text(player.getName()));
        player.playerListName(Component.text(player.getName()));
        nickCache.remove(player);
        storageConfig.set(player.getUniqueId().toString(), null);
        saveStorage();
    }

    // Helper method for getting messages with placeholders
    private String msg(String path, Map<String, String> placeholders) {
        String raw = messagesConfig.getString(path, "<red>Message not found: " + path);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return raw;
    }

    // Validate nickname input
    private boolean validateNick(Player p, String nick) {
        int minLen = nickConfig.getInt("nick-settings.min-length", 3);
        int maxLen = nickConfig.getInt("nick-settings.max-length", 16);
        boolean allowFormatting = nickConfig.getBoolean("nick-settings.allow-formatting", true);
        List<String> blocked = nickConfig.getStringList("nick-settings.blocked-words");

        String stripped = nick.replaceAll("<.*?>", ""); // remove MiniMessage tags for length check

        if (stripped.length() < minLen || stripped.length() > maxLen) {
            p.sendMessage(mini.deserialize(msg("nick.invalid_length",
                    Map.of("min", String.valueOf(minLen), "max", String.valueOf(maxLen)))));
            return false;
        }

        for (String word : blocked) {
            if (stripped.toLowerCase().contains(word.toLowerCase())) {
                p.sendMessage(mini.deserialize(msg("nick.blocked_word", Map.of("word", word))));
                return false;
            }
        }

        if (!allowFormatting && nick.contains("<")) {
            p.sendMessage(mini.deserialize(msg("nick.no_formatting", null)));
            return false;
        }

        return true;
    }

    // Command handling
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("nick")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use /nick");
                return true;
            }

            Player p = (Player) sender;

            if (args.length < 1) {
                p.sendMessage(mini.deserialize(msg("nick.usage", null)));
                return true;
            }

            if (args[0].equalsIgnoreCase("reset")) {
                resetNick(p);
                p.sendMessage(mini.deserialize(msg("nick.reset_self", null)));
                return true;
            }

            String newNick = String.join(" ", args);

            if (!validateNick(p, newNick)) return true;

            applyNick(p, newNick);
            storageConfig.set(p.getUniqueId().toString(), newNick);
            saveStorage();
            p.sendMessage(mini.deserialize(msg("nick.set", Map.of("nick", newNick))));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nicks")) {
            // /nicks reload
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                NickManager.reload();
                sender.sendMessage(mini.deserialize(msg("nicks.reload", null)));
                return true;
            }

            // /nicks reset <player>
            if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    resetNick(target);
                    sender.sendMessage(mini.deserialize(msg("nicks.reset", Map.of("player", target.getName()))));
                } else {
                    sender.sendMessage(mini.deserialize(msg("nicks.not_found", Map.of("player", args[1]))));
                }
                return true;
            }
        }

        return false;
    }

    // Reload all configurations and reapply nicknames
    private void reloadConfigs() {
        nickConfig = YamlConfiguration.loadConfiguration(nickConfigFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
        reloadNicks();
    }

    // Reapply stored nicks to online players
    private void reloadNicks() {
        nickCache.clear();
        for (String uuid : storageConfig.getKeys(false)) {
            String nick = storageConfig.getString(uuid);
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if (player != null && nick != null) {
                applyNick(player, nick);
            }
        }
    }
}
