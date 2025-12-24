package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.godlycow.org.commands.config.RenameConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class RenameItemCommand implements CommandExecutor {
    private static final String PERMISSION_NODE = "serveressentials.command.rename";
    private final PlayerLanguageManager langManager;
    private final RenameConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RenameItemCommand(PlayerLanguageManager langManager, RenameConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.rename.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rename.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rename.usage",
                    "<red>Usage: /rename <name>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rename.no-item",
                    "<red>You're not holding an item."));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rename.no-meta",
                    "<red>Could not get item meta."));
            return true;
        }

        String input = String.join(" ", args);
        if (input.length() > config.maxNameLength) {
            input = input.substring(0, config.maxNameLength);
        }

        Component displayName = config.allowColorCodes
                ? miniMessage.deserialize(input)
                : Component.text(input);

        meta.displayName(displayName);
        item.setItemMeta(meta);

        player.sendMessage(langManager.getMessageFor(player, "commands.rename.renamed",
                "<green>Item renamed to: <white>{item}",
                ComponentPlaceholder.of("{item}", input)));

        return true;
    }
}