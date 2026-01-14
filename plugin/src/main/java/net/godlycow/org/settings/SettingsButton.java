package net.godlycow.org.settings;

import org.bukkit.Material;

import java.util.List;

public final class SettingsButton {
    private final String id;
    private final int position;
    private final Material material;
    private final String nameKey;
    private final List<String> loreKeys;
    private final boolean glow;
    private final String command;
    private final String permission;

    public SettingsButton(String id, int position, Material material, String nameKey,
                          List<String> loreKeys, boolean glow, String command, String permission) {
        this.id = id;
        this.position = position;
        this.material = material;
        this.nameKey = nameKey;
        this.loreKeys = loreKeys != null ? List.copyOf(loreKeys) : List.of();
        this.glow = glow;
        this.command = command;
        this.permission = permission;
    }

    public String getId() { return id; }
    public int getPosition() { return position; }
    public Material getMaterial() { return material; }
    public String getNameKey() { return nameKey; }
    public List<String> getLoreKeys() { return loreKeys; }
    public boolean hasGlow() { return glow; }
    public String getCommand() { return command; }
    public String getPermission() { return permission; }
    public boolean hasPermission() { return permission != null && !permission.isEmpty(); }
}