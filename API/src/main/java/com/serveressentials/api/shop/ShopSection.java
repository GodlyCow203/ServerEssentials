package com.serveressentials.api.shop;

import java.util.Collections;
import java.util.Map;

/**
 * Data transfer object for shop sections
 */
public class ShopSection {
    private final String title;
    private final int size;
    private final int pages;
    private final int playerHeadSlot;
    private final int closeButtonSlot;
    private final Map<Integer, ShopLayout> layout;
    private final Map<String, ShopItem> items;

    public ShopSection(String title, int size, int pages, int playerHeadSlot, int closeButtonSlot,
                       Map<Integer, ShopLayout> layout, Map<String, ShopItem> items) {
        this.title = title;
        this.size = size;
        this.pages = pages;
        this.playerHeadSlot = playerHeadSlot;
        this.closeButtonSlot = closeButtonSlot;
        this.layout = Map.copyOf(layout);
        this.items = Map.copyOf(items);
    }

    // Getters
    public String getTitle() { return title; }
    public int getSize() { return size; }
    public int getPages() { return pages; }
    public int getPlayerHeadSlot() { return playerHeadSlot; }
    public int getCloseButtonSlot() { return closeButtonSlot; }
    public Map<Integer, ShopLayout> getLayout() { return Collections.unmodifiableMap(layout); }
    public Map<String, ShopItem> getItems() { return Collections.unmodifiableMap(items); }
}