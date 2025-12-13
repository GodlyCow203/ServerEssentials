package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.SpawnerConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class SpawnerCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "serveressentials.command.spawner";

    private final PlayerLanguageManager langManager;
    private final SpawnerConfig config;

    public SpawnerCommand(PlayerLanguageManager langManager, SpawnerConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.spawner.only-player",
                    "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.no-permission",
                    "<red>You don't have permission to use this command!"));
            return true;
        }

        if (player.getTargetBlockExact(5) == null || !(player.getTargetBlockExact(5).getState() instanceof CreatureSpawner spawner)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.look-at-spawner",
                    "<red>Look at a spawner to change it!"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.usage",
                    "<yellow>Usage: /spawner <mob>"));
            return true;
        }

        String mobName = args[0].toUpperCase(Locale.ROOT);
        try {
            EntityType type = EntityType.valueOf(mobName);
            String mobNameLower = mobName.toLowerCase(Locale.ROOT);
            String mobPermission = "serveressentials.command.spawner." + mobNameLower;

            if (!player.hasPermission("serveressentials.command.spawner.*") && !player.hasPermission(mobPermission)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.spawner.no-permission-mob",
                        "<red>You don't have permission to set a spawner to {mob}!",
                        ComponentPlaceholder.of("{mob}", mobNameLower)));
                return true;
            }

            spawner.setSpawnedType(type);
            spawner.update();

            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.changed",
                    "<green>Spawner type changed to {mob}",
                    ComponentPlaceholder.of("{mob}", mobNameLower)));
        } catch (IllegalArgumentException e) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.invalid-mob",
                    "<red>Invalid mob type: {mob}",
                    ComponentPlaceholder.of("{mob}", args[0])));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player) || !player.hasPermission(PERMISSION)) {
            return suggestions;
        }

        if (args.length == 1) {
            for (EntityType type : EntityType.values()) {
                if (!type.isAlive()) continue;

                String mobName = type.name().toLowerCase(Locale.ROOT);
                String mobPermission = "serveressentials.spawner." + mobName;

                if (player.hasPermission("serveressentials.spawner.*") || player.hasPermission(mobPermission)) {
                    suggestions.add(mobName);
                }
            }
        }

        return suggestions;
    }
}