package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.SetLoreLineConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Collections;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SetLoreLineCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.setloreline";

    private final PlayerLanguageManager langManager;
    private final SetLoreLineConfig config;
    private final CommandDataStorage dataStorage;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SetLoreLineCommand(PlayerLanguageManager langManager, SetLoreLineConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.setloreline.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.setloreline.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.setloreline.usage",
                    "<red>Usage: <white>/setloreline <text>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.setloreline.no-item",
                    "<red>You're not holding an item."));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        String input = String.join(" ", args);
        Component loreComponent = miniMessage.deserialize(input);
        meta.lore(Collections.singletonList(loreComponent));
        item.setItemMeta(meta);

        player.sendMessage(langManager.getMessageFor(player, "commands.setloreline.lore-set",
                "<green>Lore set to: <white>{lore}",
                ComponentPlaceholder.of("{lore}", input)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "setloreline", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "setloreline", "usage_count", String.valueOf(count + 1));
        });

        return true;
    }
}