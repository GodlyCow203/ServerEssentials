package net.godlycow.org.commands.impl;

import com.serveressentials.api.shop.ShopAPI;
import net.godlycow.org.commands.config.ShopConfig;
import net.godlycow.org.database.DatabaseManager;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.shop.ShopDataManager;
import net.godlycow.org.economy.shop.storage.ShopStorage;
import net.godlycow.org.economy.shop.config.MainShopConfig;
import net.godlycow.org.economy.shop.config.loader.ShopConfigLoader;
import net.godlycow.org.economy.shop.gui.ShopGUIManager;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
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
    private final ShopDataManager dataManager;
    private final EconomyManager economyManager;
    private final ShopAPI shopAPI;

    public ShopCommand(Plugin plugin, PlayerLanguageManager langManager,
                       DatabaseManager dbManager, ShopConfig config,
                       EconomyManager economyManager, ShopAPI shopAPI,
                       ShopGUIManager guiManager, ShopDataManager dataManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.economyManager = economyManager;
        this.shopAPI = shopAPI;
        this.guiManager = guiManager;
        this.dataManager = dataManager;
        this.storage = new ShopStorage(plugin, dbManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>This command can only be used by players!").toString());
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".disabled",
                    "<red>✗ The shop system is currently disabled."));
            return true;
        }

        if (!player.hasPermission(PERMISSION_USE)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", PERMISSION_USE)));
            return true;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.no-economy",
                    "<red>✗ Economy system is not available. Shop features disabled."));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(player, args);
        }

        guiManager.openMainGUI(player);
        return true;
    }

    private boolean handleReload(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION_RELOAD)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission-reload",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", PERMISSION_RELOAD)));
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-error",
                    "<red>✗ Cannot reload: Shop system is disabled!"));
            return true;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-error",
                    "<red>✗ Cannot reload: Economy system is not available!"));
            return true;
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("tofile")) {
            return handleSaveToFile(player);
        }

        reloadShop(player).thenAccept(success -> {
            if (success) {
                player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-success",
                        "<green>✓ Shop reloaded from YML files and saved to database."));
            } else {
                player.sendMessage(langManager.getMessageFor(player, "economy.shop.reload-error",
                        "<red>✗ Error reloading shop configuration."));
            }
        });
        return true;
    }

    private boolean handleSaveToFile(Player player) {
        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "economy.shop.save-to-file-error",
                    "<red>✗ Cannot save: Shop system is disabled!"));
            return true;
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File mainFile = new File(config.getShopFolder(), "main.yml");
                MainShopConfig mainConfig = guiManager.getMainConfig();
                if (mainConfig != null) {
                    ShopConfigLoader.saveMainConfig(mainFile, mainConfig);
                }

                guiManager.getSectionCache().forEach((sectionName, sectionConfig) -> {
                    File sectionFile = new File(config.getShopFolder(), sectionName + ".yml");
                    ShopConfigLoader.saveSectionConfig(sectionFile, sectionConfig);
                });

                player.sendMessage(langManager.getMessageFor(player, "economy.shop.save-to-file-success",
                        "<green>✓ Saved current database config to YML files!"));
                future.complete(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save to file: " + e.getMessage());
                e.printStackTrace();
                player.sendMessage(langManager.getMessageFor(player, "economy.shop.save-to-file-error",
                        "<red>✗ Failed to save to files!"));
                future.complete(false);
            }
        });

        return true;
    }

    private CompletableFuture<Boolean> reloadShop(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                    guiManager.reloadConfigs(true);
                    guiManager.refreshOpenInventories();
                    return null;
                }).get();

                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to reload shop: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS,
                runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)));
    }

    public ShopDataManager getDataManager() {
        return dataManager;
    }

    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    public ShopGUIManager getGuiManager() {
        return guiManager;
    }
}