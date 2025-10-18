package serveressentials.serveressentials.Fun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.entity.Fireball;
import org.bukkit.command.*;
import serveressentials.serveressentials.util.FunMessages;

public class FireballCommand implements CommandExecutor {

    private final FunMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public FireballCommand(FunMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("Fireball.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("Fireball.Launched", "<green>Fireball launched!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Fireball.PlayerOnly"));
            return true;
        }

        player.launchProjectile(Fireball.class);

        Component msg = messages.get("Fireball.Launched");
        player.sendMessage(msg);

        return true;
    }
}
