package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.PingAllConfig;
import net.lunark.io.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class PingAllCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.pingall";
    private final PlayerLanguageManager langManager;
    private final PingAllConfig config;

    public PingAllCommand(PlayerLanguageManager langManager, PingAllConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player playerSender = (sender instanceof Player) ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.pingall.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        sender.sendMessage(langManager.getMessageFor(playerSender, "commands.pingall.header", "<green>Online Player Pings:"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping = getPlayerPing(player);
            String pingDisplay = ping >= 0 ? ping + "ms" : "N/A";

            Component line = langManager.getMessageFor(playerSender, "commands.pingall.player-line", "<yellow>{player}: <aqua>{ping}",
                    ComponentPlaceholder.of("{player}", player.getName()),
                    ComponentPlaceholder.of("{ping}", pingDisplay)
            );
            sender.sendMessage(line);
        }

        return true;
    }

    private int getPlayerPing(Player player) {
        try {
            Method getPingMethod = Player.class.getMethod("getPing");
            return (int) getPingMethod.invoke(player);
        } catch (NoSuchMethodException ignored) {
            try {
                String version = getServerVersion();
                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                Object craftPlayer = craftPlayerClass.cast(player);
                Method getHandle = craftPlayerClass.getMethod("getHandle");
                Object entityPlayer = getHandle.invoke(craftPlayer);
                Field pingField = entityPlayer.getClass().getField("ping");
                return pingField.getInt(entityPlayer);
            } catch (Exception e) {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private String getServerVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}