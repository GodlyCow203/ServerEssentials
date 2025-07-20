package serveressentials.serveressentials;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NickCommand implements CommandExecutor {

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
            player.sendMessage(getPrefix() + net.md_5.bungee.api.ChatColor.RED + "Usage: /nick <name>");
            return true;
        }

        String nickname = formatColors(String.join(" ", args));
        player.setDisplayName(nickname);
        player.setPlayerListName(nickname);
        player.sendMessage(getPrefix() + net.md_5.bungee.api.ChatColor.GREEN + "Your nickname has been set to: " + nickname);

        return true;
    }
}
