package serveressentials.serveressentials;

import net.md_5.bungee.api.ChatColor;

public class Rank {
    private final String name;
    private final String displayName;
    private final String permissionGroup;
    private final int weight;
    private final String prefix;
    private final ChatColor color;

    public Rank(String name, String displayName, ChatColor color, String permissionGroup, int weight, String prefix) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.permissionGroup = permissionGroup;
        this.weight = weight;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getPermissionGroup() {
        return permissionGroup;
    }

    public int getWeight() {
        return weight;
    }

    public String getPrefix() {
        return prefix;
    }
}
