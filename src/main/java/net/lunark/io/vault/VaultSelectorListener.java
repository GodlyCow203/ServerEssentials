package net.lunark.io.vault;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.regex.Pattern;

public class VaultSelectorListener implements Listener {
    private final VaultManager vaultManager;
    private final PlayerLanguageManager langManager;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    public VaultSelectorListener(VaultManager vaultManager, PlayerLanguageManager langManager) {
        this.vaultManager = vaultManager;
        this.langManager = langManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getView().getTopInventory().getSize() != 45) return;

        String expectedTitle = PlainTextComponentSerializer.plainText()
                .serialize(langManager.getMessageFor(player, "vault.selector.title",
                        "Select a Vault"));
        String actualTitle = PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());

        if (!expectedTitle.equalsIgnoreCase(actualTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if (clicked.getType() != Material.BARREL) return;

        String plainName = PlainTextComponentSerializer.plainText()
                .serialize(clicked.getItemMeta().displayName());

        java.util.regex.Matcher matcher = NUMBER_PATTERN.matcher(plainName);
        if (!matcher.find()) return;

        int vaultNumber = Integer.parseInt(matcher.group());
        String perm = "serveressentials.command.pv." + vaultNumber;

        if (player.hasPermission(perm)) {
            player.closeInventory();
            vaultManager.openVault(player, vaultNumber);
        }
    }
}