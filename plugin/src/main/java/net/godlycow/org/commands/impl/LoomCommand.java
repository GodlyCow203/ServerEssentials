package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.LoomConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class LoomCommand implements CommandExecutor, Listener {
    private static final String PERMISSION = "serveressentials.command.loom";
    private static final int GUI_SIZE = 27;
    private static final int CENTER_SLOT = 13;

    private final PlayerLanguageManager langManager;
    private final LoomConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public LoomCommand(PlayerLanguageManager langManager, LoomConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;

        if (plugin.getConfig().getBoolean("loom.enabled", true)) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.loom.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.loom.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Component titleComponent = langManager.getMessageFor(player, "commands.loom.gui-title",
                "<red><bold>Loom Interface");
        String guiName = plainSerializer.serialize(titleComponent);
        Inventory loomGui = Bukkit.createInventory(player, GUI_SIZE, guiName);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.displayName(langManager.getMessageFor(player, "commands.loom.error-item-name",
                    "<red><bold>ERROR"));

            List<Component> lore = new ArrayList<>();
            lore.add(langManager.getMessageFor(player, "commands.loom.error-item-lore",
                    "<gray>Feature not yet implemented"));
            meta.lore(lore);

            barrier.setItemMeta(meta);
        }
        loomGui.setItem(CENTER_SLOT, barrier);

        player.openInventory(loomGui);
        player.playSound(player.getLocation(), Sound.UI_LOOM_SELECT_PATTERN, 1.0f, 1.0f);

        player.sendMessage(langManager.getMessageFor(player, "commands.loom.opened",
                "<green>Loom interface opened!"));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "loom", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "loom", "usage_count", String.valueOf(count + 1));
        });

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.BARRIER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = plainSerializer.serialize(meta.displayName());
        String expectedName = plainSerializer.serialize(langManager.getMessageFor(
                event.getWhoClicked() instanceof Player ? (Player)event.getWhoClicked() : null,
                "commands.loom.error-item-name",
                "<red><bold>ERROR"
        ));

        if (!itemName.equals(expectedName)) return;

        event.setCancelled(true);

        if (event.getWhoClicked() instanceof Player player) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(langManager.getMessageFor(player, "commands.loom.clicked-error",
                    "<red>You clicked the error item! Feature not implemented yet."));
        }
    }
}