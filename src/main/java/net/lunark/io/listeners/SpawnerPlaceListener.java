package net.lunark.io.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerPlaceListener implements Listener {

    private final JavaPlugin plugin;

    public SpawnerPlaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SPAWNER && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "mobType");
            if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String mobTypeName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                EntityType mobType = EntityType.valueOf(mobTypeName);

                Block block = event.getBlockPlaced();
                if (block.getState() instanceof CreatureSpawner spawner) {
                    spawner.setSpawnedType(mobType);
                    spawner.update();
                }
            }
        }
    }
}
