package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.*;

public class InventorySortCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public InventorySortCommand(PlayerMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("InventorySort.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("InventorySort.Success", "<green>Your inventory has been stacked and sorted!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("InventorySort.PlayerOnly"));
            return true;
        }

        ItemStack[] contents = player.getInventory().getContents();
        Map<Material, Integer> itemCounts = new HashMap<>();

        for (ItemStack item : contents) {
            if (item == null) continue;
            itemCounts.put(item.getType(),
                    itemCounts.getOrDefault(item.getType(), 0) + item.getAmount());
        }

        player.getInventory().clear();

        List<Material> sortedMaterials = new ArrayList<>(itemCounts.keySet());
        sortedMaterials.sort(Comparator.comparing(Material::toString));

        int slot = 0;
        for (Material mat : sortedMaterials) {
            int total = itemCounts.get(mat);
            int maxStack = mat.getMaxStackSize();

            while (total > 0) {
                int stackSize = Math.min(total, maxStack);
                player.getInventory().setItem(slot, new ItemStack(mat, stackSize));
                total -= stackSize;
                slot++;
            }
        }

        // Send success message
        Component msg = messages.get("InventorySort.Success");
        player.sendMessage(msg);

        return true;
    }
}
