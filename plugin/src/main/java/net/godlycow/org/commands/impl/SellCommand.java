package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.SellConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.sellgui.gui.SellGUIManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public final class SellCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.sell";
    private static final String COMMAND_NAME = "sell";

    private final PlayerLanguageManager langManager;
    private final SellConfig config;
    private final SellGUIManager guiManager;
    private final EconomyManager economyManager;
    private final JavaPlugin plugin;

    public SellCommand(JavaPlugin plugin, PlayerLanguageManager langManager, SellConfig config,
                       SellGUIManager guiManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.guiManager = guiManager;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            String message = langManager.getMessageFor(null,
                            "commands." + COMMAND_NAME + ".only-player",
                            "<red>This command can only be used by players!")
                    .toString();
            sender.sendMessage(message);
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.disabled",
                    "<red>The sell GUI is currently disabled."));
            return true;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.no-economy",
                    "<red>✗ Economy system is not available. Please contact an administrator."));
            plugin.getLogger().warning("Sell command attempted but economy system is disabled!");
            return true;
        }

        guiManager.openSellGUI(player);
        return true;
    }
}