package com.serveressentials.api.shop;

/**
 * Configuration interface for shop settings
 */
public interface ShopConfig {
    boolean isEnabled();
    String getMainTitle();
    int getMainSize();
    int getCloseButtonSlot();
    int getDefaultSectionSize();
    int getPlayerHeadSlot();
    int getMaxPages();
    String getCurrencySymbol();
    boolean isSellEnabled();
}