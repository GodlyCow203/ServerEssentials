package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.MessagesManager; // <--- MessagesManager, NOT ServerMessages

import java.util.ArrayList;
import java.util.List;

public class EnderSeeCommand implements CommandExecutor, TabCompleter {

    private final ServerEssentials plugin;
    private final MessagesManager messages; // <--- MessagesManager

    public EnderSeeCommand(ServerEssentials plugin, MessagesManager messages) {
        this.plugin = plugin;
        this.messages = messages; // <--- Assign MessagesManager
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("serveressentials.endersee")) {
            player.sendMessage(messages.get("staff.yml", "endersee.no-permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.get("staff.yml", "endersee.usage", "{usage}", "/endersee <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(messages.get("staff.yml", "endersee.invalid-player", "{player}", args[0]));
            return true;
        }

        Inventory enderChestCopy = Bukkit.createInventory(null, 27, Component.text("EnderChest of " + target.getName()));
        ItemStack[] contents = target.getEnderChest().getContents();
        enderChestCopy.setContents(contents);
        player.openInventory(enderChestCopy);

        player.sendMessage(messages.get("staff.yml", "endersee.opened", "{player}", target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) suggestions.add(p.getName());
        }
        return suggestions;
    }
}
