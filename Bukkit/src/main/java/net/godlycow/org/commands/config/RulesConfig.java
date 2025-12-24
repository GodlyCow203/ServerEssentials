package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

/**
 * Configuration for /rules command and GUI
 * Permission: serveressentials.command.rules (hardcoded)
 * Reload Permission: serveressentials.command.rules.reload (hardcoded)
 */
public final class RulesConfig {
    private final String title;
    private final String acceptButtonText;
    private final String declineButtonText;
    private final boolean mustAccept;
    private final String kickMessage;
    private final String permission;
    private final String reloadPermission;
    private final String guiFillerMaterial;
    private final boolean forceAcceptance;
    private final int acceptButtonSlot;
    private final int declineButtonSlot;

    public RulesConfig(Plugin plugin) {
        this.title = plugin.getConfig().getString("rules.title", "<green>Server Rules");
        this.acceptButtonText = plugin.getConfig().getString("rules.accept-button-text", "<green><bold>✓ ACCEPT");
        this.declineButtonSlot = plugin.getConfig().getInt("rules.buttons.decline.slot", 53);
        this.declineButtonText = plugin.getConfig().getString("rules.decline-button-text", "<red><bold>✗ DECLINE");
        this.mustAccept = plugin.getConfig().getBoolean("rules.must-accept", true);
        this.forceAcceptance = plugin.getConfig().getBoolean("rules.force-acceptance", true);
        this.kickMessage = plugin.getConfig().getString("rules.kick-message",
                "<red>You must accept the rules to join this server!");
        this.guiFillerMaterial = plugin.getConfig().getString("rules.gui.filler-material", "GRAY_STAINED_GLASS_PANE");
        this.acceptButtonSlot = plugin.getConfig().getInt("rules.buttons.accept.slot", 45);
        this.permission = "serveressentials.command.rules";
        this.reloadPermission = "serveressentials.command.rules.reload";
    }

    // Getter methods
    public String title() { return title; }
    public String acceptButtonText() { return acceptButtonText; }
    public String declineButtonText() { return declineButtonText; }
    public boolean mustAccept() { return mustAccept; }
    public boolean forceAcceptance() { return forceAcceptance; }
    public String kickMessage() { return kickMessage; }
    public String permission() { return permission; }
    public String reloadPermission() { return reloadPermission; }
    public String guiFillerMaterial() { return guiFillerMaterial; }
    public int acceptButtonSlot() { return acceptButtonSlot; }
    public int declineButtonSlot() { return declineButtonSlot; }
}