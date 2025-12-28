package com.serveressentials.api.shop;

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