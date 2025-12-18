package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.PayToggleConfig;
import net.lunark.io.economy.EconomyManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class PayToggleCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.paytoggle";
    private static final String COMMAND_NAME = "paytoggle";

    private final PlayerLanguageManager langManager;
    private final PayToggleConfig config;
    private final EconomyManager economyManager;

    public PayToggleCommand(PlayerLanguageManager langManager, PayToggleConfig config, EconomyManager economyManager) {
        this.langManager = langManager;
        this.config = config;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        economyManager.hasPaymentsDisabled(playerId.toString()).thenCompose(disabled -> {
            boolean newState = !disabled;
            return economyManager.setPaymentsDisabled(playerId.toString(), playerName, newState)
                    .thenRun(() -> {
                        String messageKey = newState ? "commands.paytoggle.disabled" : "commands.paytoggle.enabled";
                        player.sendMessage(langManager.getMessageFor(player,
                                messageKey,
                                "<green>Payments have been <yellow>{state}</yellow>.",
                                ComponentPlaceholder.of("{state}", newState ? "disabled" : "enabled")));
                    });
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".error",
                    "<red>Error toggling payment state. Please try again."));
            return null;
        });

        return true;
    }
}