package net.lunark.io.Fun;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

public class ExplosionCommand implements CommandExecutor, TabCompleter {

    private final FunMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ExplosionCommand(FunMessages messages) {
        this.messages = messages;

        messages.addDefault("Explosion.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("Explosion.NoRadius", "<yellow>Please specify a radius!");
        messages.addDefault("Explosion.InvalidRadius", "<red>Invalid radius: {input}");
        messages.addDefault("Explosion.Created", "<green>Explosion created with radius {radius}!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Explosion.PlayerOnly"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(messages.get("Explosion.NoRadius"));
            return true;
        }

        try {
            float radius = Float.parseFloat(args[0]);

            player.getWorld().createExplosion(player.getLocation(), radius);

            player.sendMessage(miniMessage.deserialize(
                    messages.getConfig().getString("Explosion.Created")
                            .replace("{radius}", args[0])
            ));

        } catch (NumberFormatException e) {
            player.sendMessage(miniMessage.deserialize(
                    messages.getConfig().getString("Explosion.InvalidRadius")
                            .replace("{input}", args[0])
            ));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (int i = 1; i <= 10; i++) suggestions.add(String.valueOf(i));
            return suggestions;
        }
        return null;
    }
}
