package net.lunark.io.Managers;

import net.lunark.io.kit.KitConfigManager;
import net.lunark.io.util.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import net.lunark.io.ServerEssentials;
import net.lunark.io.auction.AuctionMessagesManager;
import net.lunark.io.economy.EconomyMessagesManager;
import net.lunark.io.kit.KitManager;
import net.lunark.io.lobby.LobbyManager;
import net.lunark.io.lobby.LobbyMessages;
import net.lunark.io.nick.NickManager;
import net.lunark.io.staff.BanManager;
import net.lunark.io.language.LanguageManager; // Add this import

import java.io.File;


public class ReloadManager {

    private static ReloadManager instance;
    private final ServerEssentials plugin;

    public ReloadManager(ServerEssentials plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static ReloadManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ReloadManager has not been initialized yet!");
        }
        return instance;
    }

    public static void reloadAll(CommandSender sender) {
        getInstance().reloadEverything(sender);
    }

    public void reloadEverything(CommandSender sender) {
        long start = System.currentTimeMillis();
        File dataFolder = plugin.getDataFolder();

        // NEW: Reload language files first (so other managers get fresh messages)
        plugin.getLanguageManager().reloadLanguages();

        plugin.reloadConfig();
        KitConfigManager.reload();

        BanManager.reload();
        ConsoleCommandManager.reload();
        JoinLeaveManager.reload();
        LobbyManager.reload();
        NickManager.reload();

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

        AuctionMessagesManager.fullReload();
        LobbyManager.reload();

        AFKManager.reload();

        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "config"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "messages"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "shop"));
        ReloadUtils.reloadAllInDirectory(new File(dataFolder, "storage"));

        for (String rootFile : new String[]{
                "Config.yml", "FJ.yml", "kits.yml", "placeholders.yml"
        }) {
            File file = new File(dataFolder, rootFile);
            if (file.exists()) ReloadUtils.reloadFile(file);
        }

        long elapsed = System.currentTimeMillis() - start;
        Bukkit.getLogger().info("[ServerEssentials] Reloaded all files in " + elapsed + "ms.");

        // Send confirmation message to the command sender
        if (sender != null) {
            sender.sendMessage("§a✅ Reload completed in " + elapsed + "ms.");
        }
    }
}