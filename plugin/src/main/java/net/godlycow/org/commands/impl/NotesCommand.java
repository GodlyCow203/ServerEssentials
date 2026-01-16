package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.NotesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.notes.storage.NotesStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class NotesCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "essc.command.notes";

    private final PlayerLanguageManager langManager;
    private final NotesConfig config;
    private final NotesStorage storage;

    public NotesCommand(PlayerLanguageManager langManager, NotesConfig config, NotesStorage storage) {
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.notes.only-player",
                    "<red>Only players can use notes.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.notes.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            showUsage(player);
            return true;
        }

        String noteName = args[0].toLowerCase();

        if (args.length == 1) {
            viewNote(player, noteName);
        } else {
            String content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            saveNote(player, noteName, content);
        }

        return true;
    }

    private void showUsage(Player player) {
        player.sendMessage(langManager.getMessageFor(player, "commands.notes.usage",
                "<yellow>Usage:</yellow> /notes <name> [content]"));

        storage.getNotes(player.getUniqueId()).thenAccept(notes -> {
            if (notes.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.notes.list-empty",
                        "<yellow>You have no notes."));
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.notes.list-header",
                        "<yellow>Your notes:</yellow> " + String.join(", ", notes.keySet())));
            }
        });
    }

    private void viewNote(Player player, String noteName) {
        storage.getNote(player.getUniqueId(), noteName).thenAccept(optContent -> {
            if (optContent.isPresent()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.notes.view",
                        "<yellow>Note '<green>{name}</green>':</yellow> {content}",
                        ComponentPlaceholder.of("{name}", noteName),
                        ComponentPlaceholder.of("{content}", optContent.get())));
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.notes.not-found",
                        "<red>Note '<yellow>{name}</yellow>' not found.",
                        ComponentPlaceholder.of("{name}", noteName)));
            }
        });
    }

    private void saveNote(Player player, String noteName, String content) {
        storage.saveNote(player.getUniqueId(), noteName, content).thenAccept(v -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.notes.saved",
                    "<green>Saved note '<yellow>{name}</yellow>':</yellow> {content}",
                    ComponentPlaceholder.of("{name}", noteName),
                    ComponentPlaceholder.of("{content}", content)));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (!player.hasPermission(PERMISSION)) return List.of();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            CompletableFuture<Map<String, String>> notesFuture = storage.getNotes(player.getUniqueId());

            try {
                Map<String, String> notes = notesFuture.get();
                return notes.keySet().stream()
                        .filter(name -> name.startsWith(input))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                return List.of();
            }
        }

        return List.of();
    }
}