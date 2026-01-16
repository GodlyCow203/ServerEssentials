package net.godlycow.org.commands.impl;

import net.godlycow.org.auction.gui.AuctionGUIListener;
import net.godlycow.org.auction.model.AuctionItem;
import net.godlycow.org.auction.storage.AuctionStorage;
import net.godlycow.org.commands.config.AuctionConfig;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;
import java.util.logging.Level;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class AuctionCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION_BASE = "essc.command.auction";
    private static final String PERMISSION_USE = "essc.command.auction.use";
    private static final String PERMISSION_SELL = "essc.command.auction.sell";

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final AuctionConfig config;
    private final AuctionStorage storage;
    private final AuctionGUIListener guiListener;
    private final EconomyManager economyManager;

    public AuctionCommand(Plugin plugin, PlayerLanguageManager langManager, AuctionConfig config,
                          AuctionStorage storage, AuctionGUIListener guiListener, EconomyManager economyManager) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.guiListener = guiListener;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (sender instanceof ConsoleCommandSender) {
                plugin.getLogger().log(Level.INFO, langManager.getMessageFor(null, "commands.auction.only-player",
                        "<red>This command can only be used by players!").toString());
            } else {
                sender.sendMessage(langManager.getMessageFor(null, "commands.auction.only-player",
                        "<red>This command can only be used by players!"));
            }
            return true;
        }

        if (!player.hasPermission(PERMISSION_BASE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_BASE)));
            return true;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.no-economy",
                    "<red>✗ Economy system is not available."));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission(PERMISSION_USE)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_USE)));
                return true;
            }
            guiListener.openAuctionGUI(player, 1);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("sell")) {
            handleSell(player, args);
            return true;
        }

        if (subcommand.equals("my")) {
            if (!player.hasPermission(PERMISSION_USE)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_USE)));
                return true;
            }
            guiListener.openPlayerItemsGUI(player, 1);
            return true;
        }

        showUsage(player);
        return true;
    }

    private void handleSell(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION_SELL)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_SELL)));
            return;
        }

        if (!economyManager.isEnabled()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.no-economy",
                    "<red>✗ Economy system is not available."));
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.sell.no-item",
                    "<red>You must hold an item in your hand to sell!"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.sell.usage",
                    "<red>Usage: /auction sell <price>"));
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.sell.invalid-price",
                    "<red>Invalid price! Must be a positive number."));
            return;
        }

        if (handItem.getAmount() > config.maxSellLimit) {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.sell.max-limit",
                    "<red>You can only sell up to <yellow>{max}</yellow> items at once!",
                    ComponentPlaceholder.of("{max}", config.maxSellLimit)));
            return;
        }

        long expirationTime = System.currentTimeMillis() + (config.expirationDays * 24L * 60L * 60L * 1000L);
        ItemStack itemToSell = handItem.clone();
        player.getInventory().setItemInMainHand(null);

        AuctionItem auctionItem = new AuctionItem(player.getUniqueId(), itemToSell, price, expirationTime);

        storage.addItem(auctionItem).thenAccept(v -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.auction.sell.success",
                    "<green>Item listed for <red>${price}</red>!",
                    ComponentPlaceholder.of("{price}", economyManager.format(price))));
        });
    }

    private void showUsage(Player player) {
        player.sendMessage(langManager.getMessageFor(player, "commands.auction.usage",
                "<yellow>Usage:</yellow> /auction [sell <price>|my]"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (!player.hasPermission(PERMISSION_BASE)) return List.of();

        if (args.length == 1) {
            return List.of("sell", "my").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}