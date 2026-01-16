package net.godlycow.org.commands.impl;

import net.godlycow.org.rtp.RtpConfig;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;  // <-- THIS WAS MISSING
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RtpCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final RtpConfig rtpConfig;
    private final ConcurrentHashMap<String, World> worldCache = new ConcurrentHashMap<>();

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

        if (!player.hasPermission("essc.command.rtp")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no_permission",
                    "You don't have permission!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "rtp.use")));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("essc.command.rtp.reload")) {
                player.sendMessage(langManager.getMessageFor(player, "commands.no_permission",
                        "You don't have permission!",
                        LanguageManager.ComponentPlaceholder.of("{permission}", "rtp.reload")));
                return true;
            }

            rtpConfig.reload();
            worldCache.clear();
            player.sendMessage(langManager.getMessageFor(player, "commands.rtp.reloaded",
                    "RTP configuration reloaded!"));
            return true;
        }

        openRtpGui(player);
        return true;
    }

    private void openRtpGui(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Component title = langManager.getMessageFor(player, "commands.rtp.gui.title", "<green>RTP Menu");
            Inventory gui = Bukkit.createInventory(null, 9, title);

            gui.setItem(2, createGuiItem(player, Material.GRASS_BLOCK, "overworld"));
            gui.setItem(4, createGuiItem(player, Material.NETHERRACK, "nether"));
            gui.setItem(6, createGuiItem(player, Material.END_STONE, "end"));

            player.openInventory(gui);
        });
    }

    private ItemStack createGuiItem(Player player, Material material, String worldKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(langManager.getMessageFor(player, "commands.rtp.gui." + worldKey + ".name", worldKey));
        meta.lore(langManager.getMessageList(player, "rtp.gui." + worldKey + ".lore"));
        item.setItemMeta(meta);
        return item;
    }

    private World getWorld(String name) {
        return worldCache.computeIfAbsent(name, Bukkit::getWorld);
    }
}