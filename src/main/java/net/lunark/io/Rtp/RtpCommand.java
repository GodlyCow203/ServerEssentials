package net.lunark.io.Rtp;

import net.kyori.adventure.text.Component;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class RtpCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RtpConfig rtpConfig;

    public RtpCommand(Plugin plugin, PlayerLanguageManager langManager, RtpConfig rtpConfig) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.rtpConfig = rtpConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only_player",
                    "Only players can use this command!").toString());
            return true;
        }

        if (!player.hasPermission("serveressentials.command.rtp")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no_permission",
                    "You don't have permission!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "rtp.use")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("serveressentials.command.rtp.reload")) {
                player.sendMessage(langManager.getMessageFor(player, "commands.no_permission",
                        "You don't have permission!",
                        LanguageManager.ComponentPlaceholder.of("{permission}", "rtp.reload")));
                return true;
            }

            rtpConfig.reload();
            player.sendMessage(langManager.getMessageFor(player, "rtp.reloaded",
                    "RTP configuration reloaded!"));
            return true;
        }

        openRtpGui(player);
        return true;
    }

    private void openRtpGui(Player player) {
        Component title = langManager.getMessageFor(player, "rtp.gui.title", "<green>RTP Menu");
        Inventory gui = Bukkit.createInventory(null, 9, title);

        gui.setItem(2, createGuiItem(Material.GRASS_BLOCK,
                langManager.getMessageFor(player, "rtp.gui.overworld.name", "Overworld"),
                langManager.getMessageList(player, "rtp.gui.overworld.lore")));


        gui.setItem(4, createGuiItem(Material.NETHERRACK,
                langManager.getMessageFor(player, "rtp.gui.nether.name", "Nether"),
                langManager.getMessageList(player, "rtp.gui.nether.lore")));

        gui.setItem(6, createGuiItem(Material.END_STONE,
                langManager.getMessageFor(player, "rtp.gui.end.name", "The End"),
                langManager.getMessageList(player, "rtp.gui.end.lore")));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}