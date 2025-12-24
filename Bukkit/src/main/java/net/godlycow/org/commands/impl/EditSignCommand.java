package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.EditSignConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class EditSignCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.editsign";
    private final PlayerLanguageManager langManager;
    private final EditSignConfig config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public EditSignCommand(PlayerLanguageManager langManager, EditSignConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.editsign.only-player", "<red>Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.editsign.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || !target.getType().name().contains("SIGN")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.editsign.not-looking-at-sign", "<red>You are not looking at a sign."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.editsign.usage", "<yellow>Usage: /editsign <line1>|<line2>|<line3>|<line4>"));
            return true;
        }

        String input = String.join(" ", args);
        String[] lines = input.split("\\|");
        Sign sign = (Sign) target.getState();

        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            String line = LegacyComponentSerializer.legacySection().serialize(mm.deserialize(lines[i].trim()));
            sign.setLine(i, line);
        }
        sign.update();

        player.sendMessage(langManager.getMessageFor(player, "commands.editsign.success", "<green>Sign updated successfully!"));
        return true;
    }
}