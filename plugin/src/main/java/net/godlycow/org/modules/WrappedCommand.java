package net.godlycow.org.modules;

import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class WrappedCommand implements CommandExecutor, TabCompleter {
    private static final MiniMessage mini = MiniMessage.miniMessage();
    private final String moduleName;
    private final String commandName;
    private final CommandExecutor executor;
    private final TabCompleter completer;
    private final ModuleManager moduleManager;
    private final PlayerLanguageManager langManager;

    public WrappedCommand(String moduleName, String commandName, CommandExecutor executor,
                          @Nullable TabCompleter completer, ModuleManager moduleManager,
                          PlayerLanguageManager langManager) {
        this.moduleName = moduleName;
        this.commandName = commandName;
        this.executor = executor;
        this.completer = completer;
        this.moduleManager = moduleManager;
        this.langManager = langManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!moduleManager.canExecute(moduleName, commandName)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands.module.disabled-message",
                    "<red>✗ Command '/{cmd}' is disabled by server administrator.",
                    LanguageManager.ComponentPlaceholder.of("{cmd}", commandName)));
            return true;
        }
        return executor.onCommand(sender, command, label, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!moduleManager.canExecute(moduleName, commandName)) {
            return Collections.emptyList();
        }
        return completer != null ? completer.onTabComplete(sender, command, alias, args)
                : Collections.emptyList();
    }
}