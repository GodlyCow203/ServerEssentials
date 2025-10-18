package serveressentials.serveressentials.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReloadUtils {

    /**
     * Reloads a single YAML file and returns its FileConfiguration.
     */
    public static FileConfiguration reloadFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Recursively finds all .yml files under a folder.
     */
    public static List<File> getAllYmlFiles(File folder) {
        List<File> result = new ArrayList<>();
        if (folder == null || !folder.exists()) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File f : files) {
            if (f.isDirectory()) result.addAll(getAllYmlFiles(f));
            else if (f.getName().toLowerCase().endsWith(".yml")) result.add(f);
        }
        return result;
    }

    /**
     * Attempts to reload all .yml files inside a directory.
     * Returns how many were successfully reloaded.
     */
    public static int reloadAllInDirectory(File folder) {
        int count = 0;
        for (File f : getAllYmlFiles(folder)) {
            try {
                reloadFile(f);
                count++;
            } catch (Exception e) {
                System.err.println("[ServerEssentials] Failed to reload " + f.getName() + ": " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * Ensures a file exists; if not, creates an empty one.
     */
    public static void ensureExists(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
