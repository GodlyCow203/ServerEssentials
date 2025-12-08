package net.lunark.io.rules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.lunark.io.commands.config.RulesConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public class RulesGUI {
    private final PlayerLanguageManager langManager;
    private final RulesStorage storage;
    private final RulesConfig config;
    private final Plugin plugin;
    private static final int GUI_SIZE = 54;
    private final Material fillerMaterial;

    public RulesGUI(PlayerLanguageManager langManager, RulesStorage storage, RulesConfig config, Plugin plugin) {
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;
        this.fillerMaterial = Material.matchMaterial(config.guiFillerMaterial());
    }

    public void showRules(Player player) {
        if (isViewingRules(player)) {
            return;
        }

        storage.getAllRules().thenAccept(rules -> {
            if (rules.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "rules.gui.no-rules",
                        "<red>No rules are currently configured."));
                return;
            }

            Component titleComponent = langManager.getMessageFor(player, "rules.gui.title", config.title());
            String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);
            Inventory inv = Bukkit.createInventory(player, GUI_SIZE, legacyTitle);

            if (fillerMaterial != null) {
                ItemStack filler = createFiller();
                for (int i = 0; i < GUI_SIZE; i++) {
                    inv.setItem(i, filler);
                }
            }

            for (RulesStorage.Rule rule : rules) {
                int slot = rule.slot();
                if (slot >= 0 && slot < GUI_SIZE) {
                    ItemStack ruleItem = createRuleItem(player, rule);
                    inv.setItem(slot, ruleItem);
                }
            }

            inv.setItem(config.acceptButtonSlot(), createAcceptButton(player));
            inv.setItem(config.declineButtonSlot(), createDeclineButton(player));

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.openInventory(inv);
                }
            });
        });
    }

    private boolean isViewingRules(Player player) {
        String currentTitle = player.getOpenInventory().getTitle();
        Component expectedTitle = langManager.getMessageFor(player, "rules.gui.title", config.title());
        String expectedLegacy = LegacyComponentSerializer.legacySection().serialize(expectedTitle);
        return currentTitle.equals(expectedLegacy);
    }

    private ItemStack createFiller() {
        ItemStack filler = new ItemStack(fillerMaterial != null ? fillerMaterial : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        return filler;
    }

    private ItemStack createRuleItem(Player player, RulesStorage.Rule rule) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        Component nameComponent = langManager.getMessageFor(player, "rules.gui.rule-name",
                "<white><bold>Rule #{number}",
                ComponentPlaceholder.of("{number}", rule.orderIndex() + 1));
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(nameComponent));

        Component ruleComponent = langManager.getMessageFor(player, "rules.gui.rule-line", rule.text());
        String parsedText = LegacyComponentSerializer.legacySection().serialize(ruleComponent);

        List<String> lore = new ArrayList<>();
        lore.add("");

        String[] words = parsedText.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() > 35) {
                lore.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }
        if (currentLine.length() > 0) {
            lore.add(currentLine.toString());
        }

        lore.add("");
        Component hintComponent = langManager.getMessageFor(player, "rules.gui.rule-hint",
                "<gray><italic>Click accept below to continue");
        lore.add(LegacyComponentSerializer.legacySection().serialize(hintComponent));

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createAcceptButton(Player player) {
        ItemStack item = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = item.getItemMeta();
        Component nameComponent = langManager.getMessageFor(player, "rules.gui.accept-title", config.acceptButtonText());
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(nameComponent));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDeclineButton(Player player) {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();
        Component nameComponent = langManager.getMessageFor(player, "rules.gui.decline-title", config.declineButtonText());
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(nameComponent));
        item.setItemMeta(meta);
        return item;
    }

    public void handleAccept(Player player) {
        RulesListener.removePending(player.getUniqueId());
        player.closeInventory();
        storage.getLatestVersion().thenCompose(version ->
                storage.acceptRules(player.getUniqueId(), version)
        ).thenAccept(v -> {
            player.sendMessage(langManager.getMessageFor(player, "rules.accept.success",
                    "<green><bold>✓ You accepted the server rules!"));
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Failed to accept rules for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    public void handleDecline(Player player) {
        RulesListener.removePending(player.getUniqueId());
        player.closeInventory();
        player.kick(langManager.getMessageFor(player, "rules.decline.kick-message", config.kickMessage()));
    }
}