package serveressentials.serveressentials.pw;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.gui.CategoryGUI;
import serveressentials.serveressentials.gui.EditWarpGUI;
import serveressentials.serveressentials.gui.WarpListGUI;
import serveressentials.serveressentials.util.ChatUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class GUIListeners implements Listener {

    private final WarpStorage storage;
    private final Plugin plugin;
    private final GUIConfig guiConfig;
    private final Map<UUID, Consumer<String>> chatInputs = new HashMap<>();
    private final Map<UUID, PlayerWarp> awaitingCategorySelection = new HashMap<>();

    public GUIListeners(WarpStorage storage, Plugin plugin, GUIConfig guiConfig) {
        this.storage = storage;
        this.plugin = plugin;
        this.guiConfig = guiConfig;
    }

    public void waitForChatInput(Player player, Consumer<String> handler) {
        chatInputs.put(player.getUniqueId(), handler);
    }

    @EventHandler
    public void onWarpClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        String warpsPrefix = guiConfig.getWarpsGUITitlePrefix();

        if (!title.startsWith(warpsPrefix)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String category = title.substring(warpsPrefix.length());

        PlayerWarp warp = storage.getWarpsInCategory(category).stream()
                .filter(w -> ChatColor.stripColor(w.getName()).equalsIgnoreCase(name))
                .findFirst().orElse(null);

        if (warp == null) {
            player.sendMessage(ChatUtil.color(guiConfig.getMessage("warp-not-found", "§cWarp not found.")));
            return;
        }

        // TODO: Add cooldown check if needed
        player.teleport(warp.getLocation());
        player.sendMessage(ChatUtil.color(guiConfig.getMessage("teleport-success", "§aTeleported to &f" + warp.getName())));
    }

    @EventHandler
    public void onEditClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String titlePrefix = guiConfig.getEditWarpGUITitlePrefix(); // e.g. "§eEdit Warp: "
        if (!e.getView().getTitle().startsWith(titlePrefix)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        PlayerWarp warp = storage.getEditingWarp(player);
        if (warp == null) {
            player.sendMessage(ChatUtil.color(guiConfig.getMessage("warp-editing-none", "§cCould not find warp being edited.")));
            return;
        }

        int slot = e.getSlot();

        switch (slot) {
            case 10 -> { // Change Name
                player.closeInventory();
                player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-type-name", "§eType the new warp name in chat:")));
                waitForChatInput(player, input -> {
                    if (input.isBlank()) {
                        player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-name-empty", "§cName cannot be empty.")));
                        return;
                    }
                    warp.setName(input);
                    player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-name-updated", "§aWarp name updated.")));
                    new EditWarpGUI(storage, guiConfig).open(player, warp);
                });
            }
            case 11 -> { // Change Description
                player.closeInventory();
                player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-type-description", "§eType the new description in chat:")));
                waitForChatInput(player, input -> {
                    warp.setDescription(input);
                    player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-description-updated", "§aWarp description updated.")));
                    new EditWarpGUI(storage, guiConfig).open(player, warp);
                });
            }
            case 12 -> { // Change Category
                awaitingCategorySelection.put(player.getUniqueId(), warp);
                new CategoryGUI(storage, guiConfig).open(player);
            }
            case 13 -> { // Change Icon
                player.closeInventory();
                player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-icon-instruction", "§eHold the item you want as icon and type 'icon' in chat.")));
                waitForChatInput(player, input -> {
                    if (!input.equalsIgnoreCase("icon")) {
                        player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-icon-invalid", "§cInvalid input, type 'icon' to set icon.")));
                        return;
                    }
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-icon-no-item", "§cHold an item in your hand.")));
                        return;
                    }
                    warp.setIcon(hand.getType());
                    player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-icon-updated", "§aWarp icon updated.")));
                    new EditWarpGUI(storage, guiConfig).open(player, warp);
                });
            }
            case 14 -> { // Change Location
                warp.setLocation(player.getLocation());
                player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-location-updated", "§aWarp location updated.")));
                new EditWarpGUI(storage, guiConfig).open(player, warp);
            }
            case 15 -> { // Change Cooldown
                player.closeInventory();
                player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-type-cooldown", "§eType the new cooldown time in seconds:")));
                waitForChatInput(player, input -> {
                    try {
                        int cd = Integer.parseInt(input);
                        if (cd < 0) throw new NumberFormatException();
                        warp.setCooldownSeconds(cd);
                        player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-cooldown-updated", "§aWarp cooldown updated.")));
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatUtil.color(guiConfig.getMessage("edit-warp-cooldown-invalid", "§cInvalid number.")));
                    }
                    new EditWarpGUI(storage, guiConfig).open(player, warp);
                });
            }
        }
    }

    @EventHandler
    public void onCategoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String categoryTitle = guiConfig.getCategoryGUITitle();

        if (!e.getView().getTitle().equals(categoryTitle)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String category = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (awaitingCategorySelection.containsKey(player.getUniqueId())) {
            PlayerWarp warp = awaitingCategorySelection.remove(player.getUniqueId());
            warp.setCategory(category);
            player.sendMessage(ChatUtil.color(guiConfig.getMessage("category-set-success", "§aWarp category set to " + category)));
            new EditWarpGUI(storage, guiConfig).open(player, warp);
        } else {
            // Just viewing warps in category
            new WarpListGUI(storage, guiConfig).open(player, category);
        }
    }

    @EventHandler
    public void onChatInput(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (chatInputs.containsKey(uuid)) {
            e.setCancelled(true);
            String message = e.getMessage();
            Consumer<String> handler = chatInputs.remove(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> handler.accept(message));
        }
    }
}