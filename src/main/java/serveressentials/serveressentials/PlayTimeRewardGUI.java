package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayTimeRewardGUI {

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Playtime Rewards");

        long playtime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 20; // in seconds

        // Example reward at 1 hour (3600s)
        addReward(gui, 11, playtime, 3600, player);

        player.openInventory(gui);
    }

    private static void addReward(Inventory gui, int slot, long playtime, long requiredTime, Player player) {
        Material type;
        String name;
        String lore;

        if (hasClaimedReward(player, requiredTime)) {
            type = Material.GREEN_STAINED_GLASS_PANE;
            name = "§aClaimed";
            lore = "§7You've already claimed this reward.";
        } else if (playtime >= requiredTime) {
            type = Material.YELLOW_STAINED_GLASS_PANE;
            name = "§eClick to claim!";
            lore = "§7Reward available!";
        } else {
            type = Material.GRAY_STAINED_GLASS_PANE;
            name = "§7Locked";
            lore = "§cPlaytime required: " + (requiredTime / 60) + " minutes";
        }

        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Collections.singletonList(lore));
        item.setItemMeta(meta);

        gui.setItem(slot, item);
    }

    private static boolean hasClaimedReward(Player player, long requiredTime) {
        return player.getPersistentDataContainer().has(new org.bukkit.NamespacedKey(ServerEssentials.getInstance(), "reward_" + requiredTime),
                org.bukkit.persistence.PersistentDataType.BYTE);
    }

    public static void claimReward(Player player, long requiredTime) {
        player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(ServerEssentials.getInstance(), "reward_" + requiredTime),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1);

        player.sendMessage("§aYou claimed your reward!");
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
        player.getInventory().addItem(new ItemStack(Material.NETHERITE_SCRAP, 1));
        // Give money if economy system is implemented
        // Example: EconomyAPI.deposit(player, 1000);
    }
}
