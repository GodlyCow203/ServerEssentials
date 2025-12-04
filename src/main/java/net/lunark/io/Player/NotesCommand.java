package net.lunark.io.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NotesCommand implements CommandExecutor, TabCompleter {

    private final Map<UUID, Map<String, String>> playerNotes = new HashMap<>();
    private final PlayerMessages messages;
    private final ServerEssentials plugin;

    private final File notesFile;
    private FileConfiguration notesConfig;

    public NotesCommand(PlayerMessages messages, ServerEssentials plugin) {
        this.messages = messages;
        this.plugin = plugin;

        notesFile = new File(plugin.getDataFolder(), "storage/notes.yml");
        if (!notesFile.getParentFile().exists()) notesFile.getParentFile().mkdirs();
        if (!notesFile.exists()) {
            try {
                notesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        notesConfig = YamlConfiguration.loadConfiguration(notesFile);
        loadNotes();
    }

    private void loadNotes() {
        for (String uuidStr : notesConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<String, String> notes = new HashMap<>();
            for (String noteName : notesConfig.getConfigurationSection(uuidStr).getKeys(false)) {
                notes.put(noteName, notesConfig.getString(uuidStr + "." + noteName));
            }
            playerNotes.put(uuid, notes);
        }
    }

    public void saveNotes() {
        for (UUID uuid : playerNotes.keySet()) {
            for (Map.Entry<String, String> entry : playerNotes.get(uuid).entrySet()) {
                notesConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
            }
        }
        try {
            notesConfig.save(notesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = messages.get("notes.only-players");
            sender.sendMessage(msg);
            return true;
        }

        UUID uuid = player.getUniqueId();
        playerNotes.putIfAbsent(uuid, new HashMap<>());
        Map<String, String> notes = playerNotes.get(uuid);

        if (args.length == 1) {
            if (!player.hasPermission("serveressentials.note.read")) {
                Component msg = messages.get("notes.no-permission-read");
                player.sendMessage(msg);
                return true;
            }

            String noteName = args[0].toLowerCase();
            if (notes.containsKey(noteName)) {
                String content = notes.get(noteName);
                Component msg = messages.get("notes.view", "<name>", noteName, "<content>", content);
                player.sendMessage(msg);
            } else {
                Component msg = messages.get("notes.not-found", "<name>", noteName);
                player.sendMessage(msg);
            }
            return true;

        } else if (args.length >= 2) {
            if (!player.hasPermission("serveressentials.note.write")) {
                Component msg = messages.get("notes.no-permission-write");
                player.sendMessage(msg);
                return true;
            }

            String noteName = args[0].toLowerCase();
            String noteContent = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            notes.put(noteName, noteContent);

            Component msg = messages.get("notes.saved", "<name>", noteName, "<content>", noteContent);
            player.sendMessage(msg);

            saveNotes();
            return true;

        } else {
            player.sendMessage(messages.get("notes.usage-header"));

            if (player.hasPermission("serveressentials.note.write")) {
                player.sendMessage(messages.get("notes.usage-add"));
            }
            if (player.hasPermission("serveressentials.note.read")) {
                player.sendMessage(messages.get("notes.usage-view"));
            }
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        UUID uuid = player.getUniqueId();
        Map<String, String> notes = playerNotes.getOrDefault(uuid, Collections.emptyMap());

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if (player.hasPermission("serveressentials.note.read")) {
                for (String noteName : notes.keySet()) {
                    if (noteName.startsWith(input)) {
                        completions.add(noteName);
                    }
                }
            }
            return completions;
        }

        return List.of();
    }
}
