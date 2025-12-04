package net.lunark.io.homes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.HomeMessages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeListener implements Listener {

    private final ServerEssentials plugin;
    private final HomeManager homeManager;
    private final HomesGUI homesGUI;
    private final HomeMessages messages;

    private final Map<UUID, PendingAction> pending = new HashMap<>();
    private final Map<UUID, Integer> awaitingRename = new HashMap<>();

    public HomeListener(ServerEssentials plugin, HomeManager homeManager, HomesGUI homesGUI, HomeMessages messages) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.homesGUI = homesGUI;
        this.messages = messages;
    }

    private static class PendingAction {
        enum Action { SET, REMOVE, RENAME }
        public final Action action;
        public final int index;

        public PendingAction(Action action, int index) {
            this.action = action;
            this.index = index;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        UUID uuid = p.getUniqueId();

        if (e.getInventory().getHolder() instanceof HomesMainHolder) {
            e.setCancelled(true);
            int slot = e.getRawSlot();
            for (int i = 0; i < homesGUI.getBedSlots().length; i++)
                if (slot == homesGUI.getBedSlots()[i])
                    handleBedClick(p, i + 1, e.getClick());

            for (int i = 0; i < homesGUI.getDyeSlots().length; i++)
                if (slot == homesGUI.getDyeSlots()[i])
                    handleDyeClick(p, i + 1);
        }

        if (e.getInventory().getHolder() instanceof HomesConfirmHolder) {
            e.setCancelled(true);
            PendingAction pa = pending.get(uuid);
            if (pa == null) { p.closeInventory(); return; }

            int slot = e.getRawSlot();
            if (slot == 10) { // confirm
                switch (pa.action) {
                    case SET -> {
                        homeManager.setHome(uuid, pa.index, new Home("Home" + pa.index, p.getLocation()));
                        p.sendMessage(messages.get("msg.home-set",
                                "{home}", String.valueOf(pa.index),
                                "{x}", String.valueOf(Math.round(p.getLocation().getX())),
                                "{y}", String.valueOf(Math.round(p.getLocation().getY())),
                                "{z}", String.valueOf(Math.round(p.getLocation().getZ()))));
                        pending.remove(uuid);
                    }
                    case REMOVE -> {
                        homeManager.removeHome(uuid, pa.index);
                        p.sendMessage(messages.get("msg.home-removed",
                                "{home}", String.valueOf(pa.index)));
                        pending.remove(uuid);
                    }
                    case RENAME -> {
                        awaitingRename.put(uuid, pa.index);
                        pending.remove(uuid);
                        p.closeInventory();
                        p.sendMessage(messages.get("msg.rename-prompt"));
                    }
                }
                Bukkit.getScheduler().runTask(plugin, () -> homesGUI.openMain(p));
            } else if (slot == 16) {
                pending.remove(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> homesGUI.openMain(p));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (!awaitingRename.containsKey(uuid)) return;

        e.setCancelled(true);
        int index = awaitingRename.remove(uuid);
        String msg = e.getMessage().trim();

        homeManager.getHome(uuid, index).ifPresent(h -> {
            h.setName(msg);
            homeManager.setHome(uuid, index, h);
            p.sendMessage(messages.get("msg.renamed", "{name}", msg));
            Bukkit.getScheduler().runTask(plugin, () -> homesGUI.openMain(p));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        awaitingRename.remove(uuid);
        pending.remove(uuid);
    }

    private void handleBedClick(Player p, int homeIndex, org.bukkit.event.inventory.ClickType clickType) {
        UUID uuid = p.getUniqueId();

        boolean allowed = homesGUI.canSetHome(p, homeIndex);
        boolean hasHome = homeManager.getHome(uuid, homeIndex).isPresent();

        if (!allowed) {
            p.sendMessage(messages.get("msg.no-permission"));
            return;
        }

        if (hasHome && clickType.isLeftClick()) {
            homeManager.getHome(uuid, homeIndex).ifPresent(h -> {
                if (h.toLocation() != null) p.teleport(h.toLocation());
                else p.sendMessage(messages.get("msg.home-world-missing"));
            });
            return;
        }

        if (!hasHome) {
            pending.put(uuid, new PendingAction(PendingAction.Action.SET, homeIndex));
            p.openInventory(homesGUI.createConfirmInventory(p, homeIndex, "set"));
        } else if (p.hasPermission("serveressentials.renamehome")) {
            pending.put(uuid, new PendingAction(PendingAction.Action.RENAME, homeIndex));
            p.openInventory(homesGUI.createConfirmInventory(p, homeIndex, "rename"));
        } else {
            p.sendMessage(messages.get("msg.no-permission"));
        }
    }

    private void handleDyeClick(Player p, int homeIndex) {
        UUID uuid = p.getUniqueId();
        boolean allowed = p.hasPermission("serveressentials.sethome") &&
                (p.hasPermission("serveressentials.sethome.*") ||
                        p.hasPermission("serveressentials.sethome." + homeIndex) ||
                        hasNumericAllow(p));

        boolean removeAllowed = p.hasPermission("serveressentials.removehome");
        boolean hasHome = homeManager.getHome(uuid, homeIndex).isPresent();

        if (!allowed || !removeAllowed) {
            p.sendMessage(messages.get("msg.no-permission"));
            return;
        }

        if (!hasHome) {
            p.sendMessage(messages.get("lore.empty"));
            return;
        }

        pending.put(uuid, new PendingAction(PendingAction.Action.REMOVE, homeIndex));
        p.openInventory(homesGUI.createConfirmInventory(p, homeIndex, "remove"));
    }

    private boolean hasNumericAllow(Player p) {
        for (int i = 1; i <= 8; i++) {
            if (p.hasPermission("serveressentials.sethome." + i)) return true;
        }
        return false;
    }
}
