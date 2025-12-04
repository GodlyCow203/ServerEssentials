package net.lunark.io.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.HomeMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HomesGUI {

    private final ServerEssentials plugin;
    private final HomeManager homeManager;
    private final HomeMessages messages;

    private final Integer[] bedSlots = {10, 12, 14, 16, 28, 30, 32, 34};
    private final Integer[] dyeSlots = {19, 21, 23, 25, 37, 39, 41, 43};

    public HomesGUI(ServerEssentials plugin, HomeManager homeManager, HomeMessages messages) {
        this.plugin = plugin;
        this.homeManager = homeManager;
        this.messages = messages;
    }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(new HomesMainHolder(), 54, messages.get("gui.title.main"));
        fillBackground(inv);

        UUID uuid = p.getUniqueId();
        int bedCount = bedSlots.length;
        int dyeCount = dyeSlots.length;

        for (int i = 0; i < Math.max(bedCount, dyeCount); i++) {
            if (i < bedCount) {
                int slot = bedSlots[i];
                int homeIndex = i + 1;
                boolean allowed = canSetHome(p, homeIndex);
                boolean hasHome = homeManager.getHome(uuid, homeIndex).isPresent();
                inv.setItem(slot, getBedForState(hasHome, allowed, homeIndex, uuid));
            }

            if (i < dyeCount) {
                int slot = dyeSlots[i];
                int homeIndex = i + 1;
                boolean removeAllowed = p.hasPermission("serveressentials.removehome");
                boolean hasHome = homeManager.getHome(uuid, homeIndex).isPresent();
                inv.setItem(slot, getDyeForState(hasHome, removeAllowed, homeIndex, uuid));
            }
        }

        p.openInventory(inv);
    }

    public Inventory createConfirmInventory(Player p, int homeIndex, String mode) {
        Inventory inv = Bukkit.createInventory(new HomesConfirmHolder(homeIndex, mode), 27, messages.get("gui.title.confirm"));

        ItemStack green = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta gm = green.getItemMeta();
        gm.displayName(messages.get("msg.confirm"));
        green.setItemMeta(gm);

        ItemStack red = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta rm = red.getItemMeta();
        rm.displayName(messages.get("msg.cancel"));
        red.setItemMeta(rm);

        inv.setItem(10, green);
        inv.setItem(16, red);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();

        List<Component> lore = new ArrayList<>();
        Optional<Home> home = homeManager.getHome(p.getUniqueId(), homeIndex);
        home.ifPresent(h -> {
            lore.add(messages.get("lore.name", "{name}", h.getName()));
            lore.add(messages.get("lore.coords",
                    "{x}", String.valueOf(Math.round(h.getX())),
                    "{y}", String.valueOf(Math.round(h.getY())),
                    "{z}", String.valueOf(Math.round(h.getZ()))
            ));
        });
        if (lore.isEmpty()) lore.add(messages.get("lore.empty"));

        im.displayName(messages.get("gui.confirm.title", "{home}", String.valueOf(homeIndex), "{mode}", mode));
        im.lore(lore);
        info.setItemMeta(im);
        inv.setItem(13, info);

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta fm = filler.getItemMeta();
                fm.displayName(messages.get("gui.filler"));
                filler.setItemMeta(fm);
                inv.setItem(i, filler);
            }
        }

        return inv;
    }

    public boolean canSetHome(Player p, int homeIndex) {
        if (!p.hasPermission("serveressentials.sethome")) return false;
        if (p.hasPermission("serveressentials.sethome.*")) return true;
        return p.hasPermission("serveressentials.sethome." + homeIndex);
    }

    private void fillBackground(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = filler.getItemMeta();
        m.displayName(messages.get("gui.filler"));
        filler.setItemMeta(m);

        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (inv.getItem(slot) == null) inv.setItem(slot, filler);
        }
    }

    private ItemStack getBedForState(boolean hasHome, boolean allowed, int homeIndex, UUID uuid) {
        Material mat;
        List<Component> lore = new ArrayList<>();

        if (!allowed) {
            mat = Material.RED_BED;
            lore.add(messages.get("msg.no-permission"));
        } else if (!hasHome) {
            mat = Material.GRAY_BED;
            lore.add(messages.get("lore.click.set"));
        } else {
            mat = Material.BLUE_BED;
            homeManager.getHome(uuid, homeIndex).ifPresent(h -> {
                lore.add(messages.get("lore.name", "{name}", h.getName()));
                lore.add(messages.get("lore.coords",
                        "{x}", String.valueOf(Math.round(h.getX())),
                        "{y}", String.valueOf(Math.round(h.getY())),
                        "{z}", String.valueOf(Math.round(h.getZ()))
                ));
                lore.add(messages.get("lore.click.teleport"));
                lore.add(messages.get("lore.click.manage"));
            });
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.get("gui.home-title", "{home}", String.valueOf(homeIndex)));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getDyeForState(boolean hasHome, boolean allowed, int homeIndex, UUID uuid) {
        Material mat;
        List<Component> lore = new ArrayList<>();

        if (!allowed) {
            mat = Material.RED_DYE;
            lore.add(messages.get("msg.no-permission"));
        } else if (!hasHome) {
            mat = Material.GRAY_DYE;
            lore.add(messages.get("lore.empty"));
            lore.add(messages.get("lore.click.set"));
        } else {
            mat = Material.LIME_DYE;
            homeManager.getHome(uuid, homeIndex).ifPresent(h -> {
                lore.add(messages.get("lore.name", "{name}", h.getName()));
                lore.add(messages.get("lore.coords",
                        "{x}", String.valueOf(Math.round(h.getX())),
                        "{y}", String.valueOf(Math.round(h.getY())),
                        "{z}", String.valueOf(Math.round(h.getZ()))
                ));
                lore.add(messages.get("lore.click.remove"));
            });
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(messages.get("gui.home-dye-title", "{home}", String.valueOf(homeIndex)));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Integer[] getBedSlots() { return bedSlots; }
    public Integer[] getDyeSlots() { return dyeSlots; }
}
