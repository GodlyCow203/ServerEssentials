package net.lunark.io.vault;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VaultSelectorListener implements Listener {
    private final VaultManager vaultManager;
    private final PlayerLanguageManager langManager;
    private static final String GUI_TITLE_KEY = "vault.selector.title";
    private static final Pattern VAULT_NUMBER_PATTERN = Pattern.compile("Vault\\s*#?(\\d+)");

    public VaultSelectorListener(VaultManager vaultManager, PlayerLanguageManager langManager) {
        this.vaultManager = vaultManager;
        this.langManager = langManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component expectedTitle = langManager.getMessageFor(player, GUI_TITLE_KEY, "<yellow>Select a Vault");
        Component actualTitle = event.getView().title();

        String expectedTitleText = PlainTextComponentSerializer.plainText().serialize(expectedTitle);
        String actualTitleText = PlainTextComponentSerializer.plainText().serialize(actualTitle);
        if (!expectedTitleText.equals(actualTitleText)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        Component displayNameComponent = meta.displayName();
        if (displayNameComponent == null) return;

        String plainText = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);

        Matcher matcher = VAULT_NUMBER_PATTERN.matcher(plainText);
        if (!matcher.find()) return;

        int vaultNumber = Integer.parseInt(matcher.group(1));

        if (clicked.getType() == Material.BARREL) {
            player.closeInventory();
            vaultManager.openVault(player, vaultNumber);
        }
    }
}