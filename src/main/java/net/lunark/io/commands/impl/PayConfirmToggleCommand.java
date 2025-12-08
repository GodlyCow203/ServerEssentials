package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.PayConfirmToggleConfig;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class PayConfirmToggleCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.payconfirmtoggle";
    private static final String COMMAND_NAME = "payconfirmtoggle";

    private final PlayerLanguageManager langManager;
    private final PayConfirmToggleConfig config;
    private final ServerEssentialsEconomy economy;

    public PayConfirmToggleCommand(PlayerLanguageManager langManager, PayConfirmToggleConfig config, ServerEssentialsEconomy economy) {
        this.langManager = langManager;
        this.config = config;
        this.economy = economy;
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

        economy.hasPayConfirmDisabled(playerId).thenCompose(confirmDisabled -> {
            boolean newState = !confirmDisabled;
            return economy.setPayConfirmDisabled(playerId, playerName, newState)
                    .thenRun(() -> {
                        String messageKey = newState ? "commands.payconfirmtoggle.disabled" : "commands.payconfirmtoggle.enabled";
                        player.sendMessage(langManager.getMessageFor(player,
                                messageKey,
                                "<green>Payment confirmations have been <yellow>{state}</yellow>.",
                                ComponentPlaceholder.of("{state}", newState ? "disabled" : "enabled")));
                    });
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".error",
                    "<red>Error toggling confirmation state. Please try again."));
            return null;
        });

        return true;
    }
}