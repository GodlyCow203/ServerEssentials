package serveressentials.serveressentials.Managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.TPA.TPA;
import serveressentials.serveressentials.auction.AuctionMessagesManager;
import serveressentials.serveressentials.config.RTPConfig;
import serveressentials.serveressentials.economy.EconomyMessagesManager;
import serveressentials.serveressentials.economy.ShopGUIManager;
import serveressentials.serveressentials.kit.KitManager;
import serveressentials.serveressentials.lobby.LobbyManager;
import serveressentials.serveressentials.lobby.LobbyMessages;
import serveressentials.serveressentials.nick.NickManager;
import serveressentials.serveressentials.scoreboard.CustomScoreboardManager;
import serveressentials.serveressentials.staff.BanManager;
import serveressentials.serveressentials.util.*;

import java.io.File;

import static serveressentials.serveressentials.economy.ShopGUIManager.*;

public class ReloadManager {

    private static ReloadManager instance;
    private final ServerEssentials plugin;

    public ReloadManager(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * Get the global ReloadManager instance.
     */
    public static ReloadManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ReloadManager has not been initialized yet!");
        }
        return instance;
    }

    /**
     * Static method to reload everything from anywhere.
     */
    public static void reloadAll(CommandSender sender) {
        getInstance().reloadEverything(sender);
    }

    /**
     * Performs the full reload process.
     */
    public void reloadEverything(CommandSender sender) {
        long start = System.currentTimeMillis();
        sender.sendMessage("§e[ServerEssentials] Reloading all plugin files...");

        File dataFolder = plugin.getDataFolder();
        plugin.reloadConfig();
        KitManager.reload();
        ShopGUIManager.reload(plugin.getDataFolder());
        ShopGUIManager.refreshOpenInventories();
        sectionConfigs.clear();
        BanManager.reload();
        ConsoleCommandManager.reload();
        DailyRewardsManager.reload();
        JoinLeaveManager.reload();
        LobbyManager.reload();
        NickManager.reload();
        RTPConfig.fullReload();
        CustomScoreboardManager.reloadAll();
        RulesManager.reload();
        DailyMessagesManager.fullReload();
        EconomyMessagesManager.reload();

        FunMessages.reloadAll();
        HomeMessages.reloadAll();
        KitMessages.reloadAll();
        LobbyMessages.reload();
        PlayerMessages.reloadAll();
        VaultMessages.fullReload();
        WarpMessages.fullReload();
        ServerMessages.fullReload();
        MessagesManager.fullReload();

        openSection.clear();
        RTPConfig.fullReload();
        RTPMessages.fullReload();
        AuctionMessagesManager.fullReload();
        LobbyManager.reload();
        TPA.fullReload();

        currentPage.clear();
        AFKManager.reload();
        plugin.getKitMessages().reload();
        ShopGUIManager.reload(plugin.getDataFolder());

        // Reload directories
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "config"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "messages"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "shop"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "storage"));

        // Reload root files
        for (String rootFile : new String[]{
                "Config.yml", "FJ.yml", "kits.yml", "placeholders.yml"
        }) {
            File file = new File(dataFolder, rootFile);
            if (file.exists()) ReloadUtils.reloadFile(file);
        }

        long elapsed = System.currentTimeMillis() - start;
        sender.sendMessage("§a[ServerEssentials] Reload complete in " + elapsed + "ms!");
        Bukkit.getLogger().info("[ServerEssentials] Reloaded all files in " + elapsed + "ms.");
    }
}
