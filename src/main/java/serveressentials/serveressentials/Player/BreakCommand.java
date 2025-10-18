package serveressentials.serveressentials.Player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.Set;

public class BreakCommand implements CommandExecutor {

    private final PlayerMessages playerMessages;

    public BreakCommand(PlayerMessages playerMessages) {
        this.playerMessages = playerMessages;

        // Add default messages if missing
        playerMessages.addDefault("break.no_block", "<red>You are not looking at a breakable block within {distance} blocks.");
        playerMessages.addDefault("break.success", "<green>Broke the block you are looking at.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        int maxDistance = 5;
        Block targetBlock = player.getTargetBlock(Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR), maxDistance);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(playerMessages.get("break.no_block", "{distance}", String.valueOf(maxDistance)));
            return true;
        }

        targetBlock.setType(Material.AIR);
        player.sendMessage(playerMessages.get("break.success"));
        return true;
    }
}
