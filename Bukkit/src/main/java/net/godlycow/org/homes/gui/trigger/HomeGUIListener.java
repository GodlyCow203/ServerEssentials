package net.godlycow.org.homes.gui.trigger;

import net.godlycow.org.homes.gui.HomesConfirmHolder;
import net.godlycow.org.homes.gui.HomesMainHolder;
import net.godlycow.org.homes.model.Home;
import net.godlycow.org.homes.HomeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.godlycow.org.commands.config.HomesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public class HomeGUIListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final HomesConfig config;
    private final HomeManager homeManager;

    private final Map<Player, PendingAction> pendingActions = new WeakHashMap<>();
    private final Map<Player, Integer> awaitingRename = new WeakHashMap<>();

    private static final Integer[] BED_SLOTS = {10, 12, 14, 16, 28, 30, 32, 34};
    private static final Integer[] DYE_SLOTS = {19, 21, 23, 25, 37, 39, 41, 43};

    private enum Action { SET, REMOVE, RENAME }
    private record PendingAction(Action action, int slot) {}
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    public HomeGUIListener(Plugin plugin, PlayerLanguageManager langManager,
                           HomesConfig config, HomeManager homeManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.homeManager = homeManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof HomesMainHolder) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            for (int i = 0; i < BED_SLOTS.length; i++) {
                if (slot == BED_SLOTS[i]) {
                    handleBedClick(player, i + 1, event.getClick());
                    return;
                }
            }

            for (int i = 0; i < DYE_SLOTS.length; i++) {
                if (slot == DYE_SLOTS[i]) {
                    handleDyeClick(player, i + 1);
                    return;
                }
            }
        }

        if (event.getInventory().getHolder() instanceof HomesConfirmHolder holder) {
            event.setCancelled(true);
            PendingAction action = pendingActions.get(player);
            if (action == null) {
                player.closeInventory();
                return;
            }

            if (event.getRawSlot() == 10) {
                confirmAction(player, action);
            } else if (event.getRawSlot() == 16) {
                pendingActions.remove(player);
                openMainGUI(player);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingRename.containsKey(player)) return;

        event.setCancelled(true);
        int slot = awaitingRename.remove(player);
        String newName = event.getMessage().trim();

        homeManager.getHome(player.getUniqueId(), slot).thenAccept(opt -> {
            if (opt.isPresent()) {
                Home home = opt.get();
                home.setName(newName);
                homeManager.setHome(player.getUniqueId(), slot, home).thenAccept(v -> {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.success",
                            "<green>Home renamed to <yellow>{name}</yellow>!",
                            ComponentPlaceholder.of("{name}", newName)));
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                });
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        pendingActions.remove(player);
        awaitingRename.remove(player);
    }

    private void handleBedClick(Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        UUID uuid = player.getUniqueId();

        if (!canSetHome(player, slot)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.no-permission",
                    "<red>You don't have permission to manage home {slot}!",
                    ComponentPlaceholder.of("{slot}", slot)));
            return;
        }

        homeManager.getHome(uuid, slot).thenAccept(opt -> {
            boolean hasHome = opt.isPresent();

            if (hasHome && clickType.isLeftClick()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Location loc = opt.get().toLocation();
                    if (loc != null) player.teleport(loc);
                    else player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-missing",
                            "<red>Home world is no longer available!"));
                });
            } else if (!hasHome) {
                pendingActions.put(player, new PendingAction(Action.SET, slot));
                openConfirmGUI(player, slot, "set");
            } else if (config.allowRename && player.hasPermission("serveressentials.renamehome")) {
                pendingActions.put(player, new PendingAction(Action.RENAME, slot));
                openConfirmGUI(player, slot, "rename");
            }
        });
    }

    private void handleDyeClick(Player player, int slot) {
        UUID uuid = player.getUniqueId();

        if (!player.hasPermission("serveressentials.command.removehome")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.no-permission-remove",
                    "<red>You don't have permission to remove homes!"));
            return;
        }

        homeManager.getHome(uuid, slot).thenAccept(opt -> {
            if (opt.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.homes.empty-slot",
                        "<gray>No home set in slot {slot}!",
                        ComponentPlaceholder.of("{slot}", slot)));
                return;
            }

            pendingActions.put(player, new PendingAction(Action.REMOVE, slot));
            openConfirmGUI(player, slot, "remove");
        });
    }

    private void confirmAction(Player player, PendingAction action) {
        UUID uuid = player.getUniqueId();

        switch (action.action) {
            case SET -> {
                Home home = new Home("Home" + action.slot, player.getLocation());
                homeManager.setHome(uuid, action.slot, home).thenAccept(v -> {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.set.success",
                            "<green>Home <yellow>{slot}</yellow> set!",
                            ComponentPlaceholder.of("{slot}", action.slot)));
                });
            }
            case REMOVE -> {
                homeManager.removeHome(uuid, action.slot).thenAccept(v -> {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.remove.success",
                            "<green>Home <yellow>{slot}</yellow> removed!",
                            ComponentPlaceholder.of("{slot}", action.slot)));
                });
            }
            case RENAME -> {
                awaitingRename.put(player, action.slot);
                player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.prompt",
                        "<yellow>Enter a new name for this home in chat:"));
            }
        }

        pendingActions.remove(player);
        Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
    }

    public void openMainGUI(Player player) {
        getEmptyInventory(player, config.guiTitleMain, 54).thenAccept(inv -> {
            fillBackground(inv);
            setupHomeSlots(player, inv);
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        });
    }

    public void openConfirmGUI(Player player, int slot, String mode) {
        String titleKey = "commands.homes.gui.titles.confirm";
        Component titleComponent = langManager.getMessageFor(null, titleKey, config.guiTitleConfirm);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = Bukkit.createInventory(new HomesConfirmHolder(slot, mode), 27,
                    legacySerializer.serialize(titleComponent));

            ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta confirmMeta = confirm.getItemMeta();
            Component confirmName = langManager.getMessageFor(player, "commands.homes.gui.confirm", "<green>Confirm");
            confirmMeta.setDisplayName(legacySerializer.serialize(confirmName));
            confirm.setItemMeta(confirmMeta);
            inv.setItem(10, confirm);

            ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta cancelMeta = cancel.getItemMeta();
            Component cancelName = langManager.getMessageFor(player, "commands.homes.gui.cancel", "<red>Cancel");
            cancelMeta.setDisplayName(legacySerializer.serialize(cancelName));
            cancel.setItemMeta(cancelMeta);
            inv.setItem(16, cancel);

            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = info.getItemMeta();
            Component infoName = langManager.getMessageFor(player, "commands.homes.gui.info",
                    "<white>Home {slot} - {mode}",
                    ComponentPlaceholder.of("{slot}", slot),
                    ComponentPlaceholder.of("{mode}", mode));
            infoMeta.setDisplayName(legacySerializer.serialize(infoName));
            inv.setItem(13, info);

            fillConfirmBackground(inv);
            player.openInventory(inv);
        });
    }

    private CompletableFuture<Inventory> getEmptyInventory(Player player, String titleKey, int size) {
        return CompletableFuture.supplyAsync(() -> {
            Component title = langManager.getMessageFor(null, titleKey, "Home GUI");
            return Bukkit.createInventory(new HomesMainHolder(), size, title);
        });
    }

    private void setupHomeSlots(Player player, Inventory inv) {
        UUID uuid = player.getUniqueId();
        CompletableFuture.allOf(
                homeManager.getAllHomes(uuid).thenAccept(homes -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (int i = 0; i < Math.min(8, config.maxHomes); i++) {
                            int slot = i + 1;
                            Home home = homes.get(slot);
                            if (home != null) {
                                inv.setItem(BED_SLOTS[i], createBedItem(player, slot, home, true));
                                inv.setItem(DYE_SLOTS[i], createDyeItem(player, slot, true));
                            } else {
                                inv.setItem(BED_SLOTS[i], createBedItem(player, slot, null, false));
                                inv.setItem(DYE_SLOTS[i], createDyeItem(player, slot, false));
                            }
                        }
                    });
                })
        );
    }

    private ItemStack createBedItem(Player player, int slot, Home home, boolean hasHome) {
        Material material = !canSetHome(player, slot) ? Material.RED_BED :
                !hasHome ? Material.GRAY_BED : Material.BLUE_BED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component nameComponent = langManager.getMessageFor(player, "commands.homes.gui.home-title",
                "<white>Home {slot}",
                ComponentPlaceholder.of("{slot}", slot));
        meta.setDisplayName(legacySerializer.serialize(nameComponent));

        List<String> lore = new ArrayList<>();
        if (!canSetHome(player, slot)) {
            Component noPerm = langManager.getMessageFor(player, "commands.homes.gui.no-permission", "<red>No Permission!");
            lore.add(legacySerializer.serialize(noPerm));
        } else if (!hasHome) {
            Component clickSet = langManager.getMessageFor(player, "commands.homes.gui.click-set", "<yellow>Click to set");
            lore.add(legacySerializer.serialize(clickSet));
        } else {
            Component nameLore = langManager.getMessageFor(player, "commands.homes.gui.name",
                    "<gray>Name: <white><name>",
                    ComponentPlaceholder.of("<name>", home.getName()));
            lore.add(legacySerializer.serialize(nameLore));

            Component coords = langManager.getMessageFor(player, "commands.homes.gui.coords",
                    "<gray>XYZ: <white>{x}, {y}, {z}",
                    ComponentPlaceholder.of("{x}", Math.round(home.getX())),
                    ComponentPlaceholder.of("{y}", Math.round(home.getY())),
                    ComponentPlaceholder.of("{z}", Math.round(home.getZ())));
            lore.add(legacySerializer.serialize(coords));

            Component teleport = langManager.getMessageFor(player, "commands.homes.gui.click-teleport", "<green>Left-click to teleport");
            lore.add(legacySerializer.serialize(teleport));

            Component manage = langManager.getMessageFor(player, "commands.homes.gui.click-manage", "<yellow>Right-click to manage");
            lore.add(legacySerializer.serialize(manage));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDyeItem(Player player, int slot, boolean hasHome) {
        if (!player.hasPermission("serveressentials.command.removehome")) {
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            meta.setDisplayName("");
            filler.setItemMeta(meta);
            return filler;
        }

        Material material = !hasHome ? Material.GRAY_DYE : Material.LIME_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component nameComponent = langManager.getMessageFor(player, "commands.homes.gui.manage-title",
                "<white>Manage Home {slot}",
                ComponentPlaceholder.of("{slot}", slot));
        meta.setDisplayName(legacySerializer.serialize(nameComponent));

        List<String> lore = new ArrayList<>();
        if (!hasHome) {
            Component empty = langManager.getMessageFor(player, "commands.homes.gui.empty", "<gray>Empty slot");
            lore.add(legacySerializer.serialize(empty));
        } else {
            Component remove = langManager.getMessageFor(player, "commands.homes.gui.click-remove", "<red>Click to remove");
            lore.add(legacySerializer.serialize(remove));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBackground(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName("");
        filler.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private void fillConfirmBackground(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName("");
        filler.setItemMeta(meta);

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private boolean canSetHome(Player player, int slot) {
        if (!player.hasPermission("serveressentials.command.sethome")) return false;
        if (player.hasPermission("serveressentials.command.sethome.*")) return true;
        return player.hasPermission("serveressentials.command.sethome." + slot);
    }
}