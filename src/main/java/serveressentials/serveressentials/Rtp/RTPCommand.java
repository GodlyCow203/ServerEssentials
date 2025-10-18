package serveressentials.serveressentials.Rtp;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.config.RTPConfig;
import serveressentials.serveressentials.util.RTPMessages;

public class RTPCommand implements CommandExecutor {

    private final ServerEssentials plugin;

    public RTPCommand(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /rtp reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rtp.reload")) {
                RTPMessages msg = new RTPMessages(plugin);
                sender.sendMessage(msg.get("no-permission"));
                return true;
            }

            RTPConfig.fullReload();
            RTPMessages.fullReload();
            RTPMessages msg = RTPMessages.getInstance();
            sender.sendMessage(msg.get("reload-success"));
            return true;
        }

        // Must be player
        if (!(sender instanceof Player player)) {
            RTPMessages msg = new RTPMessages(plugin);
            sender.sendMessage(msg.get("only-player"));
            return true;
        }

        // Create GUI dynamically with latest messages
        RTPMessages messages = new RTPMessages(plugin);
        Component title = messages.get("gui.title");
        Inventory gui = Bukkit.createInventory(null, 9, title);

        ItemStack overworld = createGuiItem(Material.GRASS_BLOCK,
                messages.get("gui.overworld.name"),
                messages.getList("gui.overworld.lore"));
        ItemStack nether = createGuiItem(Material.NETHERRACK,
                messages.get("gui.nether.name"),
                messages.getList("gui.nether.lore"));
        ItemStack theEnd = createGuiItem(Material.END_STONE,
                messages.get("gui.end.name"),
                messages.getList("gui.end.lore"));

        gui.setItem(2, overworld);
        gui.setItem(4, nether);
        gui.setItem(6, theEnd);

        player.openInventory(gui);
        return true;
    }

    private ItemStack createGuiItem(Material material, Component name, java.util.List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
