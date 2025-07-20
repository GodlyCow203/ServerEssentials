package serveressentials.serveressentials;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TpCommandHere implements CommandExecutor {

    private static final Map<UUID, UUID> pendingTpHereRequests = new HashMap<>();
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
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
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(prefix + ChatColor.RED + "Usage: /tphere <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(prefix + ChatColor.RED + "Player not found or not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(prefix + ChatColor.RED + "You can't teleport yourself to yourself.");
            return true;
        }

        pendingTpHereRequests.put(target.getUniqueId(), player.getUniqueId());

        player.sendMessage(prefix + ChatColor.GREEN + "TPAHere request sent to " + ChatColor.YELLOW + target.getName());

        // Accept button
        TextComponent accept = new TextComponent("[Accept]");
        accept.setColor(ChatColor.GREEN);
        accept.setBold(true);
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to teleport to " + player.getName()).color(ChatColor.GRAY).create()));

        // Deny button
        TextComponent deny = new TextComponent("[Deny]");
        deny.setColor(ChatColor.RED);
        deny.setBold(true);
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + player.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to deny teleport request.").color(ChatColor.GRAY).create()));

        // Final message
        TextComponent message = new TextComponent(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " wants to teleport you to them: ");
        message.addExtra(accept);
        message.addExtra(new TextComponent(" "));
        message.addExtra(deny);

        target.spigot().sendMessage(message);
        return true;
    }

    public static UUID getRequester(UUID targetUUID) {
        return pendingTpHereRequests.get(targetUUID);
    }

    public static void removeRequest(UUID targetUUID) {
        pendingTpHereRequests.remove(targetUUID);
    }
}
