package net.godlycow.org.commands.config;

import org.bukkit.plugin.Plugin;

public class AuctionConfig {
    private final Plugin plugin;
    public final boolean enabled;
    public final int guiRows;
    public final int itemsPerPage;
    public final int maxSellLimit;
    public final int expirationDays;
    public final double maxPriceLimit;
    public final int maxItemsPerPlayer;
    public final String guiTitle;
    public final String myItemsTitle;
    public final String removeConfirmTitle;

    public AuctionConfig(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("commands.auction.enabled", true);
        this.guiRows = plugin.getConfig().getInt("commands.auction.gui.rows", 6);
        this.itemsPerPage = plugin.getConfig().getInt("commands.auction.items-per-page", 36);
        this.maxSellLimit = plugin.getConfig().getInt("commands.auction.max-sell-limit", 64);
        this.expirationDays = plugin.getConfig().getInt("commands.auction.expiration-days", 7);
        this.maxPriceLimit = plugin.getConfig().getDouble("commands.auction.max-price-limit", 1000000.0);
        this.maxItemsPerPlayer = plugin.getConfig().getInt("commands.auction.max-items-per-player", 10);
        this.guiTitle = plugin.getConfig().getString("commands.auction.gui.titles.auction", "<green>Auction House - Page {page}");
        this.myItemsTitle = plugin.getConfig().getString("commands.auction.gui.titles.my-items", "<green>Your Items - Page {page}");
        this.removeConfirmTitle = plugin.getConfig().getString("commands.auction.gui.titles.remove-confirm", "<red>Confirm Removal");
    }
}