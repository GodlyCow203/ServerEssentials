package net.lunark.io.economy;

import net.lunark.io.database.DatabaseManager;
import net.lunark.io.hooks.HooksManager;
import net.lunark.io.hooks.VaultHook;
import net.lunark.io.util.AnsiColorUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class EconomyManager {
    private static EconomyManager instance;
    private final Plugin plugin;
    private final DatabaseManager dbManager;
    private final HooksManager hooksManager;
    private EconomyAPI economy;
    private boolean enabled = false;
    private String economyName = "Disabled";

    private EconomyManager(Plugin plugin, DatabaseManager dbManager, HooksManager hooksManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
        this.hooksManager = hooksManager;
    }

    public static EconomyManager getInstance(Plugin plugin, DatabaseManager dbManager, HooksManager hooksManager) {
        if (instance == null) {
            instance = new EconomyManager(plugin, dbManager, hooksManager);
        }
        return instance;
    }

    public void initialize() {
        VaultHook vault = hooksManager.getVault();

        if (vault != null && vault.hasEconomy()) {
            plugin.getLogger().info(AnsiColorUtil.success("Using Vault economy: " + vault.getEconomy().getName()));
            this.economy = new VaultEconomyAdapter(vault);
            this.economyName = vault.getEconomy().getName();
        } else {
            plugin.getLogger().info(AnsiColorUtil.warning("Using internal economy system"));
            this.economy = new InternalEconomy(plugin, dbManager);
            this.economyName = economy.getName();

            if (vault != null && hooksManager.isVaultActive()) {
                registerWithVault();
            }
        }
        this.enabled = true;
    }

    private void registerWithVault() {
        try {
            Class<?> bridgeClass = Class.forName("net.lunark.io.economy.VaultEconomyBridge");
            Object bridge = bridgeClass.getConstructor(
                    Plugin.class, InternalEconomy.class, String.class
            ).newInstance(plugin, (InternalEconomy) economy, economyName);
            bridgeClass.getMethod("register").invoke(bridge);
        } catch (Exception e) {
            plugin.getLogger().warning(AnsiColorUtil.danger("Could not register with Vault: " + e.getMessage()));
        }
    }

    public boolean isEnabled() { return enabled; }

    public EconomyAPI getEconomy() { return economy; }

    public String getEconomyName() { return economyName; }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    public EconomyResponse withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount);
    }

    public EconomyResponse deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount);
    }

    public String format(double amount) {
        return economy.format(amount);
    }

    public CompletableFuture<Boolean> hasPaymentsDisabled(String playerUuid) {
        return economy.hasPaymentsDisabled(playerUuid);
    }

    public CompletableFuture<Void> setPaymentsDisabled(String playerUuid, String playerName, boolean disabled) {
        return economy.setPaymentsDisabled(playerUuid, playerName, disabled);
    }

    public CompletableFuture<Boolean> hasPayConfirmDisabled(String playerUuid) {
        return economy.hasPayConfirmDisabled(playerUuid);
    }

    public CompletableFuture<Void> setPayConfirmDisabled(String playerUuid, String playerName, boolean disabled) {
        return economy.setPayConfirmDisabled(playerUuid, playerName, disabled);
    }
}