package net.lunark.io.economy;

import net.lunark.io.database.DatabaseManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class ShopCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ShopStorage storage;
    private final ShopConfig config;
    private final ShopGUIManager guiManager;
    private final Economy economy;

    public ShopCommand(Plugin plugin, PlayerLanguageManager langManager,
                       DatabaseManager dbManager, ShopConfig config, Economy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = new ShopStorage(dbManager);
        this.config = config;
        this.economy = economy;
        this.guiManager = new ShopGUIManager(plugin, langManager, storage, config, economy);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-player",
                    "<red>This command can only be used by players!"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
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

        // Run inventory refresh on main thread (required for Bukkit inventory operations)
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                // Refresh all open shop inventories - configs are reloaded on-demand
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
}