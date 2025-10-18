package serveressentials.serveressentials.Fun;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.World;
import serveressentials.serveressentials.util.FunMessages;
import net.kyori.adventure.text.Component;

public class CelebrateCommand implements CommandExecutor {

    private final FunMessages funMessages;

    public CelebrateCommand(FunMessages funMessages) {
        this.funMessages = funMessages;
        // Add a default message if missing
        funMessages.addDefault("celebrate.message", "🎉 Boom! Celebration!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Location loc = player.getLocation();
        World world = loc.getWorld();

        // Spawn firework
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

        // Send configurable message from fun.yml
        Component message = funMessages.get("celebrate.message");
        player.sendMessage(message);

        return true;
    }
}
