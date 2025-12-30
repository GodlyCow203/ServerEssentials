package com.godlycow.testapi;

import com.serveressentials.api.mail.MailAPI;
import com.serveressentials.api.mail.MailInfo;
import com.serveressentials.api.mail.MailStats;
import com.serveressentials.api.mail.event.MailSendEvent;
import com.serveressentials.api.mail.event.MailReadEvent;
import com.serveressentials.api.mail.event.MailClearEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public final class MailAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private MailAPI api;

    public MailAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull MailAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cMailAPI is not yet available. Please try again in a moment.");
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("send")) {
            String targetName = args[1];
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            api.sendMail(player, targetName, message).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aMail sent to " + targetName + "!");
                } else {
                    player.sendMessage("§cFailed to send mail! Target not found, on cooldown, or message too long.");
                }
            });
            return true;
        }


        if (args.length == 1 && args[0].equalsIgnoreCase("read")) {
            api.getMailbox(player).thenAccept(mails -> {
                if (mails.isEmpty()) {
                    player.sendMessage("§eYou have no mail.");
                } else {
                    player.sendMessage("§6Your Mailbox (" + mails.size() + " messages):");
                    for (int i = 0; i < mails.size(); i++) {
                        MailInfo mail = mails.get(i);
                        player.sendMessage("§7[" + (i + 1) + "] §e" + mail.getSenderName() + ": §f" + mail.getMessage());
                    }
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("unread")) {
            api.getMailStats(player.getUniqueId()).thenAccept(stats -> {
                player.sendMessage("§eYou have " + stats.getUnreadCount() + " unread messages.");
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            api.clearMailbox(player.getUniqueId()).thenAccept(v -> {
                player.sendMessage("§aMailbox cleared!");
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {
            api.getMailStats(player.getUniqueId()).thenAccept(stats -> {
                player.sendMessage("§6Mail Statistics:");
                player.sendMessage("§7Total: " + stats.getTotalCount());
                player.sendMessage("§7Unread: " + stats.getUnreadCount());
                player.sendMessage("§7Last Activity: " + (stats.getLastActivity() > 0 ?
                        new java.util.Date(stats.getLastActivity()) : "Never"));
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cooldown")) {
            api.getRemainingCooldown(player.getUniqueId()).thenAccept(remaining -> {
                if (remaining > 0) {
                    player.sendMessage("§eRemaining cooldown: " + remaining + " seconds");
                } else {
                    player.sendMessage("§aNo cooldown active");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6MailAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            api.getMailStats(player.getUniqueId()).thenAccept(stats -> {
                player.sendMessage("§7Your total messages: " + stats.getTotalCount());
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aMail configuration reloaded!");
            });
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onMailSend(@NotNull MailSendEvent event) {
        plugin.getLogger().info("[MailAPITest] " + event.getPlayer().getName() +
                " sent mail to " + event.getTargetName() + ": " +
                event.getMailInfo().getMessage());
    }

    @EventHandler
    public void onMailRead(@NotNull MailReadEvent event) {
        plugin.getLogger().info("[MailAPITest] " + event.getPlayer().getName() +
                " read " + event.getMails().size() + " messages");
    }

    @EventHandler
    public void onMailClear(@NotNull MailClearEvent event) {
        plugin.getLogger().info("[MailAPITest] " + event.getPlayer().getName() +
                " cleared " + event.getClearedCount() + " messages");
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6MailAPI Test Command Usage:");
        player.sendMessage("§7/mailapitest send <player> <message> - Send mail");
        player.sendMessage("§7/mailapitest read - Read your mailbox");
        player.sendMessage("§7/mailapitest unread - Check unread count");
        player.sendMessage("§7/mailapitest clear - Clear your mailbox");
        player.sendMessage("§7/mailapitest stats - View mailbox statistics");
        player.sendMessage("§7/mailapitest cooldown - Check send cooldown");
        player.sendMessage("§7/mailapitest status - Show API status");
        player.sendMessage("§7/mailapitest reload - Reload configuration");
    }
}