package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.CondenseConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class CondenseCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.condense";
    private static final Map<Material, Material> CONDENSE_MAP = new HashMap<>();

    private final PlayerLanguageManager langManager;
    private final CondenseConfig config;
    private final CommandDataStorage dataStorage;

    static {
        CONDENSE_MAP.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        CONDENSE_MAP.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        CONDENSE_MAP.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        CONDENSE_MAP.put(Material.EMERALD, Material.EMERALD_BLOCK);
        CONDENSE_MAP.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        CONDENSE_MAP.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        CONDENSE_MAP.put(Material.COAL, Material.COAL_BLOCK);
        CONDENSE_MAP.put(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK);
        CONDENSE_MAP.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
    }

    public CondenseCommand(PlayerLanguageManager langManager, CondenseConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.condense.only-player",
                    "<red>Only players can use this command!").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.condense.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        boolean condensedSomething = false;
        int totalBlocksCreated = 0;

        for (Map.Entry<Material, Material> entry : CONDENSE_MAP.entrySet()) {
            Material ingot = entry.getKey();
            Material block = entry.getValue();

            int totalIngots = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == ingot) {
                    totalIngots += item.getAmount();
                    item.setAmount(0);
                }
            }

            if (totalIngots >= 9) {
                condensedSomething = true;
                int blocks = totalIngots / 9;
                int remainder = totalIngots % 9;
                totalBlocksCreated += blocks;

                player.getInventory().addItem(new ItemStack(block, blocks));
                if (remainder > 0) {
                    player.getInventory().addItem(new ItemStack(ingot, remainder));
                }
            } else if (totalIngots > 0) {
                player.getInventory().addItem(new ItemStack(ingot, totalIngots));
            }
        }

        if (condensedSomething) {
            player.sendMessage(langManager.getMessageFor(player, "commands.condense.success",
                    "<green>Condensed items into <gold>{blocks} <green>blocks!",
                    ComponentPlaceholder.of("{blocks}", totalBlocksCreated)));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.condense.not-enough",
                    "<yellow>You don't have enough items to condense."));
        }

        // Store usage statistics (async)
        UUID playerId = player.getUniqueId();
        int finalTotalBlocksCreated = totalBlocksCreated;
        dataStorage.getState(playerId, "condense", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "condense", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "condense", "blocks_created",
                    String.valueOf(finalTotalBlocksCreated));
        });

        return true;
    }
}