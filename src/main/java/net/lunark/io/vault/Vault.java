package net.lunark.io.vault;

import org.bukkit.inventory.Inventory;
import java.util.UUID;

public class Vault {
    private final UUID owner;
    private final int number;
    private final Inventory inventory;

    public Vault(UUID owner, int number, Inventory inventory) {
        this.owner = owner;
        this.number = number;
        this.inventory = inventory;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getNumber() {
        return number;
    }

    public Inventory getInventory() {
        return inventory;
    }
}