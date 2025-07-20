package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ReportNotifierListener implements Listener {

    private final ReportCommand reportCommand;

    public ReportNotifierListener(ReportCommand reportCommand) {
        this.reportCommand = reportCommand;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;

        UUID uuid = player.getUniqueId();
        File file = reportCommand.getFile();
        YamlConfiguration config = reportCommand.getConfig();

        List<String> messages = config.getStringList("reports." + uuid + ".messages");
        if (messages != null && !messages.isEmpty()) {
            player.sendMessage(getPrefix() + ChatColor.YELLOW + "You have new reports:");
            for (String msg : messages) {
                player.sendMessage(msg);
            }
            config.set("reports." + uuid + ".messages", null);
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
