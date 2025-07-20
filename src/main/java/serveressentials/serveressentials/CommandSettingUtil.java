package serveressentials.serveressentials;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandSettingUtil {

    private static final String FILE_NAME = "commands.yml";
    private static FileConfiguration config;
    private static File file;


    private static final Set<String> NON_DISABLEABLE_COMMANDS = Set.of("serveressentials", "version", "help");


    private static final Map<String, Boolean> DEFAULT_COMMANDS = new HashMap<>();

    static {
        DEFAULT_COMMANDS.put("balance", true);
        DEFAULT_COMMANDS.put("warp", true);
        DEFAULT_COMMANDS.put("sethome", false);
        DEFAULT_COMMANDS.put("adminchat", true);
        DEFAULT_COMMANDS.put("serveressentials", true);
        DEFAULT_COMMANDS.put("version", true); //
    }

    public static void setup(ServerEssentials plugin) {
        file = new File(plugin.getDataFolder(), FILE_NAME);


        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
                config = YamlConfiguration.loadConfiguration(file);


                for (Map.Entry<String, Boolean> entry : DEFAULT_COMMANDS.entrySet()) {
                    config.set(entry.getKey(), entry.getValue());
                }
                config.save(file);
                plugin.getLogger().info("Generated default " + FILE_NAME);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create " + FILE_NAME);
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }

    public static boolean isCommandEnabled(String command) {
        if (NON_DISABLEABLE_COMMANDS.contains(command.toLowerCase())) return true;

        if (config == null) {
            ServerEssentials.getInstance().getLogger().warning("commands.yml not loaded, allowing command: " + command);
            return true;
        }

        return config.getBoolean(command.toLowerCase(), true);
    }

}
