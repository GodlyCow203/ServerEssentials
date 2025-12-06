package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.ItemInfoConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.stream.Collectors;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class ItemInfoCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.iteminfo";

    private final PlayerLanguageManager langManager;
    private final ItemInfoConfig config;
    private final CommandDataStorage dataStorage;

    public ItemInfoCommand(PlayerLanguageManager langManager, ItemInfoConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.iteminfo.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.no-item",
                    "<red>You must hold an item in your main hand."));
            return true;
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.header",
                "<dark_aqua>≡ <bold>Item Information</bold> <dark_aqua>≡"));

        player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.type",
                "<gray>Type: <white>{type}",
                ComponentPlaceholder.of("{type}", item.getType().toString())));

        player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.amount",
                "<gray>Amount: <white>{amount}",
                ComponentPlaceholder.of("{amount}", String.valueOf(item.getAmount()))));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.name",
                        "<gray>Name: <white>{name}",
                        ComponentPlaceholder.of("{name}", meta.getDisplayName())));
            }

            if (meta.hasLore() && meta.lore() != null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.lore-header",
                        "<gray>≡ <bold>Lore</bold> <gray>≡"));

                String loreText = meta.lore().stream()
                        .map(line -> net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line))
                        .collect(Collectors.joining("\n"));

                player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.lore-content",
                        "<white>{lore}",
                        ComponentPlaceholder.of("{lore}", loreText)));
            }

            if (!meta.getEnchants().isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.enchants-header",
                        "<gray>≡ <bold>Enchantments</bold> <gray>≡"));

                for (Enchantment enchant : meta.getEnchants().keySet()) {
                    int level = meta.getEnchantLevel(enchant);
                    player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.enchant-line",
                            "<yellow>✦ <white>{enchant} <gray>Level {level}",
                            ComponentPlaceholder.of("{enchant}", enchant.getKey().getKey()),
                            ComponentPlaceholder.of("{level}", String.valueOf(level))));
                }
            }

            if (!meta.getItemFlags().isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.flags-header",
                        "<gray>≡ <bold>Item Flags</bold> <gray>≡"));

                for (ItemFlag flag : meta.getItemFlags()) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.flag-line",
                            "<yellow>- <gray>{flag}",
                            ComponentPlaceholder.of("{flag}", flag.name())));
                }
            }
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.iteminfo.no-meta",
                    "<gray>No additional metadata."));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "iteminfo", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "iteminfo", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "iteminfo", "last_item", item.getType().toString());
        });

        return true;
    }
}