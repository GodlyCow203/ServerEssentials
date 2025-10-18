package serveressentials.serveressentials.interaction_blocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.util.List;

public class FurnaceCommand implements CommandExecutor, Listener {

    private final ServerEssentials plugin;
    private YamlConfiguration messages;
    private File messageFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public FurnaceCommand(ServerEssentials plugin) {
        this.plugin = plugin;
        loadMessages();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Load messages from messages/interaction_blocks.yml */
    private void loadMessages() {
        messageFile = new File(plugin.getDataFolder(), "messages/interaction_blocks.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages/interaction_blocks.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messageFile);
    }

    /** Get a message as a MiniMessage Component */
    private Component getMessage(String path) {
        String msg = messages.getString(path, "<red>Missing message: " + path);
        return miniMessage.deserialize(msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plainSerializer.serialize(getMessage("only-players")));
            return true;
        }

        // Create 3x9 inventory (like a furnace GUI)
        String guiName = plainSerializer.serialize(getMessage("gui-title-furnace"));
        Inventory furnaceGui = Bukkit.createInventory(player, 3 * 9, guiName);

        // Add a placeholder barrier in the center
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<red><bold>ERROR"));
            meta.lore(List.of(getMessage("not-implemented-lore")));
            barrier.setItemMeta(meta);
        }
        furnaceGui.setItem(13, barrier); // center of 3x9

        // Open inventory
        player.openInventory(furnaceGui);

        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1f, 1f);

        // Notify player
        player.sendMessage(getMessage("opened-furnace"));

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        if (item.getType() == Material.BARRIER) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() &&
                    meta.displayName().equals(miniMessage.deserialize("<red><bold>ERROR"))) {

                event.setCancelled(true);

                if (event.getWhoClicked() instanceof Player player) {
                    // Play sound + notify on barrier click
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    player.sendMessage(getMessage("clicked-barrier"));
                }
            }
        }
    }
}
