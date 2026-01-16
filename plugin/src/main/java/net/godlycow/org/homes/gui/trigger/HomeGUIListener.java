package net.godlycow.org.homes.gui.trigger;

import net.godlycow.org.homes.gui.HomesConfirmHolder;
import net.godlycow.org.homes.gui.HomesMainHolder;
import net.godlycow.org.homes.model.Home;
import net.godlycow.org.homes.HomeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.godlycow.org.commands.config.HomesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public class HomeGUIListener implements Listener {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final HomesConfig config;
    private final HomeManager homeManager;
    private final Map<Player, PendingAction> pendingActions = new WeakHashMap<>();
    private final Map<Player, Integer> awaitingRename = new WeakHashMap<>();
    private final Set<Inventory> trackedInventories = new HashSet<>();
    private final NamespacedKey displayKey;

    private static final Integer[] BED_SLOTS = {10, 12, 14, 16, 28, 30, 32, 34};
    private static final Integer[] DYE_SLOTS = {19, 21, 23, 25, 37, 39, 41, 43};

    private enum Action { SET, REMOVE, RENAME }
    private record PendingAction(Action action, int slot) {}

    public HomeGUIListener(Plugin plugin, PlayerLanguageManager langManager,
                           HomesConfig config, HomeManager homeManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.homeManager = homeManager;
        this.displayKey = new NamespacedKey(plugin, "homes_display_item");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (trackedInventories.contains(event.getInventory())) {
            ItemStack current = event.getCurrentItem();
            if (current != null && isDisplayItem(current)) {
                event.setCancelled(true);
                return;
            }

            if (isBorderSlot(event.getRawSlot(), event.getInventory().getSize())) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getInventory().getHolder() instanceof HomesMainHolder) {
            event.setCancelled(true);
            handleMainGUIClick(player, event.getRawSlot(), event.getClick());
        } else if (event.getInventory().getHolder() instanceof HomesConfirmHolder) {
            event.setCancelled(true);
            handleConfirmGUIClick(player, event.getRawSlot());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!trackedInventories.contains(event.getInventory())) return;

        for (int slot : event.getRawSlots()) {
            if (isBorderSlot(slot, event.getInventory().getSize())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        trackedInventories.remove(event.getInventory());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Integer slot = awaitingRename.get(player);
        if (slot == null) return;

        event.setCancelled(true);
        awaitingRename.remove(player);
        String newName = event.getMessage().trim();

        if (newName.length() > config.maxHomeNameLength) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.too-long",
                    "<red>❌ Name too long! Maximum {max} characters.",
                    ComponentPlaceholder.of("{max}", config.maxHomeNameLength)));
            Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
            return;
        }

        homeManager.getAllHomes(player.getUniqueId()).thenAccept(homes -> {
            if (!config.allowDuplicateHomeNames) {
                boolean duplicate = homes.entrySet().stream()
                        .anyMatch(entry -> entry.getValue() != null &&
                                entry.getValue().getName().equalsIgnoreCase(newName) &&
                                entry.getKey() != slot);

                if (duplicate) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.duplicate",
                            "<red>❌ You already have a home named '{name}'!",
                            ComponentPlaceholder.of("{name}", newName)));
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                    return;
                }
            }
            proceedWithRename(player, slot, newName);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        pendingActions.remove(player);
        awaitingRename.remove(player);
    }

    private void handleMainGUIClick(Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        if (config.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-disabled",
                    "<red>❌ You cannot manage homes in this world!"));
            player.closeInventory();
            return;
        }

        for (int i = 0; i < BED_SLOTS.length; i++) {
            if (slot == BED_SLOTS[i]) {
                handleBedClick(player, i + 1, clickType);
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

    private void handleConfirmGUIClick(Player player, int rawSlot) {
        PendingAction action = pendingActions.get(player);
        if (action == null) {
            player.closeInventory();
            return;
        }

        if (rawSlot == 10) {
            confirmAction(player, action);
        } else if (rawSlot == 16) {
            pendingActions.remove(player);
            openMainGUI(player);
        }
    }

    private void proceedWithRename(Player player, int slot, String newName) {
        homeManager.getHome(player.getUniqueId(), slot).thenAccept(opt -> {
            if (opt.isPresent()) {
                Home home = opt.get();
                home.setName(newName);
                homeManager.setHome(player.getUniqueId(), slot, home).thenAccept(v -> {
                    if (v) {
                        player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.success",
                                "<green>✔ Home renamed to <yellow>{name}</yellow>!",
                                ComponentPlaceholder.of("{name}", newName)));
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                });
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.homes.empty-slot",
                        "<gray>No home set in slot {slot}!",
                        ComponentPlaceholder.of("{slot}", slot)));
                Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
            }
        });
    }

    private void handleBedClick(Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        UUID uuid = player.getUniqueId();

        if (!canSetHome(player, slot)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.no-permission",
                    "<red>❌ You don't have permission to manage home {slot}!",
                    ComponentPlaceholder.of("{slot}", slot)));
            return;
        }

        if (clickType.isLeftClick()) {
            homeManager.getHome(uuid, slot).thenAccept(opt -> {
                if (opt.isEmpty()) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.empty-slot",
                            "<gray>No home set in slot {slot}!",
                            ComponentPlaceholder.of("{slot}", slot)));
                    return;
                }

                Location homeLoc = opt.get().toLocation();
                if (homeLoc == null) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-missing",
                            "<red>❌ Home world is no longer available!"));
                    return;
                }

                if (!homeManager.canTeleport(uuid)) {
                    long remaining = homeManager.getRemainingTeleportCooldown(uuid);
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.teleport-cooldown",
                            "<red>⏱ Please wait {seconds} seconds before teleporting again!",
                            ComponentPlaceholder.of("{seconds}", remaining)));
                    return;
                }

                if (!config.allowCrossWorldTeleport && !player.getWorld().equals(homeLoc.getWorld())) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.cross-world-disabled",
                            "<red>❌ Cross-world teleportation is disabled!"));
                    return;
                }

                if (config.isTeleportRestricted(player.getWorld().getName())) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.teleport-restricted",
                            "<red>❌ You cannot teleport from this world!"));
                    return;
                }

                homeManager.updateLastTeleportTime(uuid);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(homeLoc);
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.teleport",
                            "<green>✔ Teleported to home <yellow>{slot}</yellow>!",
                            ComponentPlaceholder.of("{slot}", slot)));
                });
            });
        } else {
            homeManager.getHome(uuid, slot).thenAccept(opt -> {
                boolean hasHome = opt.isPresent();

                if (!hasHome) {
                    if (!homeManager.canSetHome(uuid)) {
                        long remaining = homeManager.getRemainingSetCooldown(uuid);
                        player.sendMessage(langManager.getMessageFor(player, "commands.homes.set-cooldown",
                                "<red>⏱ Please wait {seconds} seconds before setting another home!",
                                ComponentPlaceholder.of("{seconds}", remaining)));
                        return;
                    }

                    pendingActions.put(player, new PendingAction(Action.SET, slot));
                    Bukkit.getScheduler().runTask(plugin, () -> openConfirmGUI(player, slot, "set"));
                } else if (config.allowRename && player.hasPermission("essc.command.renamehome")) {
                    awaitingRename.put(player, slot);
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.rename.prompt",
                            "<yellow>Enter a new name for this home in chat:"));
                    player.closeInventory();
                }
            });
        }
    }

    private void handleDyeClick(Player player, int slot) {
        plugin.getLogger().fine("Dye clicked by " + player.getName() + " for home slot " + slot);

        if (!player.hasPermission("essc.command.removehome")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.no-permission-remove",
                    "<red>❌ You don't have permission to remove homes!"));
            return;
        }

        homeManager.getHome(player.getUniqueId(), slot).thenAccept(opt -> {
            if (opt.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.homes.empty-slot",
                        "<gray>No home set in slot {slot}!",
                        ComponentPlaceholder.of("{slot}", slot)));
                return;
            }

            pendingActions.put(player, new PendingAction(Action.REMOVE, slot));
            Bukkit.getScheduler().runTask(plugin, () -> openConfirmGUI(player, slot, "remove"));
        });
    }

    private void confirmAction(Player player, PendingAction action) {
        UUID uuid = player.getUniqueId();

        if (config.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-disabled",
                    "<red>❌ You cannot perform this action in this world!"));
            pendingActions.remove(player);
            openMainGUI(player);
            return;
        }

        switch (action.action) {
            case SET -> {
                if (!homeManager.canSetHome(uuid)) {
                    long remaining = homeManager.getRemainingSetCooldown(uuid);
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.set-cooldown",
                            "<red>⏱ Please wait {seconds} seconds before setting another home!",
                            ComponentPlaceholder.of("{seconds}", remaining)));
                    pendingActions.remove(player);
                    openMainGUI(player);
                    return;
                }

                if (config.requireEmptyInventoryToSet && !player.getInventory().isEmpty()) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.homes.require-empty-inventory",
                            "<red>❌ You must have an empty inventory to set a home!"));
                    pendingActions.remove(player);
                    openMainGUI(player);
                    return;
                }

                Home home = new Home("Home " + action.slot, player.getLocation());
                homeManager.setHome(uuid, action.slot, home).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(langManager.getMessageFor(player, "commands.homes.set",
                                "<green>✔ Home <yellow>{slot}</yellow> set!",
                                ComponentPlaceholder.of("{slot}", action.slot)));
                    }
                    pendingActions.remove(player);
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                });
                return;
            }

            case REMOVE -> {
                homeManager.removeHome(uuid, action.slot).thenAccept(success -> {
                    if (success) {
                        player.sendMessage(langManager.getMessageFor(player, "commands.homes.remove",
                                "<green>✔ Home <yellow>{slot}</yellow> removed!",
                                ComponentPlaceholder.of("{slot}", action.slot)));
                    }
                    pendingActions.remove(player);
                    Bukkit.getScheduler().runTask(plugin, () -> openMainGUI(player));
                });
                return;
            }

            case RENAME -> {
                pendingActions.remove(player);
                return;
            }
        }
    }

    public void openMainGUI(Player player) {
        if (config.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-disabled",
                    "<red>❌ You cannot access homes in this world!"));
            return;
        }

        Component title = langManager.getMessageFor(null, "commands.homes.gui.title", "<green>Home Manager");
        Inventory inv = Bukkit.createInventory(new HomesMainHolder(), 54, title);
        trackedInventories.add(inv);

        fillBackground(inv);
        setupHomeSlots(player, inv).thenAccept(v ->
                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv))
        );
    }

    public void openConfirmGUI(Player player, int slot, String mode) {
        Component title = langManager.getMessageFor(null, "commands.homes.gui.confirm-title", "<red>Confirm Action");
        Inventory inv = Bukkit.createInventory(new HomesConfirmHolder(slot, mode), 27, title);
        trackedInventories.add(inv);

        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(langManager.getMessageFor(player, "commands.homes.gui.confirm", "<green>✔ Confirm"));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(10, confirm);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(langManager.getMessageFor(player, "commands.homes.gui.cancel", "<red>❌ Cancel"));
        cancel.setItemMeta(cancelMeta);
        inv.setItem(16, cancel);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(langManager.getMessageFor(player, "commands.homes.gui.info",
                "<white>Home {slot} - {mode}",
                ComponentPlaceholder.of("{slot}", slot),
                ComponentPlaceholder.of("{mode}", mode)));
        info.setItemMeta(applyDisplayKey(infoMeta));
        inv.setItem(13, info);

        fillConfirmBackground(inv);
        player.openInventory(inv);
    }

    private ItemMeta applyDisplayKey(ItemMeta meta) {
        meta.getPersistentDataContainer().set(displayKey, PersistentDataType.BYTE, (byte) 1);
        return meta;
    }

    private CompletableFuture<Void> setupHomeSlots(Player player, Inventory inv) {
        UUID uuid = player.getUniqueId();
        return homeManager.getAllHomes(uuid).thenAccept(homes -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (int i = 0; i < Math.min(8, config.maxHomes); i++) {
                    int slot = i + 1;
                    Home home = homes.get(slot);
                    inv.setItem(BED_SLOTS[i], createBedItem(player, slot, home, home != null));
                    inv.setItem(DYE_SLOTS[i], createDyeItem(player, slot, home != null));
                }
            });
        });
    }

    private ItemStack createBedItem(Player player, int slot, Home home, boolean hasHome) {
        Material material = !canSetHome(player, slot) ? Material.RED_BED :
                !hasHome ? Material.GRAY_BED : Material.BLUE_BED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(langManager.getMessageFor(player, "commands.homes.gui.home-title",
                "<white>Home {slot}",
                ComponentPlaceholder.of("{slot}", slot)));

        List<Component> lore = new ArrayList<>();
        if (!canSetHome(player, slot)) {
            lore.add(langManager.getMessageFor(player, "commands.homes.gui.no-permission", "<red>❌ No Permission!"));
        } else if (!hasHome) {
            lore.add(langManager.getMessageFor(player, "commands.homes.gui.click-set", "<yellow>▶ Click to set"));
        } else {
            lore.add(langManager.getMessageFor(player, "commands.homes.gui.name",
                    "<gray>Name: <white>{home}",
                    ComponentPlaceholder.of("{home}", home.getName())));

            lore.add(langManager.getMessageFor(player, "commands.homes.gui.coords",
                    "<gray>XYZ: <white>{x}, {y}, {z}",
                    ComponentPlaceholder.of("{x}", Math.round(home.getX())),
                    ComponentPlaceholder.of("{y}", Math.round(home.getY())),
                    ComponentPlaceholder.of("{z}", Math.round(home.getZ()))));

            lore.add(langManager.getMessageFor(player, "commands.homes.gui.world",
                    "<gray>World: <white>{world}",
                    ComponentPlaceholder.of("{world}", simplifyWorldName(home.toLocation().getWorld().getName()))));

            lore.add(Component.empty());
            lore.add(langManager.getMessageFor(player, "commands.homes.gui.click-teleport", "<green>▶ Left-click to teleport"));
            lore.add(langManager.getMessageFor(player, "commands.homes.gui.click-manage", "<yellow>▶ Right-click to manage"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDyeItem(Player player, int slot, boolean hasHome) {
        if (!player.hasPermission("essc.command.removehome")) {
            return createFiller();
        }

        Material material = !hasHome ? Material.GRAY_DYE : Material.LIME_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(langManager.getMessageFor(player, "homes.gui.manage-title",
                "<white>Manage Home {slot}",
                ComponentPlaceholder.of("{slot}", slot)));

        List<Component> lore = new ArrayList<>();
        if (!hasHome) {
            lore.add(langManager.getMessageFor(player, "homes.gui.empty", "<gray>Empty slot"));
        } else {
            lore.add(langManager.getMessageFor(player, "homes.gui.click-remove", "<red>▶ Click to remove"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }


    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(applyDisplayKey(meta));
        return filler;
    }

    private void fillBackground(Inventory inv) {
        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private void fillConfirmBackground(Inventory inv) {
        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private boolean isBorderSlot(int slot, int inventorySize) {
        return slot < 9 || slot >= inventorySize - 9 || slot % 9 == 0 || slot % 9 == 8;
    }

    private boolean isDisplayItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(displayKey, PersistentDataType.BYTE);
    }

    private boolean canSetHome(Player player, int slot) {
        if (!player.hasPermission("essc.command.sethome")) return false;
        if (player.hasPermission("essc.command.sethome.*")) return true;
        return player.hasPermission("essc.command.sethome." + slot);
    }

    private String simplifyWorldName(String worldName) {
        return worldName.replace("_", " ").replace("-", " ");
    }
}