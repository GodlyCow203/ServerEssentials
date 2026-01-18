package net.godlycow.org.modules;

import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ModuleManager {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final Map<String, Module> modules = new HashMap<>();
    private final Map<String, CommandRegistration> commandRegistrations = new HashMap<>();
    private boolean loaded = false;

    private static class CommandRegistration {
        final String moduleName;
        final Command originalCommand;
        CommandRegistration(String moduleName, Command originalCommand) {
            this.moduleName = moduleName;
            this.originalCommand = originalCommand;
        }
    }

    public ModuleManager(JavaPlugin plugin, PlayerLanguageManager langManager) {
        this.plugin = plugin;
        this.langManager = langManager;
    }

    public void loadFromConfig() {
        modules.clear();
        commandRegistrations.clear();
        loaded = false;

        var section = plugin.getConfig().getConfigurationSection("modules");
        if (section == null) {
            plugin.getLogger().warning("No 'modules' section in config.yml");
            loaded = true;
            return;
        }

        int cmdCount = 0;
        for (String moduleName : section.getKeys(false)) {
            boolean enabled = section.getBoolean(moduleName + ".enabled", true);
            Map<String, Boolean> commands = new HashMap<>();
            var cmdSection = section.getConfigurationSection(moduleName + ".commands");
            if (cmdSection != null) {
                for (String cmd : cmdSection.getKeys(false)) {
                    commands.put(cmd.toLowerCase(), cmdSection.getBoolean(cmd, true));
                    cmdCount++;
                }
            }
            modules.put(moduleName.toLowerCase(), new Module(enabled, commands));
        }

        loaded = true;
        plugin.getLogger().info("Loaded " + modules.size() + " modules, " + cmdCount + " commands");
    }

    public void registerCommand(String moduleName, String commandName,
                                CommandExecutor executor, @Nullable TabCompleter completer) {
        if (!loaded) throw new IllegalStateException("Call loadFromConfig() first!");

        PluginCommand cmd = plugin.getCommand(commandName);
        if (cmd == null) {
            plugin.getLogger().warning("Command /" + commandName + " not in plugin.yml");
            return;
        }

        commandRegistrations.put(commandName.toLowerCase(),
                new CommandRegistration(moduleName, cmd));

        if (isCommandEnabled(moduleName, commandName)) {
            cmd.setExecutor(new WrappedCommand(moduleName, commandName, executor, completer, this, langManager));
            cmd.setTabCompleter(completer);
            plugin.getLogger().fine("Command /" + commandName + " enabled");
        } else {
            unregisterCommand(commandName);
        }
    }

    private boolean isCommandEnabled(String moduleName, String commandName) {
        Module mod = modules.get(moduleName.toLowerCase());
        if (mod == null) return true;
        if (!mod.isEnabled()) return false;
        return mod.isCommandEnabled(commandName);
    }

    public void unregisterCommand(String commandName) {
        CommandRegistration reg = commandRegistrations.get(commandName.toLowerCase());
        if (reg == null) return;

        try {
            CommandMap commandMap = getCommandMap();
            Map<String, Command> knownCommands = getKnownCommands(commandMap);

            Command current = knownCommands.get(commandName.toLowerCase());
            if (!(current instanceof PluginCommand pc) || pc.getPlugin() != plugin) {
                return;
            }

            knownCommands.remove(commandName.toLowerCase());
            knownCommands.remove(plugin.getName().toLowerCase() + ":" + commandName.toLowerCase());

            for (String alias : current.getAliases()) {
                knownCommands.remove(alias.toLowerCase());
                knownCommands.remove(plugin.getName().toLowerCase() + ":" + alias.toLowerCase());
            }

            current.unregister(commandMap);
            plugin.getLogger().fine("Command /" + commandName + " unregistered");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to unregister /" + commandName, e);
        }
    }

    private CommandMap getCommandMap() throws Exception {
        Server server = plugin.getServer();
        return (CommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands(CommandMap commandMap) throws Exception {
        Class<?> clazz = commandMap.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("knownCommands");
                field.setAccessible(true);
                return (Map<String, Command>) field.get(commandMap);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("knownCommands not found in hierarchy");
    }

    public boolean canExecute(String module, String command) {
        Module mod = modules.get(module.toLowerCase());
        if (mod == null) return true;
        if (!mod.isEnabled()) return false;
        return mod.isCommandEnabled(command);
    }

    public Map<String, Module> getModules() {
        return Collections.unmodifiableMap(modules);
    }
}