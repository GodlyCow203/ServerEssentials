package serveressentials.serveressentials;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;

public class TpaCommand implements CommandExecutor, TabCompleter {

    private static final Map<UUID, UUID> tpaRequests = new HashMap<>();

    private static final java.util.regex.Pattern HEX_PATTERN = java.util.regex.Pattern.compile("<#([A-Fa-f0-9]{6})>");

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
            player.sendMessage(prefix + ChatColor.RED + "Usage: /tpa <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(prefix + ChatColor.RED + "Player not found or not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(prefix + ChatColor.RED + "You can't send a TPA request to yourself.");
            return true;
        }

        tpaRequests.put(target.getUniqueId(), player.getUniqueId());

        // Sender message
        player.sendMessage(prefix + ChatColor.GREEN + "TPA request sent to " + ChatColor.YELLOW + target.getName());

        // Target message
        target.sendMessage(prefix + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has requested to teleport to you.");

        // Create clickable accept button
        TextComponent accept = new TextComponent("[Accept]");
        accept.setColor(ChatColor.GREEN);
        accept.setBold(true);
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + player.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to accept the teleport request.").color(ChatColor.GRAY).create()));

        // Create clickable deny button
        TextComponent deny = new TextComponent("[Deny]");
        deny.setColor(ChatColor.RED);
        deny.setBold(true);
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + player.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to deny the teleport request.").color(ChatColor.GRAY).create()));

        // Spacer between buttons
        TextComponent spacer = new TextComponent(" ");
        spacer.setColor(ChatColor.DARK_GRAY);

        // Full message
        TextComponent message = new TextComponent(prefix);
        message.addExtra(accept);
        message.addExtra(spacer);
        message.addExtra(deny);

        target.spigot().sendMessage(message);

        return true;
    }

    public static UUID getRequester(UUID targetUUID) {
        return tpaRequests.get(targetUUID);
    }

    public static void removeRequest(UUID targetUUID) {
        tpaRequests.remove(targetUUID);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && online.getName().toLowerCase().startsWith(input)) {
                    suggestions.add(online.getName());
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
