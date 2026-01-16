package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.HatConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class HatCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.hat";

    private final PlayerLanguageManager langManager;
    private final HatConfig config;
    private final CommandDataStorage dataStorage;

    public HatCommand(PlayerLanguageManager langManager, HatConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.hat.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.hat.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.hat.no-item",
                    "<red>You must hold an item in your main hand to wear it as a hat!"));
            return true;
        }

        if (!config.isAllowedItem(handItem.getType())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.hat.item-not-allowed",
                    "<red>This item cannot be worn as a hat! See allowed items in config."));
            return true;
        }

        ItemStack currentHelmet = player.getInventory().getHelmet();
        player.getInventory().setHelmet(handItem.clone());
        player.getInventory().setItemInMainHand(currentHelmet);

        player.sendMessage(langManager.getMessageFor(player, "commands.hat.success",
                "<green>✓ Your hat has been updated!"));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "hat", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "hat", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "hat", "last_item", handItem.getType().toString());
        });

        return true;
    }
}