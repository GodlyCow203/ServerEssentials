package net.lunark.io.sellgui;

import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand implements CommandExecutor {
    private final PlayerLanguageManager langManager;
    private final SellGUIManager guiManager;

    public SellCommand(PlayerLanguageManager langManager, SellGUIManager guiManager) {
        this.langManager = langManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-player",
                    "<red>This command can only be used by players!"));
            return true;
        }

        guiManager.openSellGUI(player);
        return true;
    }
}