package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.RepairConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class RepairCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.repair";

    private final PlayerLanguageManager langManager;
    private final RepairConfig config;
    private final CommandDataStorage dataStorage;

    public RepairCommand(PlayerLanguageManager langManager, RepairConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.repair.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.repair.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.repair.no-item",
                    "<red>You must hold an item in your main hand to repair it."));
            return true;
        }

        if (!item.hasItemMeta() || !(item.getItemMeta() instanceof Damageable damageable)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.repair.cannot-repair",
                    "<red>This item cannot be repaired."));
            return true;
        }

        if (damageable.getDamage() == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.repair.already-full",
                    "<yellow>This item is already at full durability."));
            return true;
        }

        damageable.setDamage(0);
        item.setItemMeta(damageable);

        player.sendMessage(langManager.getMessageFor(player, "commands.repair.success",
                "<green>✓ Item has been fully repaired!"));

        trackUsage(player.getUniqueId(), item.getType().toString(), true);

        if (config.playSound()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        }

        return true;
    }

    private void trackUsage(UUID playerId, String itemType, boolean success) {
        dataStorage.getState(playerId, "repair", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "repair", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "repair", "last_item", itemType);
            dataStorage.setState(playerId, "repair", "last_success", String.valueOf(success));
        });
    }
}