package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SetLoreLineCommand implements CommandExecutor {

    private final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return formatColors(rawPrefix);
    }

    // Formats both & codes and <#hex> codes
    private String formatColors(String message) {
        // Convert hex codes first
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        // Then translate legacy & codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /setloreline <text>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(prefix + ChatColor.RED + "You're not holding an item.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        String input = String.join(" ", args);
        String formattedLore = formatColors(input);

        meta.setLore(Collections.singletonList(formattedLore));
        item.setItemMeta(meta);

        player.sendMessage(prefix + ChatColor.GREEN + "Lore set to: " + formattedLore);
        return true;
    }
}

