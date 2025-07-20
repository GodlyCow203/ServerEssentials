package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;

public class CelebrateCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Location loc = player.getLocation();
        World world = loc.getWorld();

        Firework firework = (Firework) world.spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(Type.STAR)
                .withColor(Color.AQUA)
                .withFade(Color.PURPLE)
                .build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);

        player.sendMessage(getPrefix() + ChatColor.GOLD + "🎉 Boom! Celebration!");
        return true;
    }
}
