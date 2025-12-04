package net.lunark.io.Fun;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import net.lunark.io.util.FunMessages;

import java.util.List;

public final class CelebrateCommand implements CommandExecutor {

    private final FunMessages funMessages;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final FireworkEffect EFFECT = FireworkEffect.builder()
            .flicker(true)
            .trail(true)
            .with(Type.STAR)
            .withColor(Color.fromRGB(0x55C4AA))
            .withFade(Color.fromRGB(0xAA88CC))
            .build();

    public CelebrateCommand(FunMessages funMessages) {
        this.funMessages = funMessages;
        funMessages.addDefault("celebrate.only-players", "<#55AA55>Only players can use this.");
        funMessages.addDefault("celebrate.player-not-found", "<#CC6B6B>Player not found.");
        funMessages.addDefault("celebrate.no-permission", "<#CC6B6B>Missing permission: <white>serveressentials.celebrate");
        funMessages.addDefault("celebrate.message", "<#88C488>🎉 Boom! Celebration!");
        funMessages.addDefault("celebrate.message-other", "<#88C488>🎉 Celebration for <white><target><#88C488>!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        boolean silent = false;
        String targetName = null;
        for (String arg : args) {
            if (arg.equals("-s")) silent = true;
            else if (targetName == null) targetName = arg;
        }

        Player player;
        if (targetName == null) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(mm.deserialize(funMessages.getConfig().getString("celebrate.only-players")));
                return true;
            }
            player = p;
        } else {
            player = sender.getServer().getPlayer(targetName);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(mm.deserialize(
                        funMessages.getConfig().getString("celebrate.player-not-found")));
                return true;
            }
        }

        if (!sender.hasPermission("serveressentials.celebrate")) {
            sender.sendMessage(mm.deserialize(funMessages.getConfig().getString("celebrate.no-permission")));
            return true;
        }

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return true;

        Bukkit.getScheduler().runTask(
                Bukkit.getPluginManager().getPlugin("ServerEssentials"),
                () -> {
                    Firework fw = (Firework) world.spawnEntity(loc, EntityType.FIREWORK_ROCKET);
                    FireworkMeta meta = fw.getFireworkMeta();
                    meta.addEffect(EFFECT);
                    meta.setPower(1);
                    fw.setFireworkMeta(meta);
                }
        );

        if (!silent) {
            String key = sender.equals(player) ? "celebrate.message" : "celebrate.message-other";
            String raw = funMessages.getConfig().getString(key).replace("<target>", player.getName());
            sender.sendMessage(mm.deserialize(raw));
        }
        return true;
    }

    public @NotNull List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Entity::getName)
                    .filter(n -> n.regionMatches(true, 0, args[0], 0, args[0].length()))
                    .collect(ImmutableList.toImmutableList());
        }
        if (args.length == 2 && "-s".startsWith(args[1].toLowerCase())) {
            return ImmutableList.of("-s");
        }
        return ImmutableList.of();
    }
}