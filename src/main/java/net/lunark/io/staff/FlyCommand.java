package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /fly command with persistent state stored in command_data.db
 * Uses PlayerLanguageManager for messages and CommandDataStorage for persistence
 */
public class FlyCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final CommandDataStorage storage;
    private final String commandName = "fly";

    public FlyCommand(JavaPlugin plugin, PlayerLanguageManager langManager, CommandDataStorage storage) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = langManager.getMessageFor(null, "commands.only-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(msg.toString());
            return true;
        }

        // Check permission
        if (!player.hasPermission("serveressentials.fly")) {
            Component msg = langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.fly"));
            player.sendMessage(msg);
            return true;
        }

        // Toggle flight state (async)
        toggleFlight(player);
        return true;
    }

    private void toggleFlight(Player player) {
        UUID playerId = player.getUniqueId();

        // Get current state from database
        storage.getState(playerId, commandName, "enabled").thenAccept(optState -> {
            boolean currentlyEnabled = optState.map(Boolean::parseBoolean).orElse(false);
            boolean newState = !currentlyEnabled;

            // Save new state
            storage.setState(playerId, commandName, "enabled", String.valueOf(newState)).thenRun(() -> {
                // Apply ingame
                applyFlightState(player, newState);

                // Send message
                String messageKey = newState ? "fly.enabled" : "fly.disabled";
                String defaultMessage = newState ?
                        "<green>✈ Flight enabled! Press SPACE twice to fly." :
                        "<red>✈ Flight disabled.";

                Component msg = langManager.getMessageFor(player, messageKey, defaultMessage);
                player.sendMessage(msg);

            }).exceptionally(ex -> {
                player.sendMessage(langManager.getMessageFor(player, "fly.error",
                        "<red>Error saving flight state. Please try again."));
                plugin.getLogger().warning("Failed to save fly state for " + player.getName() + ": " + ex.getMessage());
                return null;
            });
        });
    }

    /**
     * Apply flight state to a player (called from command and on join)
     */
    public void applyFlightState(Player player, boolean enabled) {
        player.setAllowFlight(enabled);
        if (enabled && !player.isOnGround()) {
            player.setFlying(true);
        } else if (!enabled) {
            player.setFlying(false);
        }
    }

    /**
     * Load and apply flight state when player joins
     */
    public void loadFlightState(Player player) {
        storage.getState(player.getUniqueId(), commandName, "enabled").thenAccept(optState -> {
            boolean enabled = optState.map(Boolean::parseBoolean).orElse(false);
            applyFlightState(player, enabled);
            plugin.getLogger().fine("Loaded flight state for " + player.getName() + ": " + enabled);
        });
    }
}