package serveressentials.serveressentials.Fun;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import serveressentials.serveressentials.util.FunMessages;

public class LightningCommand implements CommandExecutor {

    private final FunMessages funMessages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LightningCommand(FunMessages funMessages) {
        this.funMessages = funMessages;

        // Add defaults if they don't exist
        funMessages.addDefault("lightning.only-players", "<red>Only players can use this command!");
        funMessages.addDefault("lightning.struck", "<green>Lightning struck!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize(funMessages.getConfig().getString("lightning.only-players")));
            return true;
        }

        Location loc = player.getLocation();
        player.getWorld().strikeLightning(loc);

        player.sendMessage(miniMessage.deserialize(funMessages.getConfig().getString("lightning.struck")));
        return true;
    }
}
