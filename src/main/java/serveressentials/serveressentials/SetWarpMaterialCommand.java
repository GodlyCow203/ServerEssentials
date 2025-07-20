package serveressentials.serveressentials;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.WarpManager;

public class SetWarpMaterialCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /setwarpmaterial <warp> <material>");
            return true;
        }

        String warp = args[0].toLowerCase();
        String matName = args[1].toUpperCase();

        if (!WarpManager.warpExists(warp)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp does not exist.");
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Invalid material.");
            return true;
        }

        WarpManager.setWarpMaterial(warp, material);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Material for warp " + warp + " set to " + ChatColor.YELLOW + material.name());
        return true;
    }
}
