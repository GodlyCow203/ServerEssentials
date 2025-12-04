package net.lunark.io.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

public class EnderChestCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public EnderChestCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("EnderChest.only-players"));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("serveressentials.enderchest")) {
                player.sendMessage(messages.get("EnderChest.no-permission"));
                return true;
            }

            player.sendMessage(messages.get("EnderChest.opening"));
            player.openInventory(player.getEnderChest());
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("serveressentials.enderchest.others")) {
                player.sendMessage(messages.get("EnderChest.no-permission-others"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target != null && target.isOnline()) {
                String msg = PlainTextComponentSerializer.plainText().serialize(messages.get("EnderChest.opening-other"));
                msg = msg.replace("%player%", target.getName());
                player.sendMessage(Component.text(msg));

                player.openInventory(target.getEnderChest());
                return true;
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
            if (offline == null || !offline.hasPlayedBefore()) {
                String msg = PlainTextComponentSerializer.plainText().serialize(messages.get("EnderChest.player-not-found"));
                msg = msg.replace("%player%", args[0]);
                player.sendMessage(Component.text(msg));
                return true;
            }

            player.sendMessage(messages.get("EnderChest.offline-not-supported"));
            return true;
        }

        player.sendMessage(messages.get("EnderChest.usage"));
        return true;
    }
}
