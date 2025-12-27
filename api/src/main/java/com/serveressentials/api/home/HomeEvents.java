package com.serveressentials.api.home;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class HomeEvents {

    public static class HomeCreateEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final Home home;
        private final int slot;

        public HomeCreateEvent(Player player, Home home, int slot) {
            this.player = player;
            this.home = home;
            this.slot = slot;
        }

        public Player getPlayer() { return player; }
        public Home getHome() { return home; }
        public int getSlot() { return slot; }

        @Override public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    public static class HomeTeleportEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final Home home;

        public HomeTeleportEvent(Player player, Home home) {
            this.player = player;
            this.home = home;
        }

        public Player getPlayer() { return player; }
        public Home getHome() { return home; }

        @Override public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
}