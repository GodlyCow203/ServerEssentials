package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.WorldListConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class WorldListCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.worldlist";

    private final PlayerLanguageManager langManager;
    private final WorldListConfig config;

    public WorldListCommand(PlayerLanguageManager langManager, WorldListConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.worldlist.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(player, "commands.worldlist.header",
                "<gold>========== Worlds =========="));

        for (World world : Bukkit.getWorlds()) {
            String name = world.getName();
            int loadedChunks = world.getLoadedChunks().length;
            int entityCount = world.getEntities().size();
            int playerCount = world.getPlayers().size();

            String status = langManager.getString(String.valueOf(player), "commands.worldlist.status-loaded", "<green>Loaded");

            Component entry = langManager.getMessageFor(player, "commands.worldlist.entry",
                    "<yellow>{world} <gray>({status}) <white>- Chunks: {chunks}, Entities: {entities}, Players: {players}",
                    ComponentPlaceholder.of("{world}", name),
                    ComponentPlaceholder.of("{status}", status),
                    ComponentPlaceholder.of("{chunks}", String.valueOf(loadedChunks)),
                    ComponentPlaceholder.of("{entities}", String.valueOf(entityCount)),
                    ComponentPlaceholder.of("{players}", String.valueOf(playerCount)));

            sender.sendMessage(entry);
        }

        return true;
    }
}