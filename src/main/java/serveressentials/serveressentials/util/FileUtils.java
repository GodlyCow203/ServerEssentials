package serveressentials.serveressentials.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    // Recursively get all .yml files from a directory
    public static List<File> getAllYmlFiles(File folder) {
        List<File> files = new ArrayList<>();
        if (folder == null || !folder.exists()) return files;

        File[] list = folder.listFiles();
        if (list == null) return files;

        for (File file : list) {
            if (file.isDirectory()) {
                files.addAll(getAllYmlFiles(file)); // recursion
            } else if (file.getName().endsWith(".yml")) {
                files.add(file);
            }
        }
        return files;
    }

    // Reload a single YAML file
    public static FileConfiguration reloadFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
}
