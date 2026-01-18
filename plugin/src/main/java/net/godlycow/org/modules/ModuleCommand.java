package net.godlycow.org.modules;

import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModuleCommand implements CommandExecutor, TabCompleter {
    private final ModuleManager moduleManager;
    private final PlayerLanguageManager playerLanguageManager;
    private static final MiniMessage mini = MiniMessage.miniMessage();

    public ModuleCommand(ModuleManager moduleManager, PlayerLanguageManager playerLanguageManager) {
        this.moduleManager = moduleManager;
        this.playerLanguageManager = playerLanguageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("essc.command.module")) {
            sender.sendMessage(mini.deserialize("<red>✗ No permission."));
            return true;
        }
        sendModuleList(sender);
        return true;
    }

    private void sendModuleList(CommandSender sender) {
        List<Component> messages = new ArrayList<>();
        messages.add(mini.deserialize("<yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        messages.add(mini.deserialize("<gold><bold> MODULES & COMMANDS"));
        messages.add(Component.empty());

        for (Map.Entry<String, Module> entry : moduleManager.getModules().entrySet()) {
            String moduleName = entry.getKey();
            Module module = entry.getValue();
            String status = module.isEnabled() ? "<green>✓ ENABLED" : "<red>✗ DISABLED";

            messages.add(mini.deserialize("<yellow>▸ <gold>" + moduleName + " <gray>- " + status));

            for (Map.Entry<String, Boolean> cmdEntry : module.getCommands().entrySet()) {
                String cmdName = cmdEntry.getKey();
                boolean enabled = cmdEntry.getValue();
                String cmdStatus = enabled ? "<green>✓" : "<red>✗";
                String cmdColor = enabled ? "" : "<strikethrough>";

                messages.add(mini.deserialize("  <dark_gray}> <white>" + cmdColor + "/" + cmdName + " <gray>" + cmdStatus));
            }
        }

        messages.add(Component.empty());
        messages.add(mini.deserialize("<yellow>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        messages.forEach(sender::sendMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}