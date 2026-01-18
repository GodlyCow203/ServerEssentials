package net.godlycow.org.bootstrap;

import net.godlycow.org.modules.ModuleManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandBootstrap {

    private final Map<String, CommandInfo> commandMap = new HashMap<>();
    private final ModuleManager moduleManager;

    public CommandBootstrap(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }


    public void register(String module, String commandName, CommandExecutor executor, @Nullable TabCompleter completer) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor cannot be null for command: " + commandName);
        }
        commandMap.put(commandName.toLowerCase(), new CommandInfo(module, executor, completer));
    }

    @SuppressWarnings("unchecked")
    public <T extends CommandExecutor> T getExecutor(String commandName) {
        CommandInfo info = commandMap.get(commandName.toLowerCase());
        return info != null ? (T) info.executor() : null;
    }

    @Nullable
    public TabCompleter getCompleter(String commandName) {
        CommandInfo info = commandMap.get(commandName.toLowerCase());
        return info != null ? info.completer() : null;
    }

    public void bootstrap() {
        int registered = 0;
        int skipped = 0;

        for (Map.Entry<String, CommandInfo> entry : commandMap.entrySet()) {
            String commandName = entry.getKey();
            CommandInfo info = entry.getValue();

            try {
                moduleManager.registerCommand(
                        info.module(),
                        commandName,
                        info.executor(),
                        info.completer()
                );
                registered++;
            } catch (Exception e) {
                System.err.println("[CommandBootstrap] Failed to register command '/" + commandName + "': " + e.getMessage());
                e.printStackTrace();
                skipped++;
            }
        }

        System.out.println("[CommandBootstrap] Registered " + registered + " commands, skipped " + skipped + " problematic ones.");
    }

    public Map<String, CommandInfo> getAllCommands() {
        return Map.copyOf(commandMap);
    }


    public record CommandInfo(String module, CommandExecutor executor, TabCompleter completer) {}

    public static final class Modules {
        public static final String ADMIN = "admin";
        public static final String TELEPORT = "teleport";
        public static final String ECONOMY = "economy";
        public static final String WORLD = "world";
        public static final String PLAYER = "player";
        public static final String MISC = "misc";
        private Modules() {}
    }
}