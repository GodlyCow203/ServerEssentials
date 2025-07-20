package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class NotesCommand implements CommandExecutor, TabCompleter {

    // Map of player UUID -> (noteName -> noteContent)
    private final Map<UUID, Map<String, String>> playerNotes = new HashMap<>();

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        playerNotes.putIfAbsent(uuid, new HashMap<>());
        Map<String, String> notes = playerNotes.get(uuid);

        if (args.length == 1) {
            // View a note by name
            String noteName = args[0].toLowerCase();
            if (notes.containsKey(noteName)) {
                sender.sendMessage(prefix + ChatColor.YELLOW + "Note '" + noteName + "': " + ChatColor.WHITE + notes.get(noteName));
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "No note found with name '" + noteName + "'.");
            }
            return true;
        } else if (args.length >= 2) {
            // Add/update a note by name
            String noteName = args[0].toLowerCase();
            String noteContent = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            notes.put(noteName, noteContent);
            sender.sendMessage(prefix + ChatColor.GREEN + "Saved note '" + noteName + "': " + noteContent);
            return true;
        } else {
            // Invalid usage
            sender.sendMessage(prefix + ChatColor.RED + "Usage:");
            sender.sendMessage(prefix + ChatColor.RED + " - /notes <name> <content> (to add/update a note)");
            sender.sendMessage(prefix + ChatColor.RED + " - /notes <name> (to view a note)");
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
            for (String noteName : notes.keySet()) {
                if (noteName.startsWith(input)) {
                    completions.add(noteName);
                }
            }
            return completions;
        }

        return List.of();
    }

    // Optional: Get a note for a player (by UUID) and note name
    public String getNote(UUID playerUUID, String noteName) {
        Map<String, String> notes = playerNotes.get(playerUUID);
        if (notes == null) return null;
        return notes.get(noteName.toLowerCase());
    }
}
