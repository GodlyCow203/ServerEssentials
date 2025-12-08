package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.ShopConfig;
import net.lunark.io.database.DatabaseManager;
import net.lunark.io.economy.ServerEssentialsEconomy;
import net.lunark.io.economy.ShopGUIManager;
import net.lunark.io.economy.ShopStorage;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public final class ShopCommand implements CommandExecutor {
    private static final String PERMISSION_USE = "serveressentials.command.shop";
    private static final String PERMISSION_RELOAD = "serveressentials.command.shop.reload";
    private static final String COMMAND_NAME = "shop";

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ShopStorage storage;
    private final ShopConfig config;
    private final ShopGUIManager guiManager;

    public ShopCommand(Plugin plugin, PlayerLanguageManager langManager,
                       DatabaseManager dbManager, ShopConfig config, ServerEssentialsEconomy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = new ShopStorage(plugin, dbManager);
        this.config = config;
        this.guiManager = new ShopGUIManager(plugin, langManager, storage, config, economy);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>This command can only be used by players!").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_USE)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{permission}", PERMISSION_USE)));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission(PERMISSION_RELOAD)) {
                player.sendMessage(langManager.getMessageFor(player,
                        "commands." + COMMAND_NAME + ".no-permission-reload",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        net.lunark.io.language.LanguageManager.ComponentPlaceholder.of("{permission}", PERMISSION_RELOAD)));
                return true;
            }

            reloadShop(player).thenAccept(success -> {
                if (success) {
                    player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-success",
                            "<green>Shop configuration reloaded."));
                } else {
                    player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-error",
                            "<red>Error reloading shop configuration."));
                }
            });
        } else {
            guiManager.openMainGUI(player);
        }

        return true;
    }

    private CompletableFuture<Boolean> reloadShop(Player player) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                guiManager.refreshOpenInventories();
                future.complete(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to reload shop: " + e.getMessage());
                e.printStackTrace();
                future.complete(false);
            }
        });
        return future;
    }

    public ShopGUIManager getGuiManager() {
        return guiManager;
    }
}