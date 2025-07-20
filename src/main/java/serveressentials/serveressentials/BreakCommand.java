package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Set;

public class BreakCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        int maxDistance = 5;
        Set<Material> transparent = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

        Block targetBlock = player.getTargetBlock(transparent, maxDistance);
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You are not looking at a breakable block within " + maxDistance + " blocks.");
            return true;
        }

        targetBlock.setType(Material.AIR);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Broke the block you are looking at.");
        return true;
    }
}
