package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.FreezeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class FreezeCommand implements CommandExecutor, Listener, TabCompleter {
    private static final String PERMISSION = "essc.command.freeze";
    private static final String PERMISSION_OTHERS = "essc.freeze.command.others";
    private static final Set<UUID> FROZEN_PLAYERS = new HashSet<>();

    private final PlayerLanguageManager langManager;
    private final Plugin plugin;

    public FreezeCommand(PlayerLanguageManager langManager, FreezeConfig config, Plugin plugin) {
        this.langManager = langManager;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static Set<UUID> getFrozenPlayers() {
        return FROZEN_PLAYERS;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.freeze.only-player",
                    "<red>Only players can use this command!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.freeze.usage",
                    "<red>Usage: /freeze <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.freeze.player-not-found",
                    "<red>Player not found: <yellow>{player}</yellow>",
                    ComponentPlaceholder.of("{player}", args[0])));
            return true;
        }

        if (FROZEN_PLAYERS.contains(target.getUniqueId())) {
            FROZEN_PLAYERS.remove(target.getUniqueId());
            player.sendMessage(langManager.getMessageFor(player, "commands.freeze.unfroze",
                    "<green>Unfroze <white>{player}</white>.",
                    ComponentPlaceholder.of("{player}", target.getName())));
            target.sendMessage(langManager.getMessageFor(target, "commands.freeze.target-unfrozen",
                    "<green>You have been unfrozen."));
        } else {
            FROZEN_PLAYERS.add(target.getUniqueId());
            player.sendMessage(langManager.getMessageFor(player, "commands.freeze.froze",
                    "<green>Froze <white>{player}</white>.",
                    ComponentPlaceholder.of("{player}", target.getName())));
            target.sendMessage(langManager.getMessageFor(target, "commands.freeze.target-frozen",
                    "<red>You have been frozen!"));
        }

        return true;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (FROZEN_PLAYERS.contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(langManager.getMessageFor(event.getPlayer(), "commands.freeze.target-frozen",
                    "<red>You have been frozen!"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (FROZEN_PLAYERS.contains(player.getUniqueId())) {
            if (player.isGliding() || player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (FROZEN_PLAYERS.contains(player.getUniqueId())) {
            Material mat = event.getItem() != null ? event.getItem().getType() : null;
            if (mat == Material.ENDER_PEARL) return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (FROZEN_PLAYERS.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}