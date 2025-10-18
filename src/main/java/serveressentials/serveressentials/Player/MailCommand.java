package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MailCommand implements CommandExecutor {

    private final PlayerMessages messages;
    private final File mailFile;
    private FileConfiguration mailConfig;

    public MailCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
        this.mailFile = new File(plugin.getDataFolder(), "storage/mails.yml");

        if (!mailFile.getParentFile().exists()) mailFile.getParentFile().mkdirs();
        if (!mailFile.exists()) {
            try {
                mailFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create mails.yml!");
                e.printStackTrace();
            }
        }
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Mail.only-players"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        // Read mail
        if (args.length == 0 || args[0].equalsIgnoreCase("read")) {
            List<String> inbox = mailConfig.getStringList(uuid.toString());
            if (inbox.isEmpty()) {
                player.sendMessage(messages.get("Mail.no-mail"));
            } else {
                player.sendMessage(messages.get("Mail.header"));
                for (String msg : inbox) {
                    player.sendMessage(messages.get("Mail.message", "{message}", msg));
                }
                mailConfig.set(uuid.toString(), new ArrayList<>()); // Clear after reading
                saveMail();
            }
            return true;
        }

        // Send mail
        if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID targetUUID = target.getUniqueId();
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            List<String> inbox = mailConfig.getStringList(targetUUID.toString());
            inbox.add(messages.get("Mail.from", "{sender}", player.getName(), "{message}", message).toString());
            mailConfig.set(targetUUID.toString(), inbox);
            saveMail();

            player.sendMessage(messages.get("Mail.sent", "{target}", target.getName()));
            return true;
        }

        player.sendMessage(messages.get("Mail.usage"));
        return true;
    }

    private void saveMail() {
        try {
            mailConfig.save(mailFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save mails.yml!");
            e.printStackTrace();
        }
    }
}
