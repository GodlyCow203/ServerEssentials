package serveressentials.serveressentials.Fun;

import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.FunMessages;

public class GlowCommand implements CommandExecutor {

    private final FunMessages messages;

    public GlowCommand(FunMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("glow.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (!player.hasPermission("serveressentials.glow")) {
            player.sendMessage(messages.get("glow.no-permission"));
            return true;
        }

        if (player.isGlowing()) {
            player.setGlowing(false);
            player.sendMessage(messages.get("glow.disabled"));
        } else {
            player.setGlowing(true);
            player.sendMessage(messages.get("glow.enabled"));
        }

        return true;
    }
}
