package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class HomeCommand implements CommandExecutor {

    private static final int[] HOME_SLOTS = {10, 13, 16};

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        // You can parse page number from args if you want pagination commands, else default to 0
        int page = 0;
        if (args.length == 1) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch (NumberFormatException e) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Invalid page number.");
                return true;
            }
        }

        openHomesGUI(player, page);
        return true;
    }

    public static void openHomesGUI(Player player, int page) {
        Map<String, Location> allHomes = HomeManager.getHomes(player.getUniqueId()).entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        List<Map.Entry<String, Location>> homesList = new ArrayList<>(allHomes.entrySet());

        int homesPerPage = HOME_SLOTS.length;
        int totalPages = (int) Math.ceil(homesList.size() / (double) homesPerPage);
        if (totalPages == 0) totalPages = 1; // Ensure at least one page

        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Your Homes (Page " + (page + 1) + ")");

        int startIndex = page * homesPerPage;

        for (int i = 0; i < homesPerPage; i++) {
            int index = startIndex + i;
            if (index >= homesList.size()) break;

            Map.Entry<String, Location> entry = homesList.get(index);
            String name = entry.getKey();
            Location loc = entry.getValue();

            ItemStack bed = new ItemStack(loc != null ? Material.BLUE_BED : Material.GRAY_BED);
            ItemMeta meta = bed.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + name);
            meta.setLore(Collections.singletonList(loc != null
                    ? ChatColor.GREEN + "Click to teleport"
                    : ChatColor.RED + "Not set"));
            bed.setItemMeta(meta);
            gui.setItem(HOME_SLOTS[i], bed);
        }

        // Previous page arrow
        if (page > 0) {
            ItemStack backArrow = new ItemStack(Material.ARROW);
            ItemMeta meta = backArrow.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            backArrow.setItemMeta(meta);
            gui.setItem(18, backArrow);
        }

        // Next page arrow
        if (page < totalPages - 1) {
            ItemStack nextArrow = new ItemStack(Material.ARROW);
            ItemMeta meta = nextArrow.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextArrow.setItemMeta(meta);
            gui.setItem(26, nextArrow);
        }

        player.openInventory(gui);
    }
}
