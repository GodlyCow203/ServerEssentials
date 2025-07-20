package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameItemCommand implements CommandExecutor {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l] &r");
        return formatColors(rawPrefix);
    }

    private String formatColors(String input) {
        if (input == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
        }

        matcher.appendTail(buffer);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length == 0) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /rename <name>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You're not holding an item.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Could not get item meta.");
            return true;
        }

        String newName = formatColors(String.join(" ", args));
        meta.setDisplayName(newName);
        item.setItemMeta(meta);

        player.sendMessage(getPrefix() + ChatColor.GREEN + "Item renamed to: " + newName);

        return true;
    }
}
