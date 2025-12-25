package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.KitConfig;
import net.godlycow.org.kit.KitConfigManager;
import net.godlycow.org.kit.trigger.KitGUIListener;
import net.godlycow.org.kit.KitManager;
import net.godlycow.org.kit.storage.KitStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class KitCommand implements CommandExecutor {

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final KitStorage kitStorage;
    private final KitConfig kitConfig;
    private final KitGUIListener guiListener;

    public KitCommand(Plugin plugin, PlayerLanguageManager langManager, KitStorage kitStorage, KitConfig kitConfig, KitGUIListener guiListener) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.kitStorage = kitStorage;
        this.kitConfig = kitConfig;
        this.guiListener = guiListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.command.kits.reload")) {
                sender.sendMessage(langManager.getMessageFor(
                        sender instanceof Player p ? p : null,
                        "kits.no-permission",
                        "<red>You don't have permission to reload kits!",
                        LanguageManager.ComponentPlaceholder.of("{permission}", "kits.command.reload")
                ).toString());
                return true;
            }

            reloadKits();
            sender.sendMessage(langManager.getMessageFor(
                    sender instanceof Player p ? p : null,
                    "kits.reload-success",
                    "<green>✓ Kits reloaded successfully!"
            ).toString());
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-player",
                    "<red>Only players can use this command!").toString());
            return true;
        }

        if (!player.hasPermission("serveressentials.command.kits")) {
            player.sendMessage(langManager.getMessageFor(player, "kits.no-permission",
                    "<red>You don't have permission to use kits!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "kits.command.use")));
            return true;
        }

        guiListener.openKitGUI(player);
        return true;
    }

    private void reloadKits() {
        KitConfigManager.reload();
        kitConfig.load();
        plugin.getLogger().info("Kits reloaded: " + KitManager.getKits().size() + " kits loaded.");
    }
}