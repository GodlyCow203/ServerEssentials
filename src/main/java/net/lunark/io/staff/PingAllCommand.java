package net.lunark.io.staff;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PingAllCommand implements CommandExecutor {

    private final MessagesManager messages;
    private final BukkitAudiences adventure;

    public PingAllCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);
        this.adventure = BukkitAudiences.create(plugin);

        // Defaults (MiniMessage format)
        messages.addDefault("pingall.no-permission", "<red>You do not have permission to use this command.");
        messages.addDefault("pingall.header", "<green>Online Player Pings:");
        messages.addDefault("pingall.player-line", "<yellow><player>: <aqua><ping>");
        messages.addDefault("pingall.ping-na", "N/A");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.pingall")) {
            send(sender, messages.getMessageComponent("pingall.no-permission"));
            return true;
        }

        send(sender, messages.getMessageComponent("pingall.header"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping = getPlayerPing(player);
            String pingDisplay = ping >= 0 ? ping + "ms" : messages.getConfig().getString("pingall.ping-na", "N/A");

            Component line = messages.getMessageComponent(
                    "pingall.player-line",
                    "<player>", player.getName(),
                    "<ping>", pingDisplay
            );
            send(sender, line);
        }

        return true;
    }

    private int getPlayerPing(Player player) {
        try {
            // Paper API
            Method getPingMethod = Player.class.getMethod("getPing");
            return (int) getPingMethod.invoke(player);
        } catch (NoSuchMethodException ignored) {
            try {
                // Reflection fallback for CraftBukkit/Spigot
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

    private void send(CommandSender sender, Component message) {
        adventure.sender(sender).sendMessage(message);
    }
}
