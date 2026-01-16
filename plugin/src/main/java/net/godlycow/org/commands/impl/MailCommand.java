package net.godlycow.org.commands.impl;

import net.godlycow.org.mail.MailConfig;
import net.godlycow.org.mail.model.MailMessage;
import net.godlycow.org.mail.storage.MailStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class MailCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final MailStorage storage;
    private final MailConfig config;

    public MailCommand(JavaPlugin plugin, PlayerLanguageManager langManager, MailStorage storage, MailConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = langManager.getMessageFor(null, "commands.only-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(msg.toString());
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        String perm = "essc.command.mail." + sub;

        if (!player.hasPermission(perm)) {
            Component msg = langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow><permission></yellow>!",
                    LanguageManager.ComponentPlaceholder.of("<permission>", perm));
            player.sendMessage(msg);
            return true;
        }

        switch (sub) {
            case "read":
                readMail(player);
                break;
            case "send":
                if (args.length < 3) {
                    Component msg = langManager.getMessageFor(player, "commands.mail.send-usage",
                            "<red>Usage: /mail send <player> <message>");
                    player.sendMessage(msg);
                    return true;
                }
                handleSendWithCooldown(player, args);
                break;
            case "clear":
                clearMail(player);
                break;
            default:
                sendUsage(player);
                break;
        }
        return true;
    }

    private void sendUsage(Player player) {
        Component header = langManager.getMessageFor(player, "commands.mail.usage.header",
                "<gold>----- <white>Mail System <gold>-----");
        player.sendMessage(header);

        Component readLine = langManager.getMessageFor(player, "commands.mail.usage.read",
                "<yellow>/mail read <gray>- <white>Read your inbox");
        player.sendMessage(readLine);

        Component sendLine = langManager.getMessageFor(player, "commands.mail.usage.send",
                "<yellow>/mail send <player> <message> <gray>- <white>Send a mail");
        player.sendMessage(sendLine);

        Component clearLine = langManager.getMessageFor(player, "commands.mail.usage.clear",
                "<yellow>/mail clear <gray>- <white>Clear all mail");
        player.sendMessage(clearLine);

        Component footer = langManager.getMessageFor(player, "commands.mail.usage.footer",
                "<gold>---------------------------");
        player.sendMessage(footer);
    }

    private void readMail(Player player) {
        storage.getMailbox(player.getUniqueId()).thenAccept(mails -> {
            if (mails.isEmpty()) {
                Component msg = langManager.getMessageFor(player, "commands.mail.no-mail",
                        "<yellow>You have no mail.");
                player.sendMessage(msg);
                return;
            }

            Component header = langManager.getMessageFor(player, "commands.mail.header",
                    "<gold><bold>Your Mailbox:</bold>");
            player.sendMessage(header);

            for (int i = 0; i < mails.size(); i++) {
                MailMessage mail = mails.get(i);
                int index = i + 1;

                Component indexComp = langManager.getMessageFor(player, "commands.mail.message.index",
                        "<gray>[{index}]",
                        LanguageManager.ComponentPlaceholder.of("{index}", String.valueOf(index)));

                Component senderComp = langManager.getMessageFor(player, "commands.mail.message.sender",
                        "<yellow>{sender}: ",
                        LanguageManager.ComponentPlaceholder.of("{sender}", mail.senderName()));

                Component messageComp = LegacyComponentSerializer.legacyAmpersand().deserialize(mail.message());

                player.sendMessage(indexComp.append(senderComp).append(messageComp));
            }

            Component footer = langManager.getMessageFor(player, "commands.mail.footer",
                    "<gold>-------------------- <gray>({count} messages)",
                    LanguageManager.ComponentPlaceholder.of("{count}", String.valueOf(mails.size())));
            player.sendMessage(footer);

            storage.markAllAsRead(player.getUniqueId());
        });
    }

    private void handleSendWithCooldown(Player sender, String[] args) {
        storage.getCooldown(sender.getUniqueId()).thenAccept(lastUsed -> {
            long now = System.currentTimeMillis();
            int cdMillis = config.cooldown * 1000;

            if (now - lastUsed < cdMillis) {
                long remainingSeconds = (cdMillis - (now - lastUsed)) / 1000;
                Component msg = langManager.getMessageFor(sender, "commands.mail.cooldown",
                        "<red>Please wait {time} seconds before sending mail again.",
                        LanguageManager.ComponentPlaceholder.of("{time}", String.valueOf(remainingSeconds)));
                sender.sendMessage(msg);
                return;
            }

            String targetName = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(targetName);
            if (target == null || target.getName() == null) {
                Component msg = langManager.getMessageFor(sender, "commands.mail.player-not-found",
                        "<red>Player <yellow>{player}</yellow> not found.",
                        LanguageManager.ComponentPlaceholder.of("{player}", targetName));
                sender.sendMessage(msg);
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) messageBuilder.append(" ");
                messageBuilder.append(args[i]);
            }
            String message = messageBuilder.toString();

            if (message.length() > config.maxLength) {
                Component msg = langManager.getMessageFor(sender, "commands.mail.message-too-long",
                        "<red>Message too long! Max {max} characters.",
                        LanguageManager.ComponentPlaceholder.of("{max}", String.valueOf(config.maxLength)));
                sender.sendMessage(msg);
                return;
            }

            String plainMessage = LegacyComponentSerializer.legacyAmpersand()
                    .serialize(Component.text(message));

            MailMessage mail = MailMessage.create(sender.getUniqueId(), sender.getName(), plainMessage);

            storage.addMail(target.getUniqueId(), mail).thenRun(() -> {
                storage.saveCooldown(sender.getUniqueId(), System.currentTimeMillis());

                Component senderMsg = langManager.getMessageFor(sender, "commands.mail.sent",
                        "<green>Mail sent to <yellow>{player}</yellow>.",
                        LanguageManager.ComponentPlaceholder.of("{player}", target.getName()));
                sender.sendMessage(senderMsg);

                Player onlineTarget = target.getPlayer();
                if (onlineTarget != null && onlineTarget.isOnline()) {
                    Component targetMsg = langManager.getMessageFor(onlineTarget, "commands.mail.received",
                            "<green>You have new mail from <yellow>{sender}</yellow>! Use <white>/mail read</white>",
                            LanguageManager.ComponentPlaceholder.of("{sender}", sender.getName()));
                    onlineTarget.sendMessage(targetMsg);
                }
            }).exceptionally(ex -> {
                sender.sendMessage(langManager.getMessageFor(sender, "commands.mail.error",
                        "<red>Error sending mail. Please try again."));
                plugin.getLogger().warning("Failed to send mail: " + ex.getMessage());
                return null;
            });
        });
    }

    private void clearMail(Player player) {
        storage.getTotalMailCount(player.getUniqueId()).thenAccept(count -> {
            if (count == 0) {
                Component msg = langManager.getMessageFor(player, "commands.mail.no-mail",
                        "<yellow>You have no mail to clear.");
                player.sendMessage(msg);
                return;
            }

            storage.clearMailbox(player.getUniqueId()).thenRun(() -> {
                Component msg = langManager.getMessageFor(player, "commands.mail.cleared",
                        "<green>All mail cleared!");
                player.sendMessage(msg);
            }).exceptionally(ex -> {
                player.sendMessage(langManager.getMessageFor(player, "commands.mail.error",
                        "<red>Error clearing mail. Please try again."));
                plugin.getLogger().warning("Failed to clear mail: " + ex.getMessage());
                return null;
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("read", "send", "clear").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}