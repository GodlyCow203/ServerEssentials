package net.lunark.io.Fun;

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
import org.jetbrains.annotations.NotNull;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.FunMessages;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class PowerToolCommand implements CommandExecutor, Listener {

    private final NamespacedKey commandKey;
    private final NamespacedKey usesKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final FunMessages funMessages;

    public PowerToolCommand(ServerEssentials plugin, FunMessages funMessages) {
        this.commandKey = new NamespacedKey(plugin, "powertool_command");
        this.usesKey = new NamespacedKey(plugin, "powertool_uses");
        this.funMessages = funMessages;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        funMessages.addDefault("powertool.only-players", "<red>Only players can use this command!");
        funMessages.addDefault("powertool.usage", "<red>Usage: /{label} <command> | <uses> | <name> | <tool> | enchanted <yes/no>");
        funMessages.addDefault("powertool.invalid-uses", "<red>Uses must be a number or -1 for infinite.");
        funMessages.addDefault("powertool.invalid-tool", "<red>Invalid tool material: <white>{tool}");
        funMessages.addDefault("powertool.received-tool", "<green>You received a PowerTool: <white>{name}");
        funMessages.addDefault("powertool.tool-out-of-uses", "<red>Your PowerTool has run out of uses!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(funMessages.get("powertool.only-players"));
            return true;
        }

        String fullInput = String.join(" ", args);
        String[] parts = fullInput.split("\\|");

        if (parts.length < 5) {
            player.sendMessage(parseMiniMessage("powertool.usage", "{label}", label));
            return true;
        }

        String boundCommand = parts[0].trim();

        int uses;
        try {
            uses = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            player.sendMessage(funMessages.get("powertool.invalid-uses"));
            return true;
        }

        String displayName = parts[2].trim();

        String toolName = parts[3].trim().toUpperCase();
        Material toolMaterial = Material.matchMaterial(toolName);
        if (toolMaterial == null) {
            player.sendMessage(parseMiniMessage("powertool.invalid-tool", "{tool}", toolName));
            return true;
        }

        boolean enchanted = parts[4].trim().equalsIgnoreCase("enchanted yes");

        ItemStack tool = new ItemStack(toolMaterial, 1);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(displayName));

            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Bound Command: <yellow>/" + boundCommand));
            lore.add(miniMessage.deserialize(uses == -1 ? "<gray>Uses: <green>∞" : "<gray>Uses: <green>" + uses));
            meta.lore(lore);

            if (enchanted) {
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            meta.getPersistentDataContainer().set(commandKey, PersistentDataType.STRING, boundCommand);
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);

            tool.setItemMeta(meta);
        }

        player.getInventory().addItem(tool);
        player.sendMessage(parseMiniMessage("powertool.received-tool", "{name}", displayName));
        return true;
    }

    @EventHandler
    public void onStickUse(PlayerInteractEvent event) {
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
                player.sendMessage(funMessages.get("powertool.tool-out-of-uses"));
                player.getInventory().removeItem(tool);
            }
        }
    }

    private Component parseMiniMessage(String path, String... placeholders) {
        return funMessages.get(path, placeholders);
    }
}
