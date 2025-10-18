package serveressentials.serveressentials.Player;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.HashMap;
import java.util.Map;

public class CondenseCommand implements CommandExecutor {

    private final Map<Material, Material> condenseMap = new HashMap<>();
    private final PlayerMessages messages;

    public CondenseCommand(ServerEssentials plugin) {
        // Hook into your messages system
        this.messages = plugin.getPlayerMessages();

        // Add default messages if missing
        messages.addDefault("condense.only-players", "<red>Only players can use this command!");
        messages.addDefault("condense.success", "<green>Your items have been condensed into blocks!");
        messages.addDefault("condense.not-enough", "<yellow>You don't have enough items to condense.");

        // Define which items can be condensed into blocks
        condenseMap.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        condenseMap.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        condenseMap.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        condenseMap.put(Material.EMERALD, Material.EMERALD_BLOCK);
        condenseMap.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        condenseMap.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        condenseMap.put(Material.COAL, Material.COAL_BLOCK);
        condenseMap.put(Material.NETHERITE_INGOT, Material.NETHERITE_BLOCK);
        condenseMap.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Send message to non-player
            sender.sendMessage(messages.get("condense.only-players"));
            return true;
        }

        Player player = (Player) sender;
        boolean condensedSomething = false;

        for (Map.Entry<Material, Material> entry : condenseMap.entrySet()) {
            Material ingot = entry.getKey();
            Material block = entry.getValue();

            int totalIngots = 0;

            // Count total ingots in player's inventory
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == ingot) {
                    totalIngots += item.getAmount();
                    item.setAmount(0); // Clear them first
                }
            }

            // If we have enough for at least one block
            if (totalIngots >= 9) {
                condensedSomething = true;
                int blocks = totalIngots / 9;
                int remainder = totalIngots % 9;

                // Give condensed blocks
                player.getInventory().addItem(new ItemStack(block, blocks));

                // Give leftover ingots back
                if (remainder > 0) {
                    player.getInventory().addItem(new ItemStack(ingot, remainder));
                }
            } else if (totalIngots > 0) {
                // If we didn't have enough for even one block, give the ingots back
                player.getInventory().addItem(new ItemStack(ingot, totalIngots));
            }
        }

        // Send messages
        if (condensedSomething) {
            player.sendMessage(messages.get("condense.success"));
        } else {
            player.sendMessage(messages.get("condense.not-enough"));
        }

        return true;
    }
}
