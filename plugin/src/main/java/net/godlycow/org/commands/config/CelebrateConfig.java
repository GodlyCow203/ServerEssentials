package net.godlycow.org.commands.config;

import org.bukkit.FireworkEffect;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.plugin.Plugin;

public final class CelebrateConfig {
    private final boolean flicker;
    private final boolean trail;
    private final Type type;
    private final Color color;
    private final Color fade;

    public CelebrateConfig(Plugin plugin) {
        this.flicker = plugin.getConfig().getBoolean("celebrate.effect.flicker", true);
        this.trail = plugin.getConfig().getBoolean("celebrate.effect.trail", true);
        this.type = Type.valueOf(plugin.getConfig().getString("celebrate.effect.type", "STAR"));
        this.color = Color.fromRGB(plugin.getConfig().getInt("celebrate.effect.color", 0x55C4AA));
        this.fade = Color.fromRGB(plugin.getConfig().getInt("celebrate.effect.fade", 0xAA88CC));
    }

    public FireworkEffect getEffect() {
        return FireworkEffect.builder()
                .flicker(flicker)
                .trail(trail)
                .with(type)
                .withColor(color)
                .withFade(fade)
                .build();
    }
}