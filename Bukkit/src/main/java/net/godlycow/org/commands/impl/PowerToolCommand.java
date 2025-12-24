package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.PowerToolConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class PowerToolCommand implements CommandExecutor, Listener {
    private static final String PERMISSION = "serveressentials.command.powertool";
    private static final String PERMISSION_CREATE = "serveressentials.command.powertool.create";

    private final PlayerLanguageManager langManager;
    private final PowerToolConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;
    private final NamespacedKey commandKey;
    private final NamespacedKey usesKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PowerToolCommand(PlayerLanguageManager langManager, PowerToolConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
        this.commandKey = new NamespacedKey(plugin, "powertool_command");
        this.usesKey = new NamespacedKey(plugin, "powertool_uses");

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.powertool.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.powertool.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (!player.hasPermission(PERMISSION_CREATE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.powertool.no-permission-create",
                    "<red>You need permission <yellow>{permission}</yellow> to create PowerTools!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_CREATE)));
            return true;
        }

        String fullInput = String.join(" ", args);
        String[] parts = fullInput.split("\\|");

        if (parts.length < 5) {
            player.sendMessage(langManager.getMessageFor(player, "commands.powertool.usage",
                    "<red>Usage: <white>/powertool <command> | <uses> | <name> | <tool> | enchanted <yes/no>"));
            return true;
        }

        String boundCommand = parts[0].trim();

        int uses;
        try {
            uses = Integer.parseInt(parts[1].trim());
            if (uses < -1 || uses > config.maxUses()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(langManager.getMessageFor(player, "commands.powertool.invalid-uses",
                    "<red>Uses must be -1 (infinite) or 1-{max}.",
                    ComponentPlaceholder.of("{max}", config.maxUses())));
            return true;
        }

        String displayName = parts[2].trim();

        String toolName = parts[3].trim().toUpperCase();
        Material toolMaterial = Material.matchMaterial(toolName);
        if (toolMaterial == null || !config.isAllowedMaterial(toolMaterial)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.powertool.invalid-tool",
                    "<red>Invalid tool material: <white>{tool}",
                    ComponentPlaceholder.of("{tool}", toolName)));
            return true;
        }

        boolean enchanted = parts[4].trim().equalsIgnoreCase("enchanted yes");

        ItemStack tool = createPowerTool(toolMaterial, displayName, boundCommand, uses, enchanted);
        player.getInventory().addItem(tool);

        player.sendMessage(langManager.getMessageFor(player, "commands.powertool.received-tool",
                "<green>✓ You received PowerTool: <white>{name}",
                ComponentPlaceholder.of("{name}", displayName)));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "powertool", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "powertool", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "powertool", "last_command", boundCommand);
        });

        return true;
    }

    @EventHandler
    public void onPowerToolUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        ItemStack tool = event.getItem();
        ItemMeta meta = tool.getItemMeta();
        if (meta == null) return;

        String command = meta.getPersistentDataContainer().get(commandKey, PersistentDataType.STRING);
        Integer uses = meta.getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);

        if (command == null || uses == null) return;

        Player player = event.getPlayer();
        player.performCommand(command);

        if (uses > 0) {
            uses -= 1;
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);

            List<Component> lore = meta.lore();
            if (lore != null && lore.size() > 1) {
                lore.set(1, miniMessage.deserialize(uses == 0 ? "<gray>Uses: <white>0" : "<gray>Uses: <white>" + uses));
                meta.lore(lore);
            }

            tool.setItemMeta(meta);

            if (uses == 0) {
                player.sendMessage(langManager.getMessageFor(player, "commands.powertool.out-of-uses",
                        "<red>⚠ Your PowerTool has run out of uses!"));
                player.getInventory().removeItem(tool);
            }
        }
    }

    private ItemStack createPowerTool(Material material, String displayName, String boundCommand, int uses, boolean enchanted) {
        ItemStack tool = new ItemStack(material, 1);
        ItemMeta meta = tool.getItemMeta();

        if (meta != null) {
            meta.displayName(miniMessage.deserialize(displayName));

            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Command: <yellow>/" + boundCommand));
            lore.add(miniMessage.deserialize(uses == -1 ? "<gray>Uses: <green>∞" : "<gray>Uses: <green>" + uses + "</green>"));
            lore.add(miniMessage.deserialize("<gray>(Right-click to use)"));
            meta.lore(lore);

            if (enchanted) {
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            meta.getPersistentDataContainer().set(commandKey, PersistentDataType.STRING, boundCommand);
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);

            tool.setItemMeta(meta);
        }

        return tool;
    }


    public void unregister() {
        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}