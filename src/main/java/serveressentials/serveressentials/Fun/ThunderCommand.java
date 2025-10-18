package serveressentials.serveressentials.Fun;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

public class ThunderCommand implements CommandExecutor, TabCompleter {

    private final FunMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ThunderCommand(FunMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("Thunder.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("Thunder.Struck", "<yellow>Thunder effect summoned at {target}!");
        messages.addDefault("Thunder.SelfName", "yourself");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Thunder.PlayerOnly"));
            return true;
        }

        Player target = player;

        if (args.length > 0) {
            Player found = Bukkit.getPlayerExact(args[0]);
            if (found != null) target = found;
        }

        Location loc = target.getLocation();
        target.getWorld().strikeLightningEffect(loc);

        String targetName = (target.equals(player)) ?
                messages.getConfig().getString("Thunder.SelfName", "yourself") :
                target.getName();

        String msgRaw = messages.getConfig().getString("Thunder.Struck", "<yellow>Thunder effect summoned at {target}!");
        msgRaw = msgRaw.replace("{target}", targetName);

        player.sendMessage(miniMessage.deserialize(msgRaw));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
            return suggestions;
        }
        return null;
    }
}
