package net.lunark.io.kit;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class KitPermission {
    private static final String PREFIX = "serveressentials.command.kit.";

    public static String node(String kitName) {
        return PREFIX + kitName.toLowerCase();
    }

    public static void register(String kitName) {
        String node = node(kitName);
        if (Bukkit.getPluginManager().getPermission(node) == null) {
            Bukkit.getPluginManager().addPermission(
                    new Permission(node, "Allow access to the " + kitName + " kit.", PermissionDefault.FALSE)
            );
        }
    }

    public static void unregister(String kitName) {
        String node = node(kitName);
        Permission perm = Bukkit.getPluginManager().getPermission(node);
        if (perm != null) {
            Bukkit.getPluginManager().removePermission(perm);
        }
    }
}