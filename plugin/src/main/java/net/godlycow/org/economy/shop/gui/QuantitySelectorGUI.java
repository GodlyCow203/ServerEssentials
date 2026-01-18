package net.godlycow.org.economy.shop.gui;

import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.eco.EconomyResponse;
import net.godlycow.org.economy.shop.config.ShopSectionConfig;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuantitySelectorGUI {
    private static final MiniMessage mini = MiniMessage.miniMessage();
    private static final int INVENTORY_SIZE = 27;

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final EconomyManager economyManager;
    private final ShopGUIManager shopGUIManager;

    private final Map<UUID, State> playerState = new ConcurrentHashMap<>();

    public static class State {
        public final String sectionFile;
        public final int page;
        public final ShopSectionConfig.ShopItem shopItem;
        public int quantity;
        public boolean awaitingCustomAmount = false;

        State(String sectionFile, int page, ShopSectionConfig.ShopItem shopItem) {
            this.sectionFile = sectionFile;
            this.page = page;
            this.shopItem = shopItem;
            this.quantity = 0;
        }
    }

    public QuantitySelectorGUI(Plugin plugin, PlayerLanguageManager langManager,
                               EconomyManager economyManager, ShopGUIManager shopGUIManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.economyManager = economyManager;
        this.shopGUIManager = shopGUIManager;
    }

    public void open(Player player, ShopSectionConfig.ShopItem shopItem, String sectionFile, int page) {
        State state = new State(sectionFile, page, shopItem);
        playerState.put(player.getUniqueId(), state);

        Inventory inv = createInventory(player, shopItem, 0);
        player.openInventory(inv);
    }

    public boolean isGUIOpen(UUID playerUuid) {
        return playerState.containsKey(playerUuid);
    }

    public boolean isAwaitingCustomAmount(UUID playerUuid) {
        State state = playerState.get(playerUuid);
        return state != null && state.awaitingCustomAmount;
    }

    public void cleanupGUI(UUID playerUuid) {
        playerState.remove(playerUuid);
    }

    public void cleanupChat(UUID playerUuid) {
        State state = playerState.remove(playerUuid);
        if (state != null) {
            state.awaitingCustomAmount = false;
        }
    }

    public void handleChatInput(Player player, String message) {
        State state = playerState.get(player.getUniqueId());
        if (state == null || !state.awaitingCustomAmount) return;

        state.awaitingCustomAmount = false;

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.cancelled",
                    "<red>✗ Purchase cancelled"));

            cleanupChat(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () ->
                    shopGUIManager.openSectionGUI(player, state.sectionFile, state.page));
            return;
        }

        try {
            int customAmount = Integer.parseInt(message);

            if (customAmount <= 0) {
                player.sendMessage(langManager.getMessageFor(player,
                        "commands.economy.shop.negative-amount",
                        "<red>✗ Amount must be greater than 0"));
            } else {
                state.quantity = customAmount;
            }

        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.invalid-amount",
                    "<red>✗ Invalid amount. Please enter a number."));
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = createInventory(player, state.shopItem, state.quantity);
            player.openInventory(inv);
        });
    }


    private Inventory createInventory(Player player, ShopSectionConfig.ShopItem shopItem, int quantity) {
        Component title = langManager.getMessageFor(player, "commands.economy.shop.quantity-title",
                "<gold>Select Quantity: <gray>{item}",
                LanguageManager.ComponentPlaceholder.of("{item}", shopItem.name));

        Inventory inv = Bukkit.createInventory(
                new QuantityInventoryHolder(),
                INVENTORY_SIZE,
                title
        );

        inv.setItem(4, createPlayerHead(player));
        inv.setItem(13, createItemDisplay(shopItem, quantity));
        inv.setItem(9, createQuantityButton(Material.RED_CONCRETE, "<red>-64", -64));
        inv.setItem(10, createQuantityButton(Material.RED_CONCRETE, "<red>-10", -10));
        inv.setItem(11, createQuantityButton(Material.RED_CONCRETE, "<red>-1", -1));
        inv.setItem(15, createQuantityButton(Material.LIME_CONCRETE, "<green>+1", 1));
        inv.setItem(16, createQuantityButton(Material.LIME_CONCRETE, "<green>+10", 10));
        inv.setItem(17, createQuantityButton(Material.LIME_CONCRETE, "<green>+64", 64));
        inv.setItem(18, createControlButton(Material.REDSTONE_BLOCK, "<red>✗ Cancel"));
        inv.setItem(22, createControlButton(Material.SEA_LANTERN, "<yellow>🔢 Custom Amount"));
        inv.setItem(26, createControlButton(Material.EMERALD_BLOCK, "<green>✓ Confirm"));

        return inv;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);

            String balanceText = economyManager.isEnabled() ?
                    economyManager.format(economyManager.getBalance(player)) : "N/A";

            meta.displayName(langManager.getMessageFor(player, "commands.economy.shop.balance-display",
                    "<green>Your Balance: <gold>{balance}",
                    LanguageManager.ComponentPlaceholder.of("{balance}", balanceText)));

            List<Component> lore = new ArrayList<>();
            State state = playerState.get(player.getUniqueId());
            if (state != null) {
                double totalPrice = state.shopItem.buyPrice * state.quantity;
                lore.add(mini.deserialize("<gray>Total cost: <gold>" + economyManager.format(totalPrice)));
            }

            if (!economyManager.isEnabled()) {
                lore.add(mini.deserialize("<red>✗ Economy disabled"));
            }

            meta.lore(lore);
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private ItemStack createItemDisplay(ShopSectionConfig.ShopItem shopItem, int quantity) {
        ItemStack item = new ItemStack(shopItem.material, quantity > 0 ? quantity : 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mini.deserialize(shopItem.name));

            List<Component> lore = new ArrayList<>();
            if (shopItem.lore != null) {
                shopItem.lore.forEach(line -> lore.add(mini.deserialize(line)));
            }

            lore.add(mini.deserialize("<dark_gray>-------------------"));
            lore.add(mini.deserialize("<gray>Quantity: <yellow>" + quantity));

            double totalPrice = shopItem.buyPrice * quantity;
            lore.add(mini.deserialize("<gray>Total: <gold>" + economyManager.format(totalPrice)));

            if (shopItem.buyPrice > 0) {
                lore.add(mini.deserialize("<gray>Unit price: <gold>" + economyManager.format(shopItem.buyPrice)));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createQuantityButton(Material material, String label, int change) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mini.deserialize(label));
            List<Component> lore = new ArrayList<>();
            lore.add(mini.deserialize("<gray>Click to adjust"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createControlButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mini.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(Player player, int slot) {
        State state = playerState.get(player.getUniqueId());
        if (state == null) {
            plugin.getLogger().warning("QuantitySelectorGUI: No state found for " + player.getName());
            return;
        }

        plugin.getLogger().info("QuantitySelectorGUI: Player " + player.getName() + " clicked slot " + slot);

        if (slot == 9) adjustQuantity(player, -64);
        else if (slot == 10) adjustQuantity(player, -10);
        else if (slot == 11) adjustQuantity(player, -1);
        else if (slot == 15) adjustQuantity(player, 1);
        else if (slot == 16) adjustQuantity(player, 10);
        else if (slot == 17) adjustQuantity(player, 64);
        else if (slot == 18) cancel(player);
        else if (slot == 22) promptCustomAmount(player);
        else if (slot == 26) confirm(player);
        else {
            plugin.getLogger().warning("QuantitySelectorGUI: Unexpected slot " + slot + " clicked by " + player.getName());
        }
    }

    private void adjustQuantity(Player player, int change) {
        State state = playerState.get(player.getUniqueId());
        if (state == null) return;

        int newQuantity = Math.max(0, state.quantity + change);
        state.quantity = newQuantity;

        Inventory inv = player.getOpenInventory().getTopInventory();
        inv.setItem(13, createItemDisplay(state.shopItem, newQuantity));
        inv.setItem(4, createPlayerHead(player));
    }

    private void promptCustomAmount(Player player) {
        State state = playerState.get(player.getUniqueId());
        if (state == null) return;

        state.awaitingCustomAmount = true;

        player.closeInventory();

        player.sendMessage(langManager.getMessageFor(player,
                "commands.economy.shop.custom-amount-prompt",
                "<yellow>Please type the amount you want to buy in chat, or type <red>cancel<yellow> to exit."));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            State st = playerState.get(player.getUniqueId());
            if (st != null && st.awaitingCustomAmount) {
                player.sendMessage(langManager.getMessageFor(player,
                        "commands.economy.shop.custom-amount-timeout",
                        "<red>✗ Custom amount input timed out."));
                cleanupChat(player.getUniqueId());
            }
        }, 20 * 60);
    }


    private void cancel(Player player) {
        State state = playerState.get(player.getUniqueId());
        if (state == null) return;

        cleanupGUI(player.getUniqueId());
        player.sendMessage(langManager.getMessageFor(player,
                "commands.economy.shop.cancelled",
                "<red>✗ Purchase cancelled"));

        Bukkit.getScheduler().runTask(plugin, () ->
                shopGUIManager.openSectionGUI(player, state.sectionFile, state.page));
    }

    private void confirm(Player player) {
        State state = playerState.get(player.getUniqueId());
        if (state == null) return;

        if (state.quantity <= 0) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.zero-quantity",
                    "<red>✗ Please select a quantity greater than 0"));
            return;
        }

        ShopSectionConfig.ShopItem item = state.shopItem;
        double totalPrice = item.buyPrice * state.quantity;
        double balance = economyManager.getBalance(player);

        if (balance < totalPrice) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.cannot-afford-bulk",
                    "<red>✗ You cannot afford {quantity}x {item} (cost: {price})",
                    LanguageManager.ComponentPlaceholder.of("{quantity}", state.quantity),
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", economyManager.format(totalPrice))));
            cleanupGUI(player.getUniqueId());

            Bukkit.getScheduler().runTask(plugin, () ->
                    shopGUIManager.openSectionGUI(player, state.sectionFile, state.page)
            );
            return;
        }

        int totalItems = state.quantity * item.amount;
        if (player.getInventory().firstEmpty() == -1 && !player.getInventory().contains(item.material)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.no-inventory-space",
                    "<red>✗ You don't have enough inventory space for {quantity}x {item}",
                    LanguageManager.ComponentPlaceholder.of("{quantity}", state.quantity),
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name)));
            return;
        }

        EconomyResponse response = economyManager.withdraw(player, totalPrice);

        if (response.success()) {
            ItemStack giveItem = new ItemStack(item.material, totalItems);
            player.getInventory().addItem(giveItem);

            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.bulk-buy-success",
                    "<green>✓ You bought {quantity}x {item} for {price}",
                    LanguageManager.ComponentPlaceholder.of("{quantity}", state.quantity),
                    LanguageManager.ComponentPlaceholder.of("{item}", item.name),
                    LanguageManager.ComponentPlaceholder.of("{price}", economyManager.format(totalPrice))));

            Bukkit.getScheduler().runTask(plugin, () -> {
                cleanupGUI(player.getUniqueId());
                shopGUIManager.openSectionGUI(player, state.sectionFile, state.page);
            });
        } else {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands.economy.shop.transaction-failed",
                    "<red>✗ Transaction failed: {error}",
                    LanguageManager.ComponentPlaceholder.of("{error}", response.errorMessage)));
        }
    }


}