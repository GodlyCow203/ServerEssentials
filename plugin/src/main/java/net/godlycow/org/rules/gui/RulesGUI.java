package net.godlycow.org.rules.gui;

import net.godlycow.org.rules.trigger.RulesListener;
import net.godlycow.org.rules.storage.RulesStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.godlycow.org.commands.config.RulesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public class RulesGUI {
    private final PlayerLanguageManager langManager;
    private final RulesStorage storage;
    private final RulesConfig config;
    private final Plugin plugin;
    private static final int GUI_SIZE = 54;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Map<UUID, Integer> playerPageMap = new ConcurrentHashMap<>();
    private static final int RULE_SLOTS_START = 10;
    private static final int RULE_SLOTS_END = 43;

    private static final int[] BORDER_SLOTS = {
            0,1,2,3,4,5,6,7,8,
            9,17,18,26,27,35,36,44,
            45,46,47,48,49,50,51,52,53
    };

    private static final int PREV_PAGE_SLOT = 46;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final int INFO_SLOT = 49;

    public RulesGUI(PlayerLanguageManager langManager, RulesStorage storage, RulesConfig config, Plugin plugin) {
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.plugin = plugin;
    }

    public void showRules(Player player) {
        showRules(player, 0);
    }

    private void showRules(Player player, int page) {
        if (isViewingRules(player)) {
            return;
        }

        playerPageMap.put(player.getUniqueId(), page);

        storage.getAllRules().thenAccept(rules -> {
            if (rules.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.rules.gui.no-rules",
                        "<red>No rules are currently configured."));
                return;
            }

            Inventory inv = createInventory(player);
            setupBorders(inv, player);
            setupInfoPanel(inv, player, rules.size(), page);

            if (config.enablePagination() && rules.size() > config.rulesPerPage()) {
                setupPaginationControls(inv, player, page, rules.size());
            }

            setupActionButtons(inv, player);
            setupRules(inv, player, rules, page);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.openInventory(inv);
                }
            });
        });
    }

    private Inventory createInventory(Player player) {
        Component titleComponent = langManager.getMessageFor(player, "commands.rules.gui.title", config.title());
        String legacyTitle = LegacyComponentSerializer.legacySection().serialize(titleComponent);
        return Bukkit.createInventory(player, GUI_SIZE, legacyTitle);
    }

    private void setupBorders(Inventory inv, Player player) {
        Material borderMat = Material.matchMaterial(config.guiBorderMaterial());
        if (borderMat == null) borderMat = Material.GRAY_STAINED_GLASS_PANE;

        ItemStack border = createBorderItem(borderMat, player);
        for (int slot : BORDER_SLOTS) {
            inv.setItem(slot, border);
        }
    }

    private void setupInfoPanel(Inventory inv, Player player, int totalRules, int currentPage) {
        ItemStack info = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = info.getItemMeta();

        Component title = langManager.getMessageFor(player, "commands.rules.gui.info-title",
                "<gold><bold>ℹ Server Rules");
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(title));

        List<String> lore = new ArrayList<>();

        Component line1 = langManager.getMessageFor(player, "commands.rules.gui.info-line1",
                "<gray>Please read and accept");
        lore.add(LegacyComponentSerializer.legacySection().serialize(line1));

        Component line2 = langManager.getMessageFor(player, "commands.rules.gui.info-line2",
                "<gray>our community guidelines");
        lore.add(LegacyComponentSerializer.legacySection().serialize(line2));

        lore.add("");

        if (config.enablePagination() && totalRules > config.rulesPerPage()) {
            int totalPages = (int) Math.ceil((double) totalRules / config.rulesPerPage());
            String pageInfo = LegacyComponentSerializer.legacySection().serialize(
                    langManager.getMessageFor(player, "commands.rules.gui.page-info",
                            "<yellow>Page <white>{current}</white> / {total}",
                            ComponentPlaceholder.of("{current}", currentPage + 1),
                            ComponentPlaceholder.of("{total}", totalPages))
            );
            lore.add(pageInfo);
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        info.setItemMeta(meta);
        inv.setItem(INFO_SLOT, info);
    }

    private void setupPaginationControls(Inventory inv, Player player, int page, int totalRules) {
        int totalPages = (int) Math.ceil((double) totalRules / config.rulesPerPage());

        Material prevMat = Material.ARROW;
        ItemStack prev = new ItemStack(prevMat);
        ItemMeta prevMeta = prev.getItemMeta();

        Component prevTitle = langManager.getMessageFor(player, "commands.rules.gui.prev-page",
                page > 0 ? "<green>← Previous Page" : "<gray>← Previous Page");
        prevMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(prevTitle));

        if (page > 0) {
            prevMeta.setLore(Collections.singletonList(
                    LegacyComponentSerializer.legacySection().serialize(
                            langManager.getMessageFor(player, "commands.rules.gui.prev-page-lore", "<gray>Click to go back")
                    )
            ));
        }

        prev.setItemMeta(prevMeta);
        inv.setItem(PREV_PAGE_SLOT, prev);

        Material nextMat = Material.ARROW;
        ItemStack next = new ItemStack(nextMat);
        ItemMeta nextMeta = next.getItemMeta();

        Component nextTitle = langManager.getMessageFor(player, "commands.rules.gui.next-page",
                page < totalPages - 1 ? "<green>Next Page →" : "<gray>Next Page →");
        nextMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(nextTitle));

        if (page < totalPages - 1) {
            nextMeta.setLore(Collections.singletonList(
                    LegacyComponentSerializer.legacySection().serialize(
                            langManager.getMessageFor(player, "commands.rules.gui.next-page-lore", "<gray>Click to continue")
                    )
            ));
        }

        next.setItemMeta(nextMeta);
        inv.setItem(NEXT_PAGE_SLOT, next);
    }

    private void setupActionButtons(Inventory inv, Player player) {Material acceptMat = Material.matchMaterial(config.acceptButtonMaterial());
        if (acceptMat == null) acceptMat = Material.LIME_CONCRETE;

        ItemStack accept = new ItemStack(acceptMat);
        ItemMeta acceptMeta = accept.getItemMeta();

        Component acceptName = langManager.getMessageFor(player, "commands.rules.gui.accept-title", config.acceptButtonText());
        acceptMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(acceptName));

        List<String> acceptLore = new ArrayList<>();
        Component acceptLore1 = langManager.getMessageFor(player, "commands.rules.gui.accept-lore1",
                "<gray>Click to accept the rules");
        acceptLore.add(LegacyComponentSerializer.legacySection().serialize(acceptLore1));
        Component acceptLore2 = langManager.getMessageFor(player, "commands.rules.gui.accept-lore2",
                "<gray>and gain access to the server");
        acceptLore.add(LegacyComponentSerializer.legacySection().serialize(acceptLore2));

        acceptMeta.setLore(acceptLore);
        acceptMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        accept.setItemMeta(acceptMeta);
        inv.setItem(config.acceptButtonSlot(), accept);

        Material declineMat = Material.matchMaterial(config.declineButtonMaterial());
        if (declineMat == null) declineMat = Material.RED_CONCRETE;

        ItemStack decline = new ItemStack(declineMat);
        ItemMeta declineMeta = decline.getItemMeta();

        Component declineName = langManager.getMessageFor(player, "commands.rules.gui.decline-title", config.declineButtonText());
        declineMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(declineName));

        List<String> declineLore = new ArrayList<>();
        Component declineLore1 = langManager.getMessageFor(player, "commands.rules.gui.decline-lore1",
                "<gray>Click to decline and");
        declineLore.add(LegacyComponentSerializer.legacySection().serialize(declineLore1));
        Component declineLore2 = langManager.getMessageFor(player, "commands.rules.gui.decline-lore2",
                "<gray>leave the server");
        declineLore.add(LegacyComponentSerializer.legacySection().serialize(declineLore2));

        declineMeta.setLore(declineLore);
        declineMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        decline.setItemMeta(declineMeta);
        inv.setItem(config.declineButtonSlot(), decline);
    }

    private void setupRules(Inventory inv, Player player, List<RulesStorage.Rule> rules, int page) {
        Material ruleMat = Material.matchMaterial(config.ruleItemMaterial());
        if (ruleMat == null) ruleMat = Material.PAPER;

        int startIndex = page * config.rulesPerPage();
        int endIndex = Math.min(startIndex + config.rulesPerPage(), rules.size());

        List<Integer> ruleSlots = getRuleDisplaySlots();

        for (int i = startIndex; i < endIndex; i++) {
            RulesStorage.Rule rule = rules.get(i);
            int slotIndex = i - startIndex;
            if (slotIndex >= ruleSlots.size()) break;

            ItemStack ruleItem = createRuleItem(player, rule, ruleMat);
            inv.setItem(ruleSlots.get(slotIndex), ruleItem);
        }

        Material fillerMat = Material.matchMaterial(config.guiFillerMaterial());
        if (fillerMat == null) fillerMat = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
        ItemStack filler = createFillerItem(fillerMat, player);

        for (int i = endIndex - startIndex; i < ruleSlots.size(); i++) {
            inv.setItem(ruleSlots.get(i), filler);
        }
    }

    private List<Integer> getRuleDisplaySlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 7; col++) {
                slots.add(10 + (row * 9) + col);
            }
        }
        return slots;
    }

    private ItemStack createRuleItem(Player player, RulesStorage.Rule rule, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        Component title = Component.text("Rule #" + (rule.orderIndex() + 1))
                .color(TextColor.color(0xFFD966))
                .decorate(TextDecoration.BOLD);

        meta.displayName(title);

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━")
                .color(TextColor.color(0xFFBF00)));
        lore.add(Component.empty());

        Component ruleText = MINI_MESSAGE.deserialize(rule.text())
                .colorIfAbsent(TextColor.color(0xC0C0C0))
                .decoration(TextDecoration.ITALIC, false);

        lore.addAll(wrapComponent(ruleText, 45));

        lore.add(Component.empty());
        lore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━")
                .color(TextColor.color(0xFFBF00)));

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setEnchantmentGlintOverride(true);

        item.setItemMeta(meta);
        return item;
    }


    private ItemStack createBorderItem(Material material, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFillerItem(Material material, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private String extractColorPrefix(String text) {
        StringBuilder prefix = new StringBuilder();
        int i = 0;
        while (i < text.length() - 1) {
            if ((text.charAt(i) == '&' || text.charAt(i) == '§') &&
                    "0123456789abcdefklmnor".indexOf(text.charAt(i + 1)) >= 0) {
                prefix.append(text.charAt(i)).append(text.charAt(i + 1));
                i += 2;
            } else {
                break;
            }
        }
        return prefix.toString();
    }

    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String cleanWord = word.replaceAll("[&§][0-9a-fklmnor]", "");

            if (currentLine.length() + cleanWord.length() > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private boolean isViewingRules(Player player) {
        String currentTitle = player.getOpenInventory().getTitle();
        Component expectedTitle = langManager.getMessageFor(player, "commands.rules.gui.title", config.title());
        String expectedLegacy = LegacyComponentSerializer.legacySection().serialize(expectedTitle);
        return currentTitle.equals(expectedLegacy);
    }

    public void handleAccept(Player player) {
        RulesListener.removePending(player.getUniqueId());
        player.closeInventory();

        storage.getLatestVersion().thenCompose(version ->
                storage.acceptRules(player.getUniqueId(), version)
        ).thenAccept(v -> {
            Component successMsg = langManager.getMessageFor(player, "commands.rules.accept.success",
                            "<green><bold>✔ You accepted the server rules!")
                    .decoration(TextDecoration.ITALIC, false);
            player.sendMessage(successMsg);
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Failed to accept rules for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    public void handleDecline(Player player) {
        RulesListener.removePending(player.getUniqueId());
        player.closeInventory();
        player.kick(langManager.getMessageFor(player, "commands.rules.decline.kick-message", config.kickMessage()));
    }

    public void handlePageChange(Player player, boolean next) {
        Integer currentPage = playerPageMap.get(player.getUniqueId());
        if (currentPage == null) return;

        storage.getAllRules().thenAccept(rules -> {
            int totalPages = (int) Math.ceil((double) rules.size() / config.rulesPerPage());
            int newPage = next ? currentPage + 1 : currentPage - 1;

            if (newPage >= 0 && newPage < totalPages) {
                showRules(player, newPage);
            }
        });
    }

    private List<Component> wrapComponent(Component component, int maxLineLength) {
        List<Component> lines = new ArrayList<>();

        String plain =
                PlainTextComponentSerializer.plainText().serialize(component);

        String[] words = plain.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() > maxLineLength) {
                lines.add(Component.text(current.toString())
                        .style(component.style()));
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(Component.text(current.toString())
                    .style(component.style()));
        }

        return lines;
    }


}

// dm me the word "Chickenburga" for aa surprise , @cow0990