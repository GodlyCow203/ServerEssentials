package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RTPCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    private String hex(String hexColor, String text) {
        StringBuilder colored = new StringBuilder("§x");
        for (char c : hexColor.replace("#", "").toCharArray()) {
            colored.append("§").append(c);
        }
        return colored + text;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rtp.reload")) {
                sender.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            RTPConfig.load();
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "RTP configuration reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', "&x&3&b&f&f&a&6RTP &x&3&b&f&f&a&6Menu"));

        // Overworld
        ItemStack overworld = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta1 = overworld.getItemMeta();
        meta1.setDisplayName(hex("#3bffa6", "Overworld"));
        meta1.setLore(Arrays.asList(
                hex("#c0ffc2", "Randomly teleport"),
                hex("#a0ffb4", "within the Overworld.")
        ));
        overworld.setItemMeta(meta1);
        gui.setItem(2, overworld);

        // Nether
        ItemStack nether = new ItemStack(Material.NETHERRACK);
        ItemMeta meta2 = nether.getItemMeta();
        meta2.setDisplayName(hex("#ff6961", " Nether"));
        meta2.setLore(Arrays.asList(
                hex("#ffb3a7", "Teleport into the"),
                hex("#ff9a8b", "dangerous Nether dimension.")
        ));
        nether.setItemMeta(meta2);
        gui.setItem(4, nether);

        // End
        ItemStack theEnd = new ItemStack(Material.END_STONE);
        ItemMeta meta3 = theEnd.getItemMeta();
        meta3.setDisplayName(hex("#b86bff", "The End"));
        meta3.setLore(Arrays.asList(
                hex("#d4b3ff", "Teleport to The End,"),
                hex("#c59aff", "home of the dragon.")
        ));
        theEnd.setItemMeta(meta3);
        gui.setItem(6, theEnd);

        player.openInventory(gui);
        return true;
    }
}
