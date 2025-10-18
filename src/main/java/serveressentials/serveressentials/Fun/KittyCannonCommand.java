package serveressentials.serveressentials.Fun;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import serveressentials.serveressentials.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

public class KittyCannonCommand implements CommandExecutor, TabCompleter {

    private final FunMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final double defaultVelocity;

    public KittyCannonCommand(FunMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("KittyCannon.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("KittyCannon.Fired", "<green>Kitty cannon fired with velocity {velocity}!");
        messages.addDefault("KittyCannon.Name", "<light_purple>Kitty Cannon!");
        messages.addDefault("KittyCannon.Velocity", 2.0); // default velocity

        this.defaultVelocity = messages.getConfig().getDouble("KittyCannon.Velocity", 2.0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("KittyCannon.PlayerOnly"));
            return true;
        }

        double velocity = defaultVelocity;

        if (args.length > 0) {
            try {
                velocity = Double.parseDouble(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Spawn kitty
        LivingEntity kitty = (LivingEntity) player.getWorld().spawnEntity(
                player.getEyeLocation().add(player.getLocation().getDirection()),
                EntityType.CAT
        );

        // Set velocity
        Vector direction = player.getLocation().getDirection().multiply(velocity);
        kitty.setVelocity(direction);

        // Set name
        String nameRaw = messages.getConfig().getString("KittyCannon.Name");
        if (nameRaw == null) nameRaw = "<light_purple>Kitty Cannon!";
        Component nameComponent = miniMessage.deserialize(nameRaw);
        kitty.customName(nameComponent);
        kitty.setCustomNameVisible(true); // correct method

        // Notify player
        String firedRaw = messages.getConfig().getString("KittyCannon.Fired", "<green>Kitty cannon fired!");
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
