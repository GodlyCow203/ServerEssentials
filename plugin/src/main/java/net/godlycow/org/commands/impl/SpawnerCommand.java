package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.SpawnerConfig;
import net.godlycow.org.language.PlayerLanguageManager;
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
import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SpawnerCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "essc.command.spawner";
    private static final String PERMISSION_ALL_MOBS = "essc.command.spawner.*";

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
                    "<#B22222>❌ Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.no-permission",
                    "<#B22222>❌ You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        if (args.length > 1 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        if (player.getTargetBlockExact(5) == null || !(player.getTargetBlockExact(5).getState() instanceof CreatureSpawner spawner)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.look-at-spawner",
                    "<#B22222>❌ Look at a spawner to change it!"));
            return true;
        }

        String mobName = args[0].toUpperCase(Locale.ROOT);
        try {
            EntityType type = EntityType.valueOf(mobName);
            String mobNameLower = mobName.toLowerCase(Locale.ROOT);
            String mobPermission = "essc.command.spawner." + mobNameLower;

            if (!player.hasPermission(PERMISSION_ALL_MOBS) && !player.hasPermission(mobPermission)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.spawner.no-permission-mob",
                        "<#B22222>❌ You don't have permission to set a spawner to {mob}!",
                        ComponentPlaceholder.of("{mob}", mobNameLower)));
                return true;
            }

            spawner.setSpawnedType(type);
            spawner.update();

            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.changed",
                    "<#50DB00>✔ Spawner type changed to {mob}",
                    ComponentPlaceholder.of("{mob}", mobNameLower)));
        } catch (IllegalArgumentException e) {
            player.sendMessage(langManager.getMessageFor(player, "commands.spawner.invalid-mob",
                    "<#B22222>❌ Invalid mob type: {mob}",
                    ComponentPlaceholder.of("{mob}", args[0])));
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        Component help = Component.empty()
                .append(langManager.getMessageFor(player, "commands.spawner.help.header", "<#FFD900><bold>=== Spawner Command Help ===</bold></#FFD900>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.spawner.help.description", "<#FFD900>Description: <white>Change the type of a creature spawner.</white>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.spawner.help.usage", "<#FFD900>Usage:</#FFD900> <white>/spawner <mob></white>"))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.spawner.help.permission", "<#FFD900>Required Permission:</#FFD900> <white>{permission}</white>",
                        ComponentPlaceholder.of("{permission}", PERMISSION)))
                .append(Component.newline())

                .append(langManager.getMessageFor(player, "commands.spawner.help.available.header", "<#FFD900>Available Mob Types:</#FFD900>"))
                .append(Component.newline());

        List<String> availableMobs = getAvailableMobs(player);
        int mobsToShow = Math.min(5, availableMobs.size());

        for (int i = 0; i < mobsToShow; i++) {
            String mob = availableMobs.get(i);
            help = help.append(langManager.getMessageFor(player, "commands.spawner.help.available.mob",
                            "  <white>• {mob}</white>", ComponentPlaceholder.of("{mob}", mob)))
                    .append(Component.newline());
        }

        if (availableMobs.size() > 5) {
            help = help.append(langManager.getMessageFor(player, "commands.spawner.help.available.more",
                            "  <gray>... and {count} more</gray>", ComponentPlaceholder.of("{count}", availableMobs.size() - 5)))
                    .append(Component.newline());
        }

        help = help.append(langManager.getMessageFor(player, "commands.spawner.help.footer",
                "<gray>Use <white>/spawner help</white> to see this message again.</gray>"));

        player.sendMessage(help);
    }

    private List<String> getAvailableMobs(Player player) {
        List<String> mobs = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (!type.isAlive()) continue;

            String mobName = type.name().toLowerCase(Locale.ROOT);
            String mobPermission = "essc.command.spawner." + mobName;

            if (player.hasPermission(PERMISSION_ALL_MOBS) || player.hasPermission(mobPermission)) {
                mobs.add(mobName);
            }
        }
        return mobs;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player player) || !player.hasPermission(PERMISSION)) {
            return suggestions;
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);

            if ("help".startsWith(partial)) {
                suggestions.add("help");
            }

            for (EntityType type : EntityType.values()) {
                if (!type.isAlive()) continue;

                String mobName = type.name().toLowerCase(Locale.ROOT);
                String mobPermission = "essc.command.spawner." + mobName;

                if ((player.hasPermission(PERMISSION_ALL_MOBS) || player.hasPermission(mobPermission))
                        && mobName.startsWith(partial)) {
                    suggestions.add(mobName);
                }
            }
        }

        return suggestions;
    }
}