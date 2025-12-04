package net.lunark.io.Fun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.lunark.io.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

public class BeezookaCommand implements CommandExecutor, TabCompleter {

    private final FunMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double defaultVelocity;

    public BeezookaCommand(FunMessages messages) {
        this.messages = messages;

        messages.addDefault("Beezooka.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("Beezooka.Fired", "<green>Beezooka fired with velocity {velocity}!");
        messages.addDefault("Beezooka.Name", "<yellow>Beezooka Bee!");
        messages.addDefault("Beezooka.Velocity", 2.5);

        this.defaultVelocity = messages.getConfig().getDouble("Beezooka.Velocity", 2.5);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Beezooka.PlayerOnly"));
            return true;
        }

        double velocity = defaultVelocity;

        if (args.length > 0) {
            try {
                velocity = Double.parseDouble(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        Bee bee = (Bee) player.getWorld().spawnEntity(
                player.getEyeLocation().add(player.getLocation().getDirection()),
                org.bukkit.entity.EntityType.BEE
        );

        Vector direction = player.getLocation().getDirection().multiply(velocity);
        bee.setVelocity(direction);

        bee.setAnger(0);

        String nameRaw = messages.getConfig().getString("Beezooka.Name", "<yellow>Beezooka Bee!");
        Component nameComponent = miniMessage.deserialize(nameRaw);
        bee.customName(nameComponent);
        bee.setCustomNameVisible(true);

        String firedRaw = messages.getConfig().getString("Beezooka.Fired", "<green>Beezooka fired!");
        firedRaw = firedRaw.replace("{velocity}", String.valueOf(velocity));
        player.sendMessage(miniMessage.deserialize(firedRaw));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (int i = 1; i <= 5; i++) suggestions.add(String.valueOf(i));
            return suggestions;
        }
        return null;
    }
}
