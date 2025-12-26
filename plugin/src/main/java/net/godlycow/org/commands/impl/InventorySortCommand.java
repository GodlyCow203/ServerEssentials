package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.InventorySortConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class InventorySortCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.inventorysort";

    private final PlayerLanguageManager langManager;
    private final InventorySortConfig config;

    public InventorySortCommand(PlayerLanguageManager langManager, InventorySortConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.inventorysort.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.inventorysort.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        sortInventory(player);

        player.sendMessage(langManager.getMessageFor(player, "commands.inventorysort.success",
                "<green>Your inventory has been stacked and sorted!"));

        return true;
    }

    private void sortInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        Map<Material, Integer> itemCounts = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) continue;
            itemCounts.put(item.getType(), itemCounts.getOrDefault(item.getType(), 0) + item.getAmount());
        }

        for (int i = 0; i < 36; i++) {
            player.getInventory().setItem(i, null);
        }

        List<Material> sortedMaterials = itemCounts.keySet().stream()
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());

        int slot = 0;
        for (Material mat : sortedMaterials) {
            int total = itemCounts.get(mat);
            int maxStack = mat.getMaxStackSize();

            while (total > 0 && slot < 36) {
                int stackSize = Math.min(total, maxStack);
                player.getInventory().setItem(slot++, new ItemStack(mat, stackSize));
                total -= stackSize;
            }
        }
    }
}