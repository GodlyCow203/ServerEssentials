package serveressentials.serveressentials.server;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.ServerMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpawnerCommand implements CommandExecutor, TabCompleter {

    private final ServerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SpawnerCommand(ServerMessages messages) {
        this.messages = messages;

        // Default messages
        messages.addDefault("Spawner.PlayerOnly", "<red>Only players can use this command!");
        messages.addDefault("Spawner.NoPermission", "<red>You don’t have permission to set a spawner to {mob}!");
        messages.addDefault("Spawner.LookAtSpawner", "<red>Look at a spawner to change it!");
        messages.addDefault("Spawner.Usage", "<yellow>Usage: /spawner <mob>");
        messages.addDefault("Spawner.InvalidMob", "<red>Invalid mob type: {mob}");
        messages.addDefault("Spawner.Changed", "<green>Spawner type changed to {mob}");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Spawner.PlayerOnly"));
            return true;
        }

        if (player.getTargetBlockExact(5) != null && player.getTargetBlockExact(5).getState() instanceof CreatureSpawner spawner) {

            if (args.length > 0) {
                String mobName = args[0].toUpperCase(Locale.ROOT);

                try {
                    EntityType type = EntityType.valueOf(mobName);

                    // Permission check
                    if (!(player.hasPermission("serveressentials.spawner.*")
                            || player.hasPermission("serveressentials.spawner." + mobName.toLowerCase(Locale.ROOT)))) {

                        sender.sendMessage(miniMessage.deserialize(
                                messages.getConfig().getString("Spawner.NoPermission")
                                        .replace("{mob}", mobName.toLowerCase(Locale.ROOT))
                        ));
                        return true;
                    }

                    spawner.setSpawnedType(type);
                    spawner.update();

                    sender.sendMessage(miniMessage.deserialize(
                            messages.getConfig().getString("Spawner.Changed")
                                    .replace("{mob}", mobName.toLowerCase(Locale.ROOT))
                    ));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(miniMessage.deserialize(
                            messages.getConfig().getString("Spawner.InvalidMob")
                                    .replace("{mob}", args[0])
                    ));
                }
            } else {
                sender.sendMessage(messages.get("Spawner.Usage"));
            }

        } else {
            sender.sendMessage(messages.get("Spawner.LookAtSpawner"));
        }

        return true;
    }

    // ---------------- TAB COMPLETION ----------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player)) {
            return suggestions;
        }

        if (args.length == 1) {
            for (EntityType type : EntityType.values()) {
                String mobName = type.name().toLowerCase(Locale.ROOT);

                // skip weird entities (like unknown, players, etc.)
                if (!type.isAlive()) continue;

                if (player.hasPermission("serveressentials.spawner.*")
                        || player.hasPermission("serveressentials.spawner." + mobName)) {
                    suggestions.add(mobName);
                }
            }
        }

        return suggestions;
    }
}
