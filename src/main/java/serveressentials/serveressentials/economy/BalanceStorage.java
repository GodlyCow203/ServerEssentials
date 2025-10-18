package serveressentials.serveressentials.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import serveressentials.serveressentials.ServerEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BalanceStorage {

    private static File file;
    private static FileConfiguration config;
    private static final Economy economy = ServerEssentials.getEconomy();

    public static void init() {
        // Ensure storage folder exists
        File storageFolder = new File(ServerEssentials.getInstance().getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        // File for balances.yml
        file = new File(storageFolder, "balances.yml");

        // Create file if it doesn't exist
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load configuration
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void saveAllBalances() {
        for (OfflinePlayer player : ServerEssentials.getInstance().getServer().getOfflinePlayers()) {
            double balance = economy.getBalance(player);
            config.set(player.getUniqueId().toString(), balance);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
