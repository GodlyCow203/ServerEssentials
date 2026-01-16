package com.godlycow.testapi;

import com.serveressentials.api.nick.NickAPI;
import com.serveressentials.api.nick.NickInfo;
import com.serveressentials.api.nick.NickValidationRules;
import com.serveressentials.api.nick.event.NickSetEvent;
import com.serveressentials.api.nick.event.NickResetEvent;
import com.serveressentials.api.nick.event.NickReloadEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public final class NickAPITestCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private NickAPI api;

    public NickAPITestCommand(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setAPI(@NotNull NickAPI api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (api == null) {
            player.sendMessage("§cNickAPI is not yet available. Please try again in a moment.");
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
            String nickname = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            api.setNickname(player, nickname).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aNickname set to '" + nickname + "'!");
                } else {
                    player.sendMessage("§cFailed to set nickname! Check validation rules, cooldown, or daily limit.");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            api.resetNickname(player).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aNickname reset!");
                } else {
                    player.sendMessage("§cFailed to reset nickname!");
                }
            });
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("resetother")) {
            String targetName = args[1];
            api.resetOtherNickname(player, targetName).thenAccept(success -> {
                if (success) {
                    player.sendMessage("§aReset nickname for " + targetName + "!");
                } else {
                    player.sendMessage("§cFailed to reset nickname! Player not found or no permission.");
                }
            });
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("get")) {
            String targetName = args.length == 2 ? args[1] : player.getName();
            Player target = plugin.getServer().getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }

            api.getNickname(target.getUniqueId()).thenAccept(opt -> {
                if (opt.isPresent()) {
                    NickInfo info = opt.get();
                    player.sendMessage("§e" + targetName + "'s nickname: '" + info.getNickname() + "'");
                } else {
                    player.sendMessage("§e" + targetName + " has no nickname set.");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            player.sendMessage("§6All Nicknames:");
            api.getAllNicknames().forEach(info -> {
                player.sendMessage("§7" + info.getPlayerName() + " -> '" + info.getNickname() + "'");
            });
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("validate")) {
            String nickname = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            api.validateNickname(player, nickname).thenAccept(valid -> {
                if (valid) {
                    player.sendMessage("§aNickname '" + nickname + "' is valid!");
                } else {
                    player.sendMessage("§cNickname '" + nickname + "' is invalid!");
                }
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("rules")) {
            NickValidationRules rules = api.getValidationRules();
            player.sendMessage("§6Nickname Validation Rules:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            player.sendMessage("§7Min Length: " + rules.getMinLength());
            player.sendMessage("§7Max Length: " + rules.getMaxLength());
            player.sendMessage("§7Allow Formatting: " + rules.isAllowFormatting());
            player.sendMessage("§7Allow Reset: " + rules.isAllowReset());
            player.sendMessage("§7Allow Duplicates: " + rules.isAllowDuplicates());
            player.sendMessage("§7Cooldown: " + rules.getCooldown() + " seconds");
            player.sendMessage("§7Max Changes/Day: " + rules.getMaxChangesPerDay());
            player.sendMessage("§7Blocked Words: " + String.join(", ", rules.getBlockedWords()));
            player.sendMessage("§7Blacklist Patterns: " + String.join(", ", rules.getBlacklistPatterns()));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("cooldown")) {
            api.getRemainingCooldown(player.getUniqueId()).thenAccept(remaining -> {
                if (remaining > 0) {
                    player.sendMessage("§eRemaining cooldown: " + remaining + " seconds");
                } else {
                    player.sendMessage("§aNo cooldown active");
                }
            });
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("daily")) {
            api.getDailyChanges(player.getUniqueId()).thenAccept(changes -> {
                player.sendMessage("§eDaily changes used: " + changes + " / " + api.getValidationRules().getMaxChangesPerDay());
            });
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            player.sendMessage("§6NickAPI Status:");
            player.sendMessage("§7Enabled: " + api.isEnabled());
            player.sendMessage("§7Your current nickname: " +
                    api.getNickname(player.getUniqueId()).join()
                            .map(NickInfo::getNickname)
                            .orElse("None (using " + player.getName() + ")"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            api.reload().thenAccept(v -> {
                player.sendMessage("§aNickname configuration reloaded!");
            });
            return true;
        }

        sendUsage(player);
        return true;
    }

    @EventHandler
    public void onNickSet(@NotNull NickSetEvent event) {
        plugin.getLogger().info("[NickAPITest] " + event.getPlayer().getName() +
                " changed nickname from '" + event.getOldNickname() +
                "' to '" + event.getNickInfo().getNickname() + "'");
    }

    @EventHandler
    public void onNickReset(@NotNull NickResetEvent event) {
        if (event.isSelf()) {
            plugin.getLogger().info("[NickAPITest] " + event.getPlayer().getName() + " reset their own nickname");
        } else {
            plugin.getLogger().info("[NickAPITest] " + event.getPlayer().getName() +
                    " reset nickname for " + event.getTargetName());
        }
    }

    @EventHandler
    public void onNickReload(@NotNull NickReloadEvent event) {
        plugin.getLogger().info("[NickAPITest] " + event.getPlayer().getName() +
                " reloaded " + event.getAffectedNicks().size() + " nicknames");
    }

    private void sendUsage(@NotNull Player player) {
        player.sendMessage("§6NickAPI Test Command Usage:");
        player.sendMessage("§7/nickapitest set <nickname> - Set your nickname");
        player.sendMessage("§7/nickapitest reset - Reset your nickname");
        player.sendMessage("§7/nickapitest resetother <player> - Reset another player's nickname");
        player.sendMessage("§7/nickapitest get [player] - Get nickname");
        player.sendMessage("§7/nickapitest all - List all nicknames");
        player.sendMessage("§7/nickapitest validate <nickname> - Validate a nickname");
        player.sendMessage("§7/nickapitest rules - Show validation rules");
        player.sendMessage("§7/nickapitest cooldown - Check cooldown");
        player.sendMessage("§7/nickapitest daily - Check daily changes");
        player.sendMessage("§7/nickapitest status - Show API status");
        player.sendMessage("§7/nickapitest reload - Reload configuration");
    }
}