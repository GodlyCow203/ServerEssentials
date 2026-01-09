package net.godlycow.org.commands.config;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

public final class RulesConfig {
    private final String title;
    private final String acceptButtonText;
    private final String declineButtonText;
    private final boolean mustAccept;
    private final String kickMessage;
    private final String permission;
    private final String reloadPermission;
    private final String guiFillerMaterial;
    private final String guiBorderMaterial;
    private final boolean forceAcceptance;
    private final boolean enablePagination;
    private final int rulesPerPage;
    private final int acceptButtonSlot;
    private final int declineButtonSlot;

    private final String ruleItemMaterial;
    private final String acceptButtonMaterial;
    private final String declineButtonMaterial;

    public RulesConfig(Plugin plugin) {
        this.title = plugin.getConfig().getString("rules.title", "<gradient:#fbbf24:#d97706>Server Rules</gradient>");
        this.acceptButtonText = plugin.getConfig().getString("rules.accept-button-text", "<green><bold>✔ Accept");
        this.declineButtonText = plugin.getConfig().getString("rules.decline-button-text", "<red><bold>✖ Decline");
        this.mustAccept = plugin.getConfig().getBoolean("rules.must-accept", true);
        this.forceAcceptance = plugin.getConfig().getBoolean("rules.force-acceptance", true);
        this.kickMessage = plugin.getConfig().getString("rules.kick-message",
                "<red>You must accept the rules to join this server!");

        this.permission = "serveressentials.command.rules";
        this.reloadPermission = "serveressentials.command.rules.reload";

        this.guiFillerMaterial = plugin.getConfig().getString("rules.gui.filler-material", "LIGHT_GRAY_STAINED_GLASS_PANE");
        this.guiBorderMaterial = plugin.getConfig().getString("rules.gui.border-material", "GRAY_STAINED_GLASS_PANE");
        this.ruleItemMaterial = plugin.getConfig().getString("rules.gui.rule-item-material", "PAPER");
        this.acceptButtonMaterial = plugin.getConfig().getString("rules.gui.accept-button-material", "LIME_CONCRETE");
        this.declineButtonMaterial = plugin.getConfig().getString("rules.gui.decline-button-material", "RED_CONCRETE");

        this.acceptButtonSlot = plugin.getConfig().getInt("rules.buttons.accept.slot", 47);
        this.declineButtonSlot = plugin.getConfig().getInt("rules.buttons.decline.slot", 51);
        this.enablePagination = plugin.getConfig().getBoolean("rules.gui.enable-pagination", true);
        this.rulesPerPage = plugin.getConfig().getInt("rules.gui.rules-per-page", 21);
    }

    public String title() { return title; }
    public String acceptButtonText() { return acceptButtonText; }
    public String declineButtonText() { return declineButtonText; }
    public boolean mustAccept() { return mustAccept; }
    public boolean forceAcceptance() { return forceAcceptance; }
    public String kickMessage() { return kickMessage; }
    public String permission() { return permission; }
    public String reloadPermission() { return reloadPermission; }
    public String guiFillerMaterial() { return guiFillerMaterial; }
    public String guiBorderMaterial() { return guiBorderMaterial; }
    public String ruleItemMaterial() { return ruleItemMaterial; }
    public String acceptButtonMaterial() { return acceptButtonMaterial; }
    public String declineButtonMaterial() { return declineButtonMaterial; }
    public int acceptButtonSlot() { return acceptButtonSlot; }
    public int declineButtonSlot() { return declineButtonSlot; }
    public boolean enablePagination() { return enablePagination; }
    public int rulesPerPage() { return rulesPerPage; }
}