package serveressentials.serveressentials;

import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTPListener implements Listener {

    private final Random random = new Random();
    private final File rtpLogFile = new File("plugins/ServerEssentials/rtplocations.yml");
    private final YamlConfiguration rtpLog = YamlConfiguration.loadConfiguration(rtpLogFile);

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private String translateHexColor(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    public void openDimensionSelectorGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, translateHexColor("<#32CD32>&lChoose a Dimension"));

        gui.setItem(2, createItem(
                Material.GRASS_BLOCK,
                "<#00FF00>&lOverworld",
                Collections.singletonList(String.valueOf(List.of(
                        "<#7CFC00>» Peaceful plains, forests, and villages",
                        "<#00FF00>» Teleport to the <#7CFC00>Overworld"
                )))));

        gui.setItem(4, createItem(
                Material.NETHERRACK,
                "<#FF4500>&lNether",
                Collections.singletonList(String.valueOf(List.of(
                        "<#FF6347>» Dangerous and fiery dimension",
                        "<#FF4500>» Teleport to the <#FF6347>Nether"
                )))));

        gui.setItem(6, createItem(
                Material.END_STONE,
                "<#9370DB>&lThe End",
                Collections.singletonList(String.valueOf(List.of(
                        "<#BA55D3>» Mysterious land of the Endermen",
                        "<#9370DB>» Teleport to <#BA55D3>The End"
                )))));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 1f, 1f);
    }


    private ItemStack createItem(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateHexColor(name));

            List<String> translatedLore = loreLines.stream()
                    .map(this::translateHexColor)
                    .toList();

            meta.setLore(translatedLore);
            item.setItemMeta(meta);
        }
        return item;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Choose a Dimension")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        World world;
        switch (clicked.getType()) {
            case GRASS_BLOCK -> {
                world = Bukkit.getWorld("world");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            case NETHERRACK -> {
                world = Bukkit.getWorld("world_nether");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            case END_STONE -> {
                world = Bukkit.getWorld("world_the_end");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            default -> {
                return;
            }
        }

        if (world == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "That world isn't loaded!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        String worldName = world.getName();
        if (!RTPConfig.isEnabled(worldName)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Teleportation to this world is disabled.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        if (CooldownManager.isOnCooldown(player.getUniqueId())) {
            long remaining = CooldownManager.getRemaining(player.getUniqueId());
            player.sendMessage(getPrefix() + ChatColor.RED + "You're on cooldown! Try again in " + remaining + " seconds.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        CooldownManager.setCooldown(player.getUniqueId(), RTPConfig.getCooldown(worldName));

        int x, z;
        int min = RTPConfig.getMinRadius(worldName);
        int max = RTPConfig.getMaxRadius(worldName);

        if (world.getEnvironment() == World.Environment.THE_END) {
            int distance = min + random.nextInt(max - min + 1);
            double angle = random.nextDouble() * 2 * Math.PI;
            x = (int) (Math.cos(angle) * distance);
            z = (int) (Math.sin(angle) * distance);
        } else {
            x = random.nextInt(max - min + 1) + min;
            x *= random.nextBoolean() ? 1 : -1;
            z = random.nextInt(max - min + 1) + min;
            z *= random.nextBoolean() ? 1 : -1;
        }

        int y;
        if (world.getEnvironment() == World.Environment.NETHER) {
            y = 64;
        } else {
            y = world.getHighestBlockYAt(x, z);
            if (y <= 0) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Couldn't find a safe teleport spot. Try again.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
                return;
            }
            y += 1;
        }

        Location loc = new Location(world, x + 0.5, y, z + 0.5);
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());

        player.closeInventory();
        world.spawnParticle(Particle.PORTAL, player.getLocation(), 100, 1, 1, 1);
        world.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        Bukkit.getScheduler().runTaskLater(ServerEssentials.getInstance(), () -> {
            player.teleport(loc);
            player.sendMessage(translateHexColor(getPrefix() + "<#00FFAA>Teleported to a random location in <#FFD700>" + world.getName() + "<#00FFAA>!"));
            world.spawnParticle(Particle.PORTAL, loc, 100, 1, 1, 1);
            world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }, 40L);

        rtpLog.set(player.getName(), world.getName() + ": " + x + " " + y + " " + z);
        try {
            rtpLog.save(rtpLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BackManager.setLastLocation(player.getUniqueId(), player.getLocation());
    }
}
