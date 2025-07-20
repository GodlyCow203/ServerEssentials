package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class ShopConfigManager {

    private static File shopFile;
    private static FileConfiguration shopConfig;

    // Load the shop items from shopitems.yml
    public static void loadShopItems() {
        shopFile = new File(Bukkit.getServer().getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "shopitems.yml");

        if (!shopFile.exists()) {
            shopFile.getParentFile().mkdirs();
            saveDefaultShopConfig();
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);

        if (shopConfig.getConfigurationSection("items") == null) {
            Bukkit.getLogger().warning("[ServerEssentials] No items section found in shopitems.yml!");
            return;
        }

        // Clear existing items in ShopManager before adding new ones
        ShopManager.clearItems();

        for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key;
            try {
                // Get the material from the config
                Material material = Material.matchMaterial(shopConfig.getString(path + ".material"));
                if (material == null) {
                    Bukkit.getLogger().warning("[ServerEssentials] Invalid material for item '" + key + "'!");
                    continue;
                }

                // Get other properties
                double buyPrice = shopConfig.getDouble(path + ".price");
                double sellPrice = shopConfig.getDouble(path + ".sellPrice");
                int amount = shopConfig.getInt(path + ".amount", 1);
                int slot = shopConfig.getInt(path + ".slot", 0);
                int page = shopConfig.getInt(path + ".page", 1);
                String category = shopConfig.getString(path + ".category", "Misc");

                // Get mobType if present (used for spawners)
                String mobTypeName = shopConfig.getString(path + ".mobType");
                EntityType mobType = null;
                if (mobTypeName != null) {
                    try {
                        mobType = EntityType.valueOf(mobTypeName.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        Bukkit.getLogger().warning("[ServerEssentials] Invalid mob type '" + mobTypeName + "' for item '" + key + "'");
                    }
                }

                // Create the item stack
                ItemStack item = new ItemStack(material, amount);

                // Create the ShopItem with mobType support
                ShopItem shopItem = new ShopItem(item, buyPrice, sellPrice, slot, category, page, mobType);

                // Add the item to the ShopManager
                ShopManager.addItem(shopItem);

            } catch (Exception e) {
                Bukkit.getLogger().warning("[ServerEssentials] Failed to load shop item '" + key + "': " + e.getMessage());
            }
        }
    }


    // Save a default shopitems.yml file if it doesn't exist
    private static void saveDefaultShopConfig() {
        shopConfig = new YamlConfiguration();

        // Example items for all sections
        shopConfig.set("items.book.material", "BOOK");
        shopConfig.set("items.book.price", 50);
        shopConfig.set("items.book.sellPrice", 10);
        shopConfig.set("items.book.amount", 1);
        shopConfig.set("items.book.slot", 10);
        shopConfig.set("items.book.page", 1);
        shopConfig.set("items.book.category", "Misc");

        shopConfig.set("items.clock.material", "CLOCK");
        shopConfig.set("items.clock.price", 45);
        shopConfig.set("items.clock.sellPrice", 22);
        shopConfig.set("items.clock.amount", 1);
        shopConfig.set("items.clock.slot", 12);
        shopConfig.set("items.clock.page", 1);
        shopConfig.set("items.clock.category", "Misc");

        shopConfig.set("items.nametag.material", "NAME_TAG");
        shopConfig.set("items.nametag.price", 60);
        shopConfig.set("items.nametag.sellPrice", 30);
        shopConfig.set("items.nametag.amount", 1);
        shopConfig.set("items.nametag.slot", 14);
        shopConfig.set("items.nametag.page", 1);
        shopConfig.set("items.nametag.category", "Misc");

        shopConfig.set("items.lead.material", "LEAD");
        shopConfig.set("items.lead.price", 50);
        shopConfig.set("items.lead.sellPrice", 25);
        shopConfig.set("items.lead.amount", 1);
        shopConfig.set("items.lead.slot", 14);
        shopConfig.set("items.lead.page", 1);
        shopConfig.set("items.lead.category", "Misc");

        shopConfig.set("items.saddle.material", "SADDLE");
        shopConfig.set("items.saddle.price", 55);
        shopConfig.set("items.saddle.sellPrice", 28);
        shopConfig.set("items.saddle.amount", 1);
        shopConfig.set("items.saddle.slot", 16);
        shopConfig.set("items.saddle.page", 1);
        shopConfig.set("items.saddle.category", "Misc");

        shopConfig.set("items.bookandquill.material", "WRITABLE_BOOK");
        shopConfig.set("items.bookandquill.price", 25);
        shopConfig.set("items.bookandquill.sellPrice", 12);
        shopConfig.set("items.bookandquill.amount", 1);
        shopConfig.set("items.bookandquill.slot", 19);
        shopConfig.set("items.bookandquill.page", 1);
        shopConfig.set("items.bookandquill.category", "Misc");

        shopConfig.set("items.map.material", "MAP");
        shopConfig.set("items.map.price", 30);
        shopConfig.set("items.map.sellPrice", 15);
        shopConfig.set("items.map.amount", 1);
        shopConfig.set("items.map.slot", 21);
        shopConfig.set("items.map.page", 1);
        shopConfig.set("items.map.category", "Misc");

        shopConfig.set("items.bundle.material", "BUNDLE");
        shopConfig.set("items.bundle.price", 35);
        shopConfig.set("items.bundle.sellPrice", 17);
        shopConfig.set("items.bundle.amount", 1);
        shopConfig.set("items.bundle.slot", 23);
        shopConfig.set("items.bundle.page", 1);
        shopConfig.set("items.bundle.category", "Misc");

        shopConfig.set("items.spyglass.material", "SPYGLASS");
        shopConfig.set("items.spyglass.price", 60);
        shopConfig.set("items.spyglass.sellPrice", 30);
        shopConfig.set("items.spyglass.amount", 1);
        shopConfig.set("items.spyglass.slot", 25);
        shopConfig.set("items.spyglass.page", 1);
        shopConfig.set("items.spyglass.category", "Misc");

        shopConfig.set("items.goathorn.material", "GOAT_HORN");
        shopConfig.set("items.goathorn.price", 100);
        shopConfig.set("items.goathorn.sellPrice", 50);
        shopConfig.set("items.goathorn.amount", 1);
        shopConfig.set("items.goathorn.slot", 28);
        shopConfig.set("items.goathorn.page", 1);
        shopConfig.set("items.goathorn.category", "Misc");

        shopConfig.set("items.dragonbreath.material", "DRAGON_BREATH");
        shopConfig.set("items.dragonbreath.price", 80);
        shopConfig.set("items.dragonbreath.sellPrice", 40);
        shopConfig.set("items.dragonbreath.amount", 1);
        shopConfig.set("items.dragonbreath.slot", 30);
        shopConfig.set("items.dragonbreath.page", 1);
        shopConfig.set("items.dragonbreath.category", "Misc");

        shopConfig.set("items.heartofthesea.material", "HEART_OF_THE_SEA");
        shopConfig.set("items.heartofthesea.price", 200);
        shopConfig.set("items.heartofthesea.sellPrice", 100);
        shopConfig.set("items.heartofthesea.amount", 1);
        shopConfig.set("items.heartofthesea.slot", 32);
        shopConfig.set("items.heartofthesea.page", 1);
        shopConfig.set("items.heartofthesea.category", "Misc");

        shopConfig.set("items.nautilushell.material", "NAUTILUS_SHELL");
        shopConfig.set("items.nautilushell.price", 90);
        shopConfig.set("items.nautilushell.sellPrice", 45);
        shopConfig.set("items.nautilushell.amount", 1);
        shopConfig.set("items.nautilushell.slot", 34);
        shopConfig.set("items.nautilushell.page", 1);
        shopConfig.set("items.nautilushell.category", "Misc");

        shopConfig.set("items.glassbottle.material", "GLASS_BOTTLE");
        shopConfig.set("items.glassbottle.price", 5);
        shopConfig.set("items.glassbottle.sellPrice", 2);
        shopConfig.set("items.glassbottle.amount", 1);
        shopConfig.set("items.glassbottle.slot", 10);
        shopConfig.set("items.glassbottle.page", 2);
        shopConfig.set("items.glassbottle.category", "Misc");

        shopConfig.set("items.honeybottle.material", "HONEY_BOTTLE");
        shopConfig.set("items.honeybottle.price", 15);
        shopConfig.set("items.honeybottle.sellPrice", 7);
        shopConfig.set("items.honeybottle.amount", 1);
        shopConfig.set("items.honeybottle.slot", 12);
        shopConfig.set("items.honeybottle.page", 2);
        shopConfig.set("items.honeybottle.category", "Misc");

        shopConfig.set("items.glowinksac.material", "GLOW_INK_SAC");
        shopConfig.set("items.glowinksac.price", 20);
        shopConfig.set("items.glowinksac.sellPrice", 10);
        shopConfig.set("items.glowinksac.amount", 1);
        shopConfig.set("items.glowinksac.slot", 12);
        shopConfig.set("items.glowinksac.page", 2);
        shopConfig.set("items.glowinksac.category", "Misc");

        shopConfig.set("items.inksac.material", "INK_SAC");
        shopConfig.set("items.inksac.price", 10);
        shopConfig.set("items.inksac.sellPrice", 5);
        shopConfig.set("items.inksac.amount", 1);
        shopConfig.set("items.inksac.slot", 14);
        shopConfig.set("items.inksac.page", 2);
        shopConfig.set("items.inksac.category", "Misc");

        shopConfig.set("items.snowball.material", "SNOWBALL");
        shopConfig.set("items.snowball.price", 5);
        shopConfig.set("items.snowball.sellPrice", 2);
        shopConfig.set("items.snowball.amount", 1);
        shopConfig.set("items.snowball.slot", 16);
        shopConfig.set("items.snowball.page", 2);
        shopConfig.set("items.snowball.category", "Misc");

        shopConfig.set("items.egg1.material", "EGG");
        shopConfig.set("items.egg1.price", 5);
        shopConfig.set("items.egg1.sellPrice", 2);
        shopConfig.set("items.egg1.amount", 1);
        shopConfig.set("items.egg1.slot", 19);
        shopConfig.set("items.egg1.page", 2);
        shopConfig.set("items.egg1.category", "Misc");

        shopConfig.set("items.slimeball.material", "SLIME_BALL");
        shopConfig.set("items.slimeball.price", 15);
        shopConfig.set("items.slimeball.sellPrice", 7);
        shopConfig.set("items.slimeball.amount", 1);
        shopConfig.set("items.slimeball.slot", 21);
        shopConfig.set("items.slimeball.page", 2);
        shopConfig.set("items.slimeball.category", "Misc");

        shopConfig.set("items.rabbitfoot.material", "RABBIT_FOOT");
        shopConfig.set("items.rabbitfoot.price", 25);
        shopConfig.set("items.rabbitfoot.sellPrice", 12);
        shopConfig.set("items.rabbitfoot.amount", 1);
        shopConfig.set("items.rabbitfoot.slot", 23);
        shopConfig.set("items.rabbitfoot.page", 2);
        shopConfig.set("items.rabbitfoot.category", "Misc");


        shopConfig.set("items.stonepickaxe.material", "STONE_PICKAXE");
        shopConfig.set("items.stonepickaxe.price", 50);
        shopConfig.set("items.stonepickaxe.sellPrice", 12);
        shopConfig.set("items.stonepickaxe.amount", 1);
        shopConfig.set("items.stonepickaxe.slot", 10);
        shopConfig.set("items.stonepickaxe.page", 1);
        shopConfig.set("items.stonepickaxe.category", "Tools");

        shopConfig.set("items.ironpickaxe.material", "IRON_PICKAXE");
        shopConfig.set("items.ironpickaxe.price", 150);
        shopConfig.set("items.ironpickaxe.sellPrice", 40);
        shopConfig.set("items.ironpickaxe.amount", 1);
        shopConfig.set("items.ironpickaxe.slot", 12);
        shopConfig.set("items.ironpickaxe.page", 1);
        shopConfig.set("items.ironpickaxe.category", "Tools");

        shopConfig.set("items.diamondpickaxe.material", "DIAMOND_PICKAXE");
        shopConfig.set("items.diamondpickaxe.price", 800);
        shopConfig.set("items.diamondpickaxe.sellPrice", 250);
        shopConfig.set("items.diamondpickaxe.amount", 1);
        shopConfig.set("items.diamondpickaxe.slot", 14);
        shopConfig.set("items.diamondpickaxe.page", 1);
        shopConfig.set("items.diamondpickaxe.category", "Tools");

        shopConfig.set("items.netheritepickaxe.material", "NETHERITE_PICKAXE");
        shopConfig.set("items.netheritepickaxe.price", 3000);
        shopConfig.set("items.netheritepickaxe.sellPrice", 900);
        shopConfig.set("items.netheritepickaxe.amount", 1);
        shopConfig.set("items.netheritepickaxe.slot", 16);
        shopConfig.set("items.netheritepickaxe.page", 1);
        shopConfig.set("items.netheritepickaxe.category", "Tools");

        shopConfig.set("items.stoneaxe.material", "STONE_AXE");
        shopConfig.set("items.stoneaxe.price", 50);
        shopConfig.set("items.stoneaxe.sellPrice", 12);
        shopConfig.set("items.stoneaxe.amount", 1);
        shopConfig.set("items.stoneaxe.slot", 19);
        shopConfig.set("items.stoneaxe.page", 1);
        shopConfig.set("items.stoneaxe.category", "Tools");

        shopConfig.set("items.ironaxe.material", "IRON_AXE");
        shopConfig.set("items.ironaxe.price", 150);
        shopConfig.set("items.ironaxe.sellPrice", 40);
        shopConfig.set("items.ironaxe.amount", 1);
        shopConfig.set("items.ironaxe.slot", 21);
        shopConfig.set("items.ironaxe.page", 1);
        shopConfig.set("items.ironaxe.category", "Tools");

        shopConfig.set("items.diamondaxe.material", "DIAMOND_AXE");
        shopConfig.set("items.diamondaxe.price", 800);
        shopConfig.set("items.diamondaxe.sellPrice", 250);
        shopConfig.set("items.diamondaxe.amount", 1);
        shopConfig.set("items.diamondaxe.slot", 23);
        shopConfig.set("items.diamondaxe.page", 1);
        shopConfig.set("items.diamondaxe.category", "Tools");

        shopConfig.set("items.netheriteaxe.material", "NETHERITE_AXE");
        shopConfig.set("items.netheriteaxe.price", 3000);
        shopConfig.set("items.netheriteaxe.sellPrice", 900);
        shopConfig.set("items.netheriteaxe.amount", 1);
        shopConfig.set("items.netheriteaxe.slot", 25);
        shopConfig.set("items.netheriteaxe.page", 1);
        shopConfig.set("items.netheriteaxe.category", "Tools");


        shopConfig.set("items.stoneshovel.material", "STONE_SHOVEL");
        shopConfig.set("items.stoneshovel.price", 50);
        shopConfig.set("items.stoneshovel.sellPrice", 8);
        shopConfig.set("items.stoneshovel.amount", 1);
        shopConfig.set("items.stoneshovel.slot", 28);
        shopConfig.set("items.stoneshovel.page", 1);
        shopConfig.set("items.stoneshovel.category", "Tools");

        shopConfig.set("items.ironshovel.material", "IRON_SHOVEL");
        shopConfig.set("items.ironshovel.price", 80);
        shopConfig.set("items.ironshovel.sellPrice", 20);
        shopConfig.set("items.ironshovel.amount", 1);
        shopConfig.set("items.ironshovel.slot", 30);
        shopConfig.set("items.ironshovel.page", 1);
        shopConfig.set("items.ironshovel.category", "Tools");

        shopConfig.set("items.diamondshovel.material", "DIAMOND_SHOVEL");
        shopConfig.set("items.diamondshovel.price", 400);
        shopConfig.set("items.diamondshovel.sellPrice", 100);
        shopConfig.set("items.diamondshovel.amount", 1);
        shopConfig.set("items.diamondshovel.slot", 32);
        shopConfig.set("items.diamondshovel.page", 1);
        shopConfig.set("items.diamondshovel.category", "Tools");

        shopConfig.set("items.netheriteshovel.material", "NETHERITE_SHOVEL");
        shopConfig.set("items.netheriteshovel.price", 3000);
        shopConfig.set("items.netheriteshovel.sellPrice", 400);
        shopConfig.set("items.netheriteshovel.amount", 1);
        shopConfig.set("items.netheriteshovel.slot", 34);
        shopConfig.set("items.netheriteshovel.page", 1);
        shopConfig.set("items.netheriteshovel.category", "Tools");

        shopConfig.set("items.shears.material", "SHEARS");
        shopConfig.set("items.shears.price", 60);
        shopConfig.set("items.shears.sellPrice", 15);
        shopConfig.set("items.shears.amount", 1);
        shopConfig.set("items.shears.slot", 10);
        shopConfig.set("items.shears.page", 2);
        shopConfig.set("items.shears.category", "Tools");

        shopConfig.set("items.flintandsteal.material", "FLINT_AND_STEEL");
        shopConfig.set("items.flintandsteal.price", 50);
        shopConfig.set("items.flintandsteal.sellPrice", 10);
        shopConfig.set("items.flintandsteal.amount", 1);
        shopConfig.set("items.flintandsteal.slot", 12);
        shopConfig.set("items.flintandsteal.page", 2);
        shopConfig.set("items.flintandsteal.category", "Tools");

        shopConfig.set("items.fishingrod.material", "FISHING_ROD");
        shopConfig.set("items.fishingrod.price", 70);
        shopConfig.set("items.fishingrod.sellPrice", 15);
        shopConfig.set("items.fishingrod.amount", 1);
        shopConfig.set("items.fishingrod.slot", 14);
        shopConfig.set("items.fishingrod.page", 2);
        shopConfig.set("items.fishingrod.category", "Tools");

        shopConfig.set("items.compass.material", "COMPASS");
        shopConfig.set("items.compass.price", 100);
        shopConfig.set("items.compass.sellPrice", 25);
        shopConfig.set("items.compass.amount", 1);
        shopConfig.set("items.compass.slot", 16);
        shopConfig.set("items.compass.page", 2);
        shopConfig.set("items.compass.category", "Tools");

        shopConfig.set("items.waterbucket.material", "WATER_BUCKET");
        shopConfig.set("items.waterbucket.price", 70);
        shopConfig.set("items.waterbucket.sellPrice", 15);
        shopConfig.set("items.waterbucket.amount", 1);
        shopConfig.set("items.waterbucket.slot", 19);
        shopConfig.set("items.waterbucket.page", 2);
        shopConfig.set("items.waterbucket.category", "Tools");

        shopConfig.set("items.endstone.material", "END_STONE");
        shopConfig.set("items.endstone.price", 12);
        shopConfig.set("items.endstone.sellPrice", 4);
        shopConfig.set("items.endstone.amount", 1);
        shopConfig.set("items.endstone.slot", 10);
        shopConfig.set("items.endstone.page", 1);
        shopConfig.set("items.endstone.category", "End");

        shopConfig.set("items.endstonebricks.material", "END_STONE_BRICKS");
        shopConfig.set("items.endstonebricks.price", 16);
        shopConfig.set("items.endstonebricks.sellPrice", 6);
        shopConfig.set("items.endstonebricks.amount", 1);
        shopConfig.set("items.endstonebricks.slot", 12);
        shopConfig.set("items.endstonebricks.page", 1);
        shopConfig.set("items.endstonebricks.category", "End");

        shopConfig.set("items.purpurblock.material", "PURPUR_BLOCK");
        shopConfig.set("items.purpurblock.price", 20);
        shopConfig.set("items.purpurblock.sellPrice", 7);
        shopConfig.set("items.purpurblock.amount", 1);
        shopConfig.set("items.purpurblock.slot", 14);
        shopConfig.set("items.purpurblock.page", 1);
        shopConfig.set("items.purpurblock.category", "End");

        shopConfig.set("items.purpurpillar.material", "PURPUR_PILLAR");
        shopConfig.set("items.purpurpillar.price", 24);
        shopConfig.set("items.purpurpillar.sellPrice", 8);
        shopConfig.set("items.purpurpillar.amount", 1);
        shopConfig.set("items.purpurpillar.slot", 16);
        shopConfig.set("items.purpurpillar.page", 1);
        shopConfig.set("items.purpurpillar.category", "End");

        shopConfig.set("items.purpurslab.material", "PURPUR_SLAB");
        shopConfig.set("items.purpurslab.price", 6);
        shopConfig.set("items.purpurslab.sellPrice", 2);
        shopConfig.set("items.purpurslab.amount", 1);
        shopConfig.set("items.purpurslab.slot", 19);
        shopConfig.set("items.purpurslab.page", 1);
        shopConfig.set("items.purpurslab.category", "End");

        shopConfig.set("items.purourstairs.material", "PURPUR_STAIRS");
        shopConfig.set("items.purourstairs.price", 10);
        shopConfig.set("items.purourstairs.sellPrice", 3);
        shopConfig.set("items.purourstairs.amount", 1);
        shopConfig.set("items.purourstairs.slot", 21);
        shopConfig.set("items.purourstairs.page", 1);
        shopConfig.set("items.purourstairs.category", "End");

        shopConfig.set("items.chorusflower.material", "CHORUS_FLOWER");
        shopConfig.set("items.chorusflower.price", 30);
        shopConfig.set("items.chorusflower.sellPrice", 10);
        shopConfig.set("items.chorusflower.amount", 1);
        shopConfig.set("items.chorusflower.slot", 23);
        shopConfig.set("items.chorusflower.page", 1);
        shopConfig.set("items.chorusflower.category", "End");

        shopConfig.set("items.chorusfruit.material", "CHORUS_FRUIT");
        shopConfig.set("items.chorusfruit.price", 12);
        shopConfig.set("items.chorusfruit.sellPrice", 4);
        shopConfig.set("items.chorusfruit.amount", 1);
        shopConfig.set("items.chorusfruit.slot", 25);
        shopConfig.set("items.chorusfruit.page", 1);
        shopConfig.set("items.chorusfruit.category", "End");

        shopConfig.set("items.poppedchorusfruit.material", "POPPED_CHORUS_FRUIT");
        shopConfig.set("items.poppedchorusfruit.price", 16);
        shopConfig.set("items.poppedchorusfruit.sellPrice", 5);
        shopConfig.set("items.poppedchorusfruit.amount", 1);
        shopConfig.set("items.poppedchorusfruit.slot", 28);
        shopConfig.set("items.poppedchorusfruit.page", 1);
        shopConfig.set("items.poppedchorusfruit.category", "End");

        shopConfig.set("items.dragonhead.material", "DRAGON_HEAD");
        shopConfig.set("items.dragonhead.price", 5000);
        shopConfig.set("items.dragonhead.sellPrice", 1500);
        shopConfig.set("items.dragonhead.amount", 1);
        shopConfig.set("items.dragonhead.slot", 30);
        shopConfig.set("items.dragonhead.page", 1);
        shopConfig.set("items.dragonhead.category", "End");

        shopConfig.set("items.endrod.material", "END_ROD");
        shopConfig.set("items.endrod.price", 18);
        shopConfig.set("items.endrod.sellPrice", 6);
        shopConfig.set("items.endrod.amount", 1);
        shopConfig.set("items.endrod.slot", 32);
        shopConfig.set("items.endrod.page", 1);
        shopConfig.set("items.endrod.category", "End");

        shopConfig.set("items.elytra.material", "ELYTRA");
        shopConfig.set("items.elytra.price", 15000);
        shopConfig.set("items.elytra.sellPrice", 3200);
        shopConfig.set("items.elytra.amount", 1);
        shopConfig.set("items.elytra.slot", 34);
        shopConfig.set("items.elytra.page", 1);
        shopConfig.set("items.elytra.category", "End");

        shopConfig.set("items.enderpearl.material", "ENDER_PEARL");
        shopConfig.set("items.enderpearl.price", 8);
        shopConfig.set("items.enderpearl.sellPrice", 2);
        shopConfig.set("items.enderpearl.amount", 1);
        shopConfig.set("items.enderpearl.slot", 10);
        shopConfig.set("items.enderpearl.page", 2);
        shopConfig.set("items.enderpearl.category", "End");

        shopConfig.set("items.endcrystal.material", "END_CRYSTAL");
        shopConfig.set("items.endcrystal.price", 1500);
        shopConfig.set("items.endcrystal.sellPrice", 400);
        shopConfig.set("items.endcrystal.amount", 1);
        shopConfig.set("items.endcrystal.slot", 12);
        shopConfig.set("items.endcrystal.page", 2);
        shopConfig.set("items.endcrystal.category", "End");

        shopConfig.set("items.shulkershell.material", "SHULKER_SHELL");
        shopConfig.set("items.shulkershell.price", 600);
        shopConfig.set("items.shulkershell.sellPrice", 180);
        shopConfig.set("items.shulkershell.amount", 1);
        shopConfig.set("items.shulkershell.slot", 14);
        shopConfig.set("items.shulkershell.page", 2);
        shopConfig.set("items.shulkershell.category", "End");

        shopConfig.set("items.shulkerbox.material", "SHULKER_BOX");
        shopConfig.set("items.shulkerbox.price", 1000);
        shopConfig.set("items.shulkerbox.sellPrice", 250);
        shopConfig.set("items.shulkerbox.amount", 1);
        shopConfig.set("items.shulkerbox.slot", 16);
        shopConfig.set("items.shulkerbox.page", 2);
        shopConfig.set("items.shulkerbox.category", "End");

        shopConfig.set("items.Coal.material", "COAL");
        shopConfig.set("items.Coal.price", 8);
        shopConfig.set("items.Coal.sellPrice", 2);
        shopConfig.set("items.Coal.amount", 1);
        shopConfig.set("items.Coal.slot", 10);
        shopConfig.set("items.Coal.page", 1);
        shopConfig.set("items.Coal.category", "Ores");

        shopConfig.set("items.charcoal.material", "CHARCOAL");
        shopConfig.set("items.charcoal.price", 8);
        shopConfig.set("items.charcoal.sellPrice", 2);
        shopConfig.set("items.charcoal.amount", 1);
        shopConfig.set("items.charcoal.slot", 12);
        shopConfig.set("items.charcoal.page", 1);
        shopConfig.set("items.charcoal.category", "Ores");

        shopConfig.set("items.ironingot.material", "IRON_INGOT");
        shopConfig.set("items.ironingot.price", 20);
        shopConfig.set("items.ironingot.sellPrice", 6);
        shopConfig.set("items.ironingot.amount", 1);
        shopConfig.set("items.ironingot.slot", 14);
        shopConfig.set("items.ironingot.page", 1);
        shopConfig.set("items.ironingot.category", "Ores");

        shopConfig.set("items.goldingot.material", "GOLD_INGOT");
        shopConfig.set("items.goldingot.price", 30);
        shopConfig.set("items.goldingot.sellPrice", 10);
        shopConfig.set("items.goldingot.amount", 1);
        shopConfig.set("items.goldingot.slot", 16);
        shopConfig.set("items.goldingot.page", 1);
        shopConfig.set("items.goldingot.category", "Ores");

        shopConfig.set("items.copperingot.material", "COPPER_INGOT");
        shopConfig.set("items.copperingot.price", 15);
        shopConfig.set("items.copperingot.sellPrice", 5);
        shopConfig.set("items.copperingot.amount", 1);
        shopConfig.set("items.copperingot.slot", 19);
        shopConfig.set("items.copperingot.page", 1);
        shopConfig.set("items.copperingot.category", "Ores");

        shopConfig.set("items.scrap.material", "NETHERITE_SCRAP");
        shopConfig.set("items.scrap.price", 500);
        shopConfig.set("items.scrap.sellPrice", 120);
        shopConfig.set("items.scrap.amount", 1);
        shopConfig.set("items.scrap.slot", 21);
        shopConfig.set("items.scrap.page", 1);
        shopConfig.set("items.scrap.category", "Ores");

        shopConfig.set("items.Lapislazuli.material", "LAPIS_LAZULI");
        shopConfig.set("items.Lapislazuli.price", 10);
        shopConfig.set("items.Lapislazuli.sellPrice", 3);
        shopConfig.set("items.Lapislazuli.amount", 1);
        shopConfig.set("items.Lapislazuli.slot", 23);
        shopConfig.set("items.Lapislazuli.page", 1);
        shopConfig.set("items.Lapislazuli.category", "Ores");

        shopConfig.set("items.emerald.material", "EMERALD");
        shopConfig.set("items.emerald.price", 60);
        shopConfig.set("items.emerald.sellPrice", 20);
        shopConfig.set("items.emerald.amount", 1);
        shopConfig.set("items.emerald.slot", 25);
        shopConfig.set("items.emerald.page", 1);
        shopConfig.set("items.emerald.category", "Ores");

        shopConfig.set("items.diamond.material", "DIAMOND");
        shopConfig.set("items.diamond.price", 150);
        shopConfig.set("items.diamond.sellPrice", 50);
        shopConfig.set("items.diamond.amount", 1);
        shopConfig.set("items.diamond.slot", 28);
        shopConfig.set("items.diamond.page", 1);
        shopConfig.set("items.diamond.category", "Ores");

        shopConfig.set("items.quartz1.material", "QUARTZ");
        shopConfig.set("items.quartz1.price", 12);
        shopConfig.set("items.quartz1.sellPrice", 4);
        shopConfig.set("items.quartz1.amount", 1);
        shopConfig.set("items.quartz1.slot", 30);
        shopConfig.set("items.quartz1.page", 1);
        shopConfig.set("items.quartz1.category", "Ores");

        shopConfig.set("items.amethystshard.material", "AMETHYST_SHARD");
        shopConfig.set("items.amethystshard.price", 25);
        shopConfig.set("items.amethystshard.sellPrice", 8);
        shopConfig.set("items.amethystshard.amount", 1);
        shopConfig.set("items.amethystshard.slot", 32);
        shopConfig.set("items.amethystshard.page", 1);
        shopConfig.set("items.amethystshard.category", "Ores");

        shopConfig.set("items.redstone1.material", "REDSTONE");
        shopConfig.set("items.redstone1.price", 30);
        shopConfig.set("items.redstone1.sellPrice", 9);
        shopConfig.set("items.redstone1.amount", 1);
        shopConfig.set("items.redstone1.slot", 34);
        shopConfig.set("items.redstone1.page", 1);
        shopConfig.set("items.redstone1.category", "Ores");


        shopConfig.set("items.rottenflesh.material", "ROTTEN_FLESH");
        shopConfig.set("items.rottenflesh.price", 2);
        shopConfig.set("items.rottenflesh.sellPrice", 0.5);
        shopConfig.set("items.rottenflesh.amount", 1);
        shopConfig.set("items.rottenflesh.slot", 10);
        shopConfig.set("items.rottenflesh.page", 1);
        shopConfig.set("items.rottenflesh.category", "Mob Drops");

        shopConfig.set("items.bone.material", "BONE");
        shopConfig.set("items.bone.price", 5);
        shopConfig.set("items.bone.sellPrice", 1.5);
        shopConfig.set("items.bone.amount", 1);
        shopConfig.set("items.bone.slot", 12);
        shopConfig.set("items.bone.page", 1);
        shopConfig.set("items.bone.category", "Mob Drops");

        shopConfig.set("items.arrow.material", "ARROW");
        shopConfig.set("items.arrow.price", 4);
        shopConfig.set("items.arrow.sellPrice", 1);
        shopConfig.set("items.arrow.amount", 1);
        shopConfig.set("items.arrow.slot", 14);
        shopConfig.set("items.arrow.page", 1);
        shopConfig.set("items.arrow.category", "Mob Drops");

        shopConfig.set("items.string.material", "STRING");
        shopConfig.set("items.string.price", 6);
        shopConfig.set("items.string.sellPrice", 2);
        shopConfig.set("items.string.amount", 1);
        shopConfig.set("items.string.slot", 16);
        shopConfig.set("items.string.page", 1);
        shopConfig.set("items.string.category", "Mob Drops");

        shopConfig.set("items.spidereye.material", "SPIDER_EYE");
        shopConfig.set("items.spidereye.price", 8);
        shopConfig.set("items.spidereye.sellPrice", 3);
        shopConfig.set("items.spidereye.amount", 1);
        shopConfig.set("items.spidereye.slot", 19);
        shopConfig.set("items.spidereye.page", 1);
        shopConfig.set("items.spidereye.category", "Mob Drops");

        shopConfig.set("items.gunpowder.material", "GUNPOWDER");
        shopConfig.set("items.gunpowder.price", 15);
        shopConfig.set("items.gunpowder.sellPrice", 5);
        shopConfig.set("items.gunpowder.amount", 1);
        shopConfig.set("items.gunpowder.slot", 21);
        shopConfig.set("items.gunpowder.page", 1);
        shopConfig.set("items.gunpowder.category", "Mob Drops");

        shopConfig.set("items.enderperal1.material", "ENDER_PEARL");
        shopConfig.set("items.enderperal1.price", 8);
        shopConfig.set("items.enderperal1.sellPrice", 2);
        shopConfig.set("items.enderperal1.amount", 1);
        shopConfig.set("items.enderperal1.slot", 23);
        shopConfig.set("items.enderperal1.page", 1);
        shopConfig.set("items.enderperal1.category", "Mob Drops");

        shopConfig.set("items.slimeball.material", "SLIME_BALL");
        shopConfig.set("items.slimeball.price", 25);
        shopConfig.set("items.slimeball.sellPrice", 8);
        shopConfig.set("items.slimeball.amount", 1);
        shopConfig.set("items.slimeball.slot", 23);
        shopConfig.set("items.slimeball.page", 1);
        shopConfig.set("items.slimeball.category", "Mob Drops");

        shopConfig.set("items.phantommembrane.material", "PHANTOM_MEMBRANE");
        shopConfig.set("items.phantommembrane.price", 80);
        shopConfig.set("items.phantommembrane.sellPrice", 25);
        shopConfig.set("items.phantommembrane.amount", 1);
        shopConfig.set("items.phantommembrane.slot", 25);
        shopConfig.set("items.phantommembrane.page", 1);
        shopConfig.set("items.phantommembrane.category", "Mob Drops");

        shopConfig.set("items.blazerod.material", "BLAZE_ROD");
        shopConfig.set("items.blazerod.price", 50);
        shopConfig.set("items.blazerod.sellPrice", 15);
        shopConfig.set("items.blazerod.amount", 1);
        shopConfig.set("items.blazerod.slot", 28);
        shopConfig.set("items.blazerod.page", 1);
        shopConfig.set("items.blazerod.category", "Mob Drops");

        shopConfig.set("items.ghasttear.material", "GHAST_TEAR");
        shopConfig.set("items.ghasttear.price", 150);
        shopConfig.set("items.ghasttear.sellPrice", 50);
        shopConfig.set("items.ghasttear.amount", 1);
        shopConfig.set("items.ghasttear.slot", 30);
        shopConfig.set("items.ghasttear.page", 1);
        shopConfig.set("items.ghasttear.category", "Mob Drops");

        shopConfig.set("items.magmacream.material", "MAGMA_CREAM");
        shopConfig.set("items.magmacream.price", 30);
        shopConfig.set("items.magmacream.sellPrice", 9);
        shopConfig.set("items.magmacream.amount", 1);
        shopConfig.set("items.magmacream.slot", 32);
        shopConfig.set("items.magmacream.page", 1);
        shopConfig.set("items.magmacream.category", "Mob Drops");

        shopConfig.set("items.whiterskeletonskull.material", "WITHER_SKELETON_SKULL");
        shopConfig.set("items.whiterskeletonskull.price", 1000);
        shopConfig.set("items.whiterskeletonskull.sellPrice", 300);
        shopConfig.set("items.whiterskeletonskull.amount", 1);
        shopConfig.set("items.whiterskeletonskull.slot", 34);
        shopConfig.set("items.whiterskeletonskull.page", 1);
        shopConfig.set("items.whiterskeletonskull.category", "Mob Drops");

        shopConfig.set("items.shulkershell1.material", "SHULKER_SHELL");
        shopConfig.set("items.shulkershell1.price", 600);
        shopConfig.set("items.shulkershell1.sellPrice", 180);
        shopConfig.set("items.shulkershell1.amount", 1);
        shopConfig.set("items.shulkershell1.slot", 10);
        shopConfig.set("items.shulkershell1.page", 2);
        shopConfig.set("items.shulkershell1.category", "Mob Drops");

        shopConfig.set("items.rabbitfoot1.material", "RABBIT_FOOT");
        shopConfig.set("items.rabbitfoot1.price", 60);
        shopConfig.set("items.rabbitfoot1.sellPrice", 20);
        shopConfig.set("items.rabbitfoot1.amount", 1);
        shopConfig.set("items.rabbitfoot1.slot", 12);
        shopConfig.set("items.rabbitfoot1.page", 2);
        shopConfig.set("items.rabbitfoot1.category", "Mob Drops");

        shopConfig.set("items.leather.material", "LEATHER");
        shopConfig.set("items.leather.price", 10);
        shopConfig.set("items.leather.sellPrice", 3);
        shopConfig.set("items.leather.amount", 1);
        shopConfig.set("items.leather.slot", 14);
        shopConfig.set("items.leather.page", 2);
        shopConfig.set("items.leather.category", "Mob Drops");

        shopConfig.set("items.feather.material", "FEATHER");
        shopConfig.set("items.feather.price", 5);
        shopConfig.set("items.feather.sellPrice", 1.5);
        shopConfig.set("items.feather.amount", 1);
        shopConfig.set("items.feather.slot", 16);
        shopConfig.set("items.feather.page", 2);
        shopConfig.set("items.feather.category", "Mob Drops");

        shopConfig.set("items.inksac1.material", "INK_SAC");
        shopConfig.set("items.inksac1.price", 10);
        shopConfig.set("items.inksac1.sellPrice", 5);
        shopConfig.set("items.inksac1.amount", 1);
        shopConfig.set("items.inksac1.slot", 19);
        shopConfig.set("items.inksac1.page", 2);
        shopConfig.set("items.inksac1.category", "Mob Drops");

        shopConfig.set("items.glowinksac1.material", "GLOW_INK_SAC");
        shopConfig.set("items.glowinksac1.price", 20);
        shopConfig.set("items.glowinksac1.sellPrice", 10);
        shopConfig.set("items.glowinksac1.amount", 1);
        shopConfig.set("items.glowinksac1.slot", 21);
        shopConfig.set("items.glowinksac1.page", 2);
        shopConfig.set("items.glowinksac1.category", "Mob Drops");

        shopConfig.set("items.scute.material", "TURTLE_SCUTE");
        shopConfig.set("items.scute.price", 200);
        shopConfig.set("items.scute.sellPrice", 60);
        shopConfig.set("items.scute.amount", 1);
        shopConfig.set("items.scute.slot", 23);
        shopConfig.set("items.scute.page", 2);
        shopConfig.set("items.scute.category", "Mob Drops");

        shopConfig.set("items.honeycomb.material", "HONEYCOMB");
        shopConfig.set("items.honeycomb.price", 25);
        shopConfig.set("items.honeycomb.sellPrice", 10);
        shopConfig.set("items.honeycomb.amount", 1);
        shopConfig.set("items.honeycomb.slot", 25);
        shopConfig.set("items.honeycomb.page", 2);
        shopConfig.set("items.honeycomb.category", "Mob Drops");



        shopConfig.set("items.stonesword.material", "STONE_SWORD");
        shopConfig.set("items.stonesword.price", 15);
        shopConfig.set("items.stonesword.sellPrice", 5);
        shopConfig.set("items.stonesword.amount", 1);
        shopConfig.set("items.stonesword.slot", 10);
        shopConfig.set("items.stonesword.page", 1);
        shopConfig.set("items.stonesword.category", "Combat");

        shopConfig.set("items.ironsword.material", "IRON_SWORD");
        shopConfig.set("items.ironsword.price", 140);
        shopConfig.set("items.ironsword.sellPrice", 45);
        shopConfig.set("items.ironsword.amount", 1);
        shopConfig.set("items.ironsword.slot", 12);
        shopConfig.set("items.ironsword.page", 1);
        shopConfig.set("items.ironsword.category", "Combat");

        shopConfig.set("items.diamondsword.material", "DIAMOND_SWORD");
        shopConfig.set("items.diamondsword.price", 400);
        shopConfig.set("items.diamondsword.sellPrice", 180);
        shopConfig.set("items.diamondsword.amount", 1);
        shopConfig.set("items.diamondsword.slot", 14);
        shopConfig.set("items.diamondsword.page", 1);
        shopConfig.set("items.diamondsword.category", "Combat");

        shopConfig.set("items.netheritesword.material", "NETHERITE_SWORD");
        shopConfig.set("items.netheritesword.price", 2500);
        shopConfig.set("items.netheritesword.sellPrice", 340);
        shopConfig.set("items.netheritesword.amount", 1);
        shopConfig.set("items.netheritesword.slot", 16);
        shopConfig.set("items.netheritesword.page", 1);
        shopConfig.set("items.netheritesword.category", "Combat");

        shopConfig.set("items.bow.material", "BOW");
        shopConfig.set("items.bow.price", 150);
        shopConfig.set("items.bow.sellPrice", 30);
        shopConfig.set("items.bow.amount", 1);
        shopConfig.set("items.bow.slot", 19);
        shopConfig.set("items.bow.page", 1);
        shopConfig.set("items.bow.category", "Combat");

        shopConfig.set("items.crossbow.material", "CROSSBOW");
        shopConfig.set("items.crossbow.price", 160);
        shopConfig.set("items.crossbow.sellPrice", 40);
        shopConfig.set("items.crossbow.amount", 1);
        shopConfig.set("items.crossbow.slot", 21);
        shopConfig.set("items.crossbow.page", 1);
        shopConfig.set("items.crossbow.category", "Combat");

        shopConfig.set("items.trident.material", "TRIDENT");
        shopConfig.set("items.trident.price", 500);
        shopConfig.set("items.trident.sellPrice", 150);
        shopConfig.set("items.trident.amount", 1);
        shopConfig.set("items.trident.slot", 23);
        shopConfig.set("items.trident.page", 1);
        shopConfig.set("items.trident.category", "Combat");

        shopConfig.set("items.shield.material", "SHIELD");
        shopConfig.set("items.shield.price", 230);
        shopConfig.set("items.shield.sellPrice", 70);
        shopConfig.set("items.shield.amount", 1);
        shopConfig.set("items.shield.slot", 25);
        shopConfig.set("items.shield.page", 1);
        shopConfig.set("items.shield.category", "Combat");

        shopConfig.set("items.chainmailboots.material", "CHAINMAIL_BOOTS");
        shopConfig.set("items.chainmailboots.price", 180);
        shopConfig.set("items.chainmailboots.sellPrice", 45);
        shopConfig.set("items.chainmailboots.amount", 1);
        shopConfig.set("items.chainmailboots.slot", 28);
        shopConfig.set("items.chainmailboots.page", 1);
        shopConfig.set("items.chainmailboots.category", "Combat");

        shopConfig.set("items.chainmailleggings.material", "CHAINMAIL_LEGGINGS");
        shopConfig.set("items.chainmailleggings.price", 180);
        shopConfig.set("items.chainmailleggings.sellPrice", 45);
        shopConfig.set("items.chainmailleggings.amount", 1);
        shopConfig.set("items.chainmailleggings.slot", 30);
        shopConfig.set("items.chainmailleggings.page", 1);
        shopConfig.set("items.chainmailleggings.category", "Combat");

        shopConfig.set("items.chaianmailchestplate.material", "CHAINMAIL_CHESTPLATE");
        shopConfig.set("items.chaianmailchestplate.price", 180);
        shopConfig.set("items.chaianmailchestplate.sellPrice", 45);
        shopConfig.set("items.chaianmailchestplate.amount", 1);
        shopConfig.set("items.chaianmailchestplate.slot", 32);
        shopConfig.set("items.chaianmailchestplate.page", 1);
        shopConfig.set("items.chaianmailchestplate.category", "Combat");

        shopConfig.set("items.chainmailhelmet.material", "CHAINMAIL_HELMET");
        shopConfig.set("items.chainmailhelmet.price", 180);
        shopConfig.set("items.chainmailhelmet.sellPrice", 45);
        shopConfig.set("items.chainmailhelmet.amount", 1);
        shopConfig.set("items.chainmailhelmet.slot", 34);
        shopConfig.set("items.chainmailhelmet.page", 1);
        shopConfig.set("items.chainmailhelmet.category", "Combat");

        shopConfig.set("items.ironboots.material", "IRON_BOOTS");
        shopConfig.set("items.ironboots.price", 230);
        shopConfig.set("items.ironboots.sellPrice", 55);
        shopConfig.set("items.ironboots.amount", 1);
        shopConfig.set("items.ironboots.slot", 10);
        shopConfig.set("items.ironboots.page", 2);
        shopConfig.set("items.ironboots.category", "Combat");

        shopConfig.set("items.ironleggings.material", "IRON_LEGGINGS");
        shopConfig.set("items.ironleggings.price", 230);
        shopConfig.set("items.ironleggings.sellPrice", 55);
        shopConfig.set("items.ironleggings.amount", 1);
        shopConfig.set("items.ironleggings.slot", 12);
        shopConfig.set("items.ironleggings.page", 2);
        shopConfig.set("items.ironleggings.category", "Combat");

        shopConfig.set("items.ironchestplate.material", "IRON_CHESTPLATE");
        shopConfig.set("items.ironchestplate.price", 230);
        shopConfig.set("items.ironchestplate.sellPrice", 55);
        shopConfig.set("items.ironchestplate.amount", 1);
        shopConfig.set("items.ironchestplate.slot", 14);
        shopConfig.set("items.ironchestplate.page", 2);
        shopConfig.set("items.ironchestplate.category", "Combat");

        shopConfig.set("items.ironhelmet.material", "IRON_HELMET");
        shopConfig.set("items.ironhelmet.price", 230);
        shopConfig.set("items.ironhelmet.sellPrice", 55);
        shopConfig.set("items.ironhelmet.amount", 1);
        shopConfig.set("items.ironhelmet.slot", 16);
        shopConfig.set("items.ironhelmet.page", 2);
        shopConfig.set("items.ironhelmet.category", "Combat");

        shopConfig.set("items.diamondboots.material", "DIAMOND_BOOTS");
        shopConfig.set("items.diamondboots.price", 1750);
        shopConfig.set("items.diamondboots.sellPrice", 460);
        shopConfig.set("items.diamondboots.amount", 1);
        shopConfig.set("items.diamondboots.slot", 19);
        shopConfig.set("items.diamondboots.page", 2);
        shopConfig.set("items.diamondboots.category", "Combat");

        shopConfig.set("items.diamondleggings.material", "DIAMOND_LEGGINGS");
        shopConfig.set("items.diamondleggings.price", 1750);
        shopConfig.set("items.diamondleggings.sellPrice", 460);
        shopConfig.set("items.diamondleggings.amount", 1);
        shopConfig.set("items.diamondleggings.slot", 21);
        shopConfig.set("items.diamondleggings.page", 2);
        shopConfig.set("items.diamondleggings.category", "Combat");

        shopConfig.set("items.diamondchestplate.material", "DIAMOND_CHESTPLATE");
        shopConfig.set("items.diamondchestplate.price", 1750);
        shopConfig.set("items.diamondchestplate.sellPrice", 460);
        shopConfig.set("items.diamondchestplate.amount", 1);
        shopConfig.set("items.diamondchestplate.slot", 23);
        shopConfig.set("items.diamondchestplate.page", 2);
        shopConfig.set("items.diamondchestplate.category", "Combat");

        shopConfig.set("items.diamondhelmet.material", "DIAMOND_HELMET");
        shopConfig.set("items.diamondhelmet.price", 1750);
        shopConfig.set("items.diamondhelmet.sellPrice", 460);
        shopConfig.set("items.diamondhelmet.amount", 1);
        shopConfig.set("items.diamondhelmet.slot", 25);
        shopConfig.set("items.diamondhelmet.page", 2);
        shopConfig.set("items.diamondhelmet.category", "Combat");

        shopConfig.set("items.netheriteboots.material", "NETHERITE_BOOTS");
        shopConfig.set("items.netheriteboots.price", 3400);
        shopConfig.set("items.netheriteboots.sellPrice", 1240);
        shopConfig.set("items.netheriteboots.amount", 1);
        shopConfig.set("items.netheriteboots.slot", 28);
        shopConfig.set("items.netheriteboots.page", 2);
        shopConfig.set("items.netheriteboots.category", "Combat");

        shopConfig.set("items.netheriteleggings.material", "NETHERITE_LEGGINGS");
        shopConfig.set("items.netheriteleggings.price", 3400);
        shopConfig.set("items.netheriteleggings.sellPrice", 1240);
        shopConfig.set("items.netheriteleggings.amount", 1);
        shopConfig.set("items.netheriteleggings.slot", 30);
        shopConfig.set("items.netheriteleggings.page", 2);
        shopConfig.set("items.netheriteleggings.category", "Combat");

        shopConfig.set("items.netheritechestplate.material", "NETHERITE_CHESTPLATE");
        shopConfig.set("items.netheritechestplate.price", 3400);
        shopConfig.set("items.netheritechestplate.sellPrice", 1240);
        shopConfig.set("items.netheritechestplate.amount", 1);
        shopConfig.set("items.netheritechestplate.slot", 32);
        shopConfig.set("items.netheritechestplate.page", 2);
        shopConfig.set("items.netheritechestplate.category", "Combat");

        shopConfig.set("items.netheritehelmet.material", "NETHERITE_HELMET");
        shopConfig.set("items.netheritehelmet.price", 3400);
        shopConfig.set("items.netheritehelmet.sellPrice", 1240);
        shopConfig.set("items.netheritehelmet.amount", 1);
        shopConfig.set("items.netheritehelmet.slot", 34);
        shopConfig.set("items.netheritehelmet.page", 2);
        shopConfig.set("items.netheritehelmet.category", "Combat");

        shopConfig.set("items.arrow1.material", "ARROW");
        shopConfig.set("items.arrow1.price", 8);
        shopConfig.set("items.arrow1.sellPrice", 2);
        shopConfig.set("items.arrow1.amount", 1);
        shopConfig.set("items.arrow1.slot", 10);
        shopConfig.set("items.arrow1.page", 3);
        shopConfig.set("items.arrow1.category", "Combat");

        shopConfig.set("items.spectralarrow.material", "SPECTRAL_ARROW");
        shopConfig.set("items.spectralarrow.price", 15);
        shopConfig.set("items.spectralarrow.sellPrice", 4);
        shopConfig.set("items.spectralarrow.amount", 1);
        shopConfig.set("items.spectralarrow.slot", 12);
        shopConfig.set("items.spectralarrow.page", 3);
        shopConfig.set("items.spectralarrow.category", "Combat");

        shopConfig.set("items.tnt.material", "TNT");
        shopConfig.set("items.tnt.price", 40);
        shopConfig.set("items.tnt.sellPrice", 10);
        shopConfig.set("items.tnt.amount", 1);
        shopConfig.set("items.tnt.slot", 14);
        shopConfig.set("items.tnt.page", 3);
        shopConfig.set("items.tnt.category", "Combat");


        shopConfig.set("items.firecharge1.material", "FIRE_CHARGE");
        shopConfig.set("items.firecharge1.price", 40);
        shopConfig.set("items.firecharge1.sellPrice", 8);
        shopConfig.set("items.firecharge1.amount", 1);
        shopConfig.set("items.firecharge1.slot", 16);
        shopConfig.set("items.firecharge1.page", 3);
        shopConfig.set("items.firecharge1.category", "Combat");

        shopConfig.set("items.endcrystal1.material", "END_CRYSTAL");
        shopConfig.set("items.endcrystal1.price", 130);
        shopConfig.set("items.endcrystal1.sellPrice", 35);
        shopConfig.set("items.endcrystal1.amount", 1);
        shopConfig.set("items.endcrystal1.slot", 19);
        shopConfig.set("items.endcrystal1.page", 3);
        shopConfig.set("items.endcrystal1.category", "Combat");

        shopConfig.set("items.obsidian.material", "OBSIDIAN");
        shopConfig.set("items.obsidian.price", 110);
        shopConfig.set("items.obsidian.sellPrice", 25);
        shopConfig.set("items.obsidian.amount", 1);
        shopConfig.set("items.obsidian.slot", 21);
        shopConfig.set("items.obsidian.page", 3);
        shopConfig.set("items.obsidian.category", "Combat");

        shopConfig.set("items.respawnanchor.material", "RESPAWN_ANCHOR");
        shopConfig.set("items.respawnanchor.price", 230);
        shopConfig.set("items.respawnanchor.sellPrice", 60);
        shopConfig.set("items.respawnanchor.amount", 1);
        shopConfig.set("items.respawnanchor.slot", 23);
        shopConfig.set("items.respawnanchor.page", 3);
        shopConfig.set("items.respawnanchor.category", "Combat");

        shopConfig.set("items.glowstone.material", "GLOWSTONE");
        shopConfig.set("items.glowstone.price", 140);
        shopConfig.set("items.glowstone.sellPrice", 40);
        shopConfig.set("items.glowstone.amount", 1);
        shopConfig.set("items.glowstone.slot", 25);
        shopConfig.set("items.glowstone.page", 3);
        shopConfig.set("items.glowstone.category", "Combat");


        shopConfig.set("items.whitewool.material", "WHITE_WOOL");
        shopConfig.set("items.whitewool.price", 20);
        shopConfig.set("items.whitewool.sellPrice", 4);
        shopConfig.set("items.whitewool.amount", 1);
        shopConfig.set("items.whitewool.slot", 10);
        shopConfig.set("items.whitewool.page", 1);
        shopConfig.set("items.whitewool.category", "Colored Blocks");

        shopConfig.set("items.lightgraywool.material", "LIGHT_GRAY_WOOL");
        shopConfig.set("items.lightgraywool.price", 20);
        shopConfig.set("items.lightgraywool.sellPrice", 4);
        shopConfig.set("items.lightgraywool.amount", 1);
        shopConfig.set("items.lightgraywool.slot", 12);
        shopConfig.set("items.lightgraywool.page", 1);
        shopConfig.set("items.lightgraywool.category", "Colored Blocks");

        shopConfig.set("items.graywool.material", "GRAY_WOOL");
        shopConfig.set("items.graywool.price", 20);
        shopConfig.set("items.graywool.sellPrice", 4);
        shopConfig.set("items.graywool.amount", 1);
        shopConfig.set("items.graywool.slot", 14);
        shopConfig.set("items.graywool.page", 1);
        shopConfig.set("items.graywool.category", "Colored Blocks");

        shopConfig.set("items.blackwool.material", "BLACK_WOOL");
        shopConfig.set("items.blackwool.price", 20);
        shopConfig.set("items.blackwool.sellPrice", 4);
        shopConfig.set("items.blackwool.amount", 1);
        shopConfig.set("items.blackwool.slot", 16);
        shopConfig.set("items.blackwool.page", 1);
        shopConfig.set("items.blackwool.category", "Colored Blocks");

        shopConfig.set("items.brownwool.material", "BROWN_WOOL");
        shopConfig.set("items.brownwool.price", 20);
        shopConfig.set("items.brownwool.sellPrice", 4);
        shopConfig.set("items.brownwool.amount", 1);
        shopConfig.set("items.brownwool.slot", 19);
        shopConfig.set("items.brownwool.page", 1);
        shopConfig.set("items.brownwool.category", "Colored Blocks");

        shopConfig.set("items.redwool.material", "RED_WOOL");
        shopConfig.set("items.redwool.price", 20);
        shopConfig.set("items.redwool.sellPrice", 4);
        shopConfig.set("items.redwool.amount", 1);
        shopConfig.set("items.redwool.slot", 21);
        shopConfig.set("items.redwool.page", 1);
        shopConfig.set("items.redwool.category", "Colored Blocks");

        shopConfig.set("items.orangewool.material", "ORANGE_WOOL");
        shopConfig.set("items.orangewool.price", 20);
        shopConfig.set("items.orangewool.sellPrice", 4);
        shopConfig.set("items.orangewool.amount", 1);
        shopConfig.set("items.orangewool.slot", 23);
        shopConfig.set("items.orangewool.page", 1);
        shopConfig.set("items.orangewool.category", "Colored Blocks");

        shopConfig.set("items.yellowwool.material", "YELLOW_WOOL");
        shopConfig.set("items.yellowwool.price", 20);
        shopConfig.set("items.yellowwool.sellPrice", 4);
        shopConfig.set("items.yellowwool.amount", 1);
        shopConfig.set("items.yellowwool.slot", 25);
        shopConfig.set("items.yellowwool.page", 1);
        shopConfig.set("items.yellowwool.category", "Colored Blocks");

        shopConfig.set("items.limewool.material", "LIME_WOOL");
        shopConfig.set("items.limewool.price", 20);
        shopConfig.set("items.limewool.sellPrice", 4);
        shopConfig.set("items.limewool.amount", 1);
        shopConfig.set("items.limewool.slot", 28);
        shopConfig.set("items.limewool.page", 1);
        shopConfig.set("items.limewool.category", "Colored Blocks");

        shopConfig.set("items.greenwool.material", "GREEN_WOOL");
        shopConfig.set("items.greenwool.price", 20);
        shopConfig.set("items.greenwool.sellPrice", 4);
        shopConfig.set("items.greenwool.amount", 1);
        shopConfig.set("items.greenwool.slot", 30);
        shopConfig.set("items.greenwool.page", 1);
        shopConfig.set("items.greenwool.category", "Colored Blocks");

        shopConfig.set("items.cyanwool.material", "CYAN_WOOL");
        shopConfig.set("items.cyanwool.price", 20);
        shopConfig.set("items.cyanwool.sellPrice", 4);
        shopConfig.set("items.cyanwool.amount", 1);
        shopConfig.set("items.cyanwool.slot", 32);
        shopConfig.set("items.cyanwool.page", 1);
        shopConfig.set("items.cyanwool.category", "Colored Blocks");

        shopConfig.set("items.lightbluewool.material", "LIGHT_BLUE_WOOL");
        shopConfig.set("items.lightbluewool.price", 20);
        shopConfig.set("items.lightbluewool.sellPrice", 4);
        shopConfig.set("items.lightbluewool.amount", 1);
        shopConfig.set("items.lightbluewool.slot", 34);
        shopConfig.set("items.lightbluewool.page", 1);
        shopConfig.set("items.lightbluewool.category", "Colored Blocks");

        shopConfig.set("items.bluewool.material", "BLUE_WOOL");
        shopConfig.set("items.bluewool.price", 20);
        shopConfig.set("items.bluewool.sellPrice", 4);
        shopConfig.set("items.bluewool.amount", 1);
        shopConfig.set("items.bluewool.slot", 10);
        shopConfig.set("items.bluewool.page", 2);
        shopConfig.set("items.bluewool.category", "Colored Blocks");

        shopConfig.set("items.purplewool.material", "PURPLE_WOOL");
        shopConfig.set("items.purplewool.price", 20);
        shopConfig.set("items.purplewool.sellPrice", 4);
        shopConfig.set("items.purplewool.amount", 1);
        shopConfig.set("items.purplewool.slot", 12);
        shopConfig.set("items.purplewool.page", 2);
        shopConfig.set("items.purplewool.category", "Colored Blocks");

        shopConfig.set("items.magentawool.material", "MAGENTA_WOOL");
        shopConfig.set("items.magentawool.price", 20);
        shopConfig.set("items.magentawool.sellPrice", 4);
        shopConfig.set("items.magentawool.amount", 1);
        shopConfig.set("items.magentawool.slot", 14);
        shopConfig.set("items.magentawool.page", 2);
        shopConfig.set("items.magentawool.category", "Colored Blocks");

        shopConfig.set("items.pinkwool.material", "PINK_WOOL");
        shopConfig.set("items.pinkwool.price", 20);
        shopConfig.set("items.pinkwool.sellPrice", 4);
        shopConfig.set("items.pinkwool.amount", 1);
        shopConfig.set("items.pinkwool.slot", 16);
        shopConfig.set("items.pinkwool.page", 2);
        shopConfig.set("items.pinkwool.category", "Colored Blocks");

        shopConfig.set("items.whitecarpet.material", "WHITE_CARPET");
        shopConfig.set("items.whitecarpet.price", 10);
        shopConfig.set("items.whitecarpet.sellPrice", 2);
        shopConfig.set("items.whitecarpet.amount", 1);
        shopConfig.set("items.whitecarpet.slot", 19);
        shopConfig.set("items.whitecarpet.page", 2);
        shopConfig.set("items.whitecarpet.category", "Colored Blocks");

        shopConfig.set("items.lightgraycarpet.material", "LIGHT_GRAY_CARPET");
        shopConfig.set("items.lightgraycarpet.price", 10);
        shopConfig.set("items.lightgraycarpet.sellPrice", 2);
        shopConfig.set("items.lightgraycarpet.amount", 1);
        shopConfig.set("items.lightgraycarpet.slot", 21);
        shopConfig.set("items.lightgraycarpet.page", 2);
        shopConfig.set("items.lightgraycarpet.category", "Colored Blocks");

        shopConfig.set("items.graycarpet.material", "GRAY_CARPET");
        shopConfig.set("items.graycarpet.price", 10);
        shopConfig.set("items.graycarpet.sellPrice", 2);
        shopConfig.set("items.graycarpet.amount", 1);
        shopConfig.set("items.graycarpet.slot", 23);
        shopConfig.set("items.graycarpet.page", 2);
        shopConfig.set("items.graycarpet.category", "Colored Blocks");

        shopConfig.set("items.blackcarpet.material", "BLACK_CARPET");
        shopConfig.set("items.blackcarpet.price", 10);
        shopConfig.set("items.blackcarpet.sellPrice", 2);
        shopConfig.set("items.blackcarpet.amount", 1);
        shopConfig.set("items.blackcarpet.slot", 25);
        shopConfig.set("items.blackcarpet.page", 2);
        shopConfig.set("items.blackcarpet.category", "Colored Blocks");

        shopConfig.set("items.browncarpet.material", "BROWN_CARPET");
        shopConfig.set("items.browncarpet.price", 10);
        shopConfig.set("items.browncarpet.sellPrice", 2);
        shopConfig.set("items.browncarpet.amount", 1);
        shopConfig.set("items.browncarpet.slot", 28);
        shopConfig.set("items.browncarpet.page", 2);
        shopConfig.set("items.browncarpet.category", "Colored Blocks");

        shopConfig.set("items.redcarpet.material", "RED_CARPET");
        shopConfig.set("items.redcarpet.price", 10);
        shopConfig.set("items.redcarpet.sellPrice", 2);
        shopConfig.set("items.redcarpet.amount", 1);
        shopConfig.set("items.redcarpet.slot", 30);
        shopConfig.set("items.redcarpet.page", 2);
        shopConfig.set("items.redcarpet.category", "Colored Blocks");

        shopConfig.set("items.orangecarpet.material", "ORANGE_CARPET");
        shopConfig.set("items.orangecarpet.price", 10);
        shopConfig.set("items.orangecarpet.sellPrice", 2);
        shopConfig.set("items.orangecarpet.amount", 1);
        shopConfig.set("items.orangecarpet.slot", 32);
        shopConfig.set("items.orangecarpet.page", 2);
        shopConfig.set("items.orangecarpet.category", "Colored Blocks");


        shopConfig.set("items.limecarpet.material", "LIME_CARPET");
        shopConfig.set("items.limecarpet.price", 10);
        shopConfig.set("items.limecarpet.sellPrice", 2);
        shopConfig.set("items.limecarpet.amount", 1);
        shopConfig.set("items.limecarpet.slot", 10);
        shopConfig.set("items.limecarpet.page", 3);
        shopConfig.set("items.limecarpet.category", "Colored Blocks");

        shopConfig.set("items.greencarpet.material", "GREEN_CARPET");
        shopConfig.set("items.greencarpet.price", 10);
        shopConfig.set("items.greencarpet.sellPrice", 2);
        shopConfig.set("items.greencarpet.amount", 1);
        shopConfig.set("items.greencarpet.slot", 12);
        shopConfig.set("items.greencarpet.page", 3);
        shopConfig.set("items.greencarpet.category", "Colored Blocks");

        shopConfig.set("items.cyancarpet.material", "CYAN_CARPET");
        shopConfig.set("items.cyancarpet.price", 10);
        shopConfig.set("items.cyancarpet.sellPrice", 2);
        shopConfig.set("items.cyancarpet.amount", 1);
        shopConfig.set("items.cyancarpet.slot", 14);
        shopConfig.set("items.cyancarpet.page", 3);
        shopConfig.set("items.cyancarpet.category", "Colored Blocks");

        shopConfig.set("items.lightbluecarpet.material", "LIGHT_BLUE_CARPET");
        shopConfig.set("items.lightbluecarpet.price", 10);
        shopConfig.set("items.lightbluecarpet.sellPrice", 2);
        shopConfig.set("items.lightbluecarpet.amount", 1);
        shopConfig.set("items.lightbluecarpet.slot", 16);
        shopConfig.set("items.lightbluecarpet.page", 3);
        shopConfig.set("items.lightbluecarpet.category", "Colored Blocks");

        shopConfig.set("items.bluecarpet.material", "BLUE_CARPET");
        shopConfig.set("items.bluecarpet.price", 10);
        shopConfig.set("items.bluecarpet.sellPrice", 2);
        shopConfig.set("items.bluecarpet.amount", 1);
        shopConfig.set("items.bluecarpet.slot", 19);
        shopConfig.set("items.bluecarpet.page", 3);
        shopConfig.set("items.bluecarpet.category", "Colored Blocks");

        shopConfig.set("items.purplecarpet.material", "PURPLE_CARPET");
        shopConfig.set("items.purplecarpet.price", 10);
        shopConfig.set("items.purplecarpet.sellPrice", 2);
        shopConfig.set("items.purplecarpet.amount", 1);
        shopConfig.set("items.purplecarpet.slot", 21);
        shopConfig.set("items.purplecarpet.page", 3);
        shopConfig.set("items.purplecarpet.category", "Colored Blocks");

        shopConfig.set("items.magentacarpet.material", "MAGENTA_CARPET");
        shopConfig.set("items.magentacarpet.price", 10);
        shopConfig.set("items.magentacarpet.sellPrice", 2);
        shopConfig.set("items.magentacarpet.amount", 1);
        shopConfig.set("items.magentacarpet.slot", 23);
        shopConfig.set("items.magentacarpet.page", 3);
        shopConfig.set("items.magentacarpet.category", "Colored Blocks");

        shopConfig.set("items.pinkcarpet.material", "PINK_CARPET");
        shopConfig.set("items.pinkcarpet.price", 10);
        shopConfig.set("items.pinkcarpet.sellPrice", 2);
        shopConfig.set("items.pinkcarpet.amount", 1);
        shopConfig.set("items.pinkcarpet.slot", 25);
        shopConfig.set("items.pinkcarpet.page", 3);
        shopConfig.set("items.pinkcarpet.category", "Colored Blocks");

        shopConfig.set("items.terracotta.material", "TERRACOTTA");
        shopConfig.set("items.terracotta.price", 70);
        shopConfig.set("items.terracotta.sellPrice", 15);
        shopConfig.set("items.terracotta.amount", 1);
        shopConfig.set("items.terracotta.slot", 28);
        shopConfig.set("items.terracotta.page", 3);
        shopConfig.set("items.terracotta.category", "Colored Blocks");

        shopConfig.set("items.whiteterracotta.material", "WHITE_TERRACOTTA");
        shopConfig.set("items.whiteterracotta.price", 70);
        shopConfig.set("items.whiteterracotta.sellPrice", 15);
        shopConfig.set("items.whiteterracotta.amount", 1);
        shopConfig.set("items.whiteterracotta.slot", 30);
        shopConfig.set("items.whiteterracotta.page", 3);
        shopConfig.set("items.whiteterracotta.category", "Colored Blocks");

        shopConfig.set("items.lightgrayterracotta.material", "LIGHT_GRAY_TERRACOTTA");
        shopConfig.set("items.lightgrayterracotta.price", 70);
        shopConfig.set("items.lightgrayterracotta.sellPrice", 15);
        shopConfig.set("items.lightgrayterracotta.amount", 1);
        shopConfig.set("items.lightgrayterracotta.slot", 32);
        shopConfig.set("items.lightgrayterracotta.page", 3);
        shopConfig.set("items.lightgrayterracotta.category", "Colored Blocks");

        shopConfig.set("items.grayterracotta.material", "GRAY_TERRACOTTA");
        shopConfig.set("items.grayterracotta.price", 70);
        shopConfig.set("items.grayterracotta.sellPrice", 15);
        shopConfig.set("items.grayterracotta.amount", 1);
        shopConfig.set("items.grayterracotta.slot", 34);
        shopConfig.set("items.grayterracotta.page", 3);
        shopConfig.set("items.grayterracotta.category", "Colored Blocks");

        shopConfig.set("items.blackterracotta.material", "BLACK_TERRACOTTA");
        shopConfig.set("items.blackterracotta.price", 70);
        shopConfig.set("items.blackterracotta.sellPrice", 15);
        shopConfig.set("items.blackterracotta.amount", 1);
        shopConfig.set("items.blackterracotta.slot", 10);
        shopConfig.set("items.blackterracotta.page", 4);
        shopConfig.set("items.blackterracotta.category", "Colored Blocks");

        shopConfig.set("items.brownterracotta.material", "BROWN_TERRACOTTA");
        shopConfig.set("items.brownterracotta.price", 70);
        shopConfig.set("items.brownterracotta.sellPrice", 15);
        shopConfig.set("items.brownterracotta.amount", 1);
        shopConfig.set("items.brownterracotta.slot", 12);
        shopConfig.set("items.brownterracotta.page", 4);
        shopConfig.set("items.brownterracotta.category", "Colored Blocks");

        shopConfig.set("items.redterracotta.material", "RED_TERRACOTTA");
        shopConfig.set("items.redterracotta.price", 70);
        shopConfig.set("items.redterracotta.sellPrice", 15);
        shopConfig.set("items.redterracotta.amount", 1);
        shopConfig.set("items.redterracotta.slot", 14);
        shopConfig.set("items.redterracotta.page", 4);
        shopConfig.set("items.redterracotta.category", "Colored Blocks");

        shopConfig.set("items.orangeterracotta.material", "ORANGE_TERRACOTTA");
        shopConfig.set("items.orangeterracotta.price", 70);
        shopConfig.set("items.orangeterracotta.sellPrice", 15);
        shopConfig.set("items.orangeterracotta.amount", 1);
        shopConfig.set("items.orangeterracotta.slot", 16);
        shopConfig.set("items.orangeterracotta.page", 4);
        shopConfig.set("items.orangeterracotta.category", "Colored Blocks");

        shopConfig.set("items.yellowterracotta.material", "YELLOW_TERRACOTTA");
        shopConfig.set("items.yellowterracotta.price", 70);
        shopConfig.set("items.yellowterracotta.sellPrice", 15);
        shopConfig.set("items.yellowterracotta.amount", 1);
        shopConfig.set("items.yellowterracotta.slot", 19);
        shopConfig.set("items.yellowterracotta.page", 4);
        shopConfig.set("items.yellowterracotta.category", "Colored Blocks");

        shopConfig.set("items.limeterracotta.material", "LIME_TERRACOTTA");
        shopConfig.set("items.limeterracotta.price", 70);
        shopConfig.set("items.limeterracotta.sellPrice", 15);
        shopConfig.set("items.limeterracotta.amount", 1);
        shopConfig.set("items.limeterracotta.slot", 21);
        shopConfig.set("items.limeterracotta.page", 4);
        shopConfig.set("items.limeterracotta.category", "Colored Blocks");

        shopConfig.set("items.greenterracotta.material", "GREEN_TERRACOTTA");
        shopConfig.set("items.greenterracotta.price", 70);
        shopConfig.set("items.greenterracotta.sellPrice", 15);
        shopConfig.set("items.greenterracotta.amount", 1);
        shopConfig.set("items.greenterracotta.slot", 23);
        shopConfig.set("items.greenterracotta.page", 4);
        shopConfig.set("items.greenterracotta.category", "Colored Blocks");

        shopConfig.set("items.cyanterracotta.material", "CYAN_TERRACOTTA");
        shopConfig.set("items.cyanterracotta.price", 70);
        shopConfig.set("items.cyanterracotta.sellPrice", 15);
        shopConfig.set("items.cyanterracotta.amount", 1);
        shopConfig.set("items.cyanterracotta.slot", 25);
        shopConfig.set("items.cyanterracotta.page", 4);
        shopConfig.set("items.cyanterracotta.category", "Colored Blocks");

        shopConfig.set("items.lightblueterracotta.material", "LIGHT_BLUE_TERRACOTTA");
        shopConfig.set("items.lightblueterracotta.price", 70);
        shopConfig.set("items.lightblueterracotta.sellPrice", 15);
        shopConfig.set("items.lightblueterracotta.amount", 1);
        shopConfig.set("items.lightblueterracotta.slot", 28);
        shopConfig.set("items.lightblueterracotta.page", 4);
        shopConfig.set("items.lightblueterracotta.category", "Colored Blocks");

        shopConfig.set("items.blueterracotta.material", "BLUE_TERRACOTTA");
        shopConfig.set("items.blueterracotta.price", 70);
        shopConfig.set("items.blueterracotta.sellPrice", 15);
        shopConfig.set("items.blueterracotta.amount", 1);
        shopConfig.set("items.blueterracotta.slot", 28);
        shopConfig.set("items.blueterracotta.page", 4);
        shopConfig.set("items.blueterracotta.category", "Colored Blocks");

        shopConfig.set("items.purpleterracotta.material", "PURPLE_TERRACOTTA");
        shopConfig.set("items.purpleterracotta.price", 70);
        shopConfig.set("items.purpleterracotta.sellPrice", 15);
        shopConfig.set("items.purpleterracotta.amount", 1);
        shopConfig.set("items.purpleterracotta.slot", 30);
        shopConfig.set("items.purpleterracotta.page", 4);
        shopConfig.set("items.purpleterracotta.category", "Colored Blocks");

        shopConfig.set("items.magentaterracotta.material", "MAGENTA_TERRACOTTA");
        shopConfig.set("items.magentaterracotta.price", 70);
        shopConfig.set("items.magentaterracotta.sellPrice", 15);
        shopConfig.set("items.magentaterracotta.amount", 1);
        shopConfig.set("items.magentaterracotta.slot", 32);
        shopConfig.set("items.magentaterracotta.page", 4);
        shopConfig.set("items.magentaterracotta.category", "Colored Blocks");

        shopConfig.set("items.pinkterracotta.material", "MAGENTA_TERRACOTTA");
        shopConfig.set("items.pinkterracotta.price", 70);
        shopConfig.set("items.pinkterracotta.sellPrice", 15);
        shopConfig.set("items.pinkterracotta.amount", 1);
        shopConfig.set("items.pinkterracotta.slot", 34);
        shopConfig.set("items.pinkterracotta.page", 4);
        shopConfig.set("items.pinkterracotta.category", "Colored Blocks");

        shopConfig.set("items.whiteconcrete.material", "WHITE_CONCRETE");
        shopConfig.set("items.whiteconcrete.price", 80);
        shopConfig.set("items.whiteconcrete.sellPrice", 25);
        shopConfig.set("items.whiteconcrete.amount", 1);
        shopConfig.set("items.whiteconcrete.slot", 10);
        shopConfig.set("items.whiteconcrete.page", 4);
        shopConfig.set("items.whiteconcrete.category", "Colored Blocks");

        shopConfig.set("items.lightgrayconcrete.material", "LIGHT_GRAY_CONCRETE");
        shopConfig.set("items.lightgrayconcrete.price", 80);
        shopConfig.set("items.lightgrayconcrete.sellPrice", 25);
        shopConfig.set("items.lightgrayconcrete.amount", 1);
        shopConfig.set("items.lightgrayconcrete.slot", 12);
        shopConfig.set("items.lightgrayconcrete.page", 4);
        shopConfig.set("items.lightgrayconcrete.category", "Colored Blocks");

        shopConfig.set("items.grayconcrete.material", "GRAY_CONCRETE");
        shopConfig.set("items.grayconcrete.price", 80);
        shopConfig.set("items.grayconcrete.sellPrice", 25);
        shopConfig.set("items.grayconcrete.amount", 1);
        shopConfig.set("items.grayconcrete.slot", 14);
        shopConfig.set("items.grayconcrete.page", 4);
        shopConfig.set("items.grayconcrete.category", "Colored Blocks");

        shopConfig.set("items.blackconcrete.material", "BLACK_CONCRETE");
        shopConfig.set("items.blackconcrete.price", 80);
        shopConfig.set("items.blackconcrete.sellPrice", 25);
        shopConfig.set("items.blackconcrete.amount", 1);
        shopConfig.set("items.blackconcrete.slot", 16);
        shopConfig.set("items.blackconcrete.page", 4);
        shopConfig.set("items.blackconcrete.category", "Colored Blocks");

        shopConfig.set("items.bronwconcrete.material", "BROWN_CONCRETE");
        shopConfig.set("items.bronwconcrete.price", 80);
        shopConfig.set("items.bronwconcrete.sellPrice", 25);
        shopConfig.set("items.bronwconcrete.amount", 1);
        shopConfig.set("items.bronwconcrete.slot", 19);
        shopConfig.set("items.bronwconcrete.page", 4);
        shopConfig.set("items.bronwconcrete.category", "Colored Blocks");

        shopConfig.set("items.redconcrete.material", "RED_CONCRETE");
        shopConfig.set("items.redconcrete.price", 80);
        shopConfig.set("items.redconcrete.sellPrice", 25);
        shopConfig.set("items.redconcrete.amount", 1);
        shopConfig.set("items.redconcrete.slot", 21);
        shopConfig.set("items.redconcrete.page", 4);
        shopConfig.set("items.redconcrete.category", "Colored Blocks");

        shopConfig.set("items.orangeconcrete.material", "ORANGE_CONCRETE");
        shopConfig.set("items.orangeconcrete.price", 80);
        shopConfig.set("items.orangeconcrete.sellPrice", 25);
        shopConfig.set("items.orangeconcrete.amount", 1);
        shopConfig.set("items.orangeconcrete.slot", 23);
        shopConfig.set("items.orangeconcrete.page", 4);
        shopConfig.set("items.orangeconcrete.category", "Colored Blocks");

        shopConfig.set("items.yellowcarpet.material", "YELLOW_CONCRETE");
        shopConfig.set("items.yellowcarpet.price", 80);
        shopConfig.set("items.yellowcarpet.sellPrice", 25);
        shopConfig.set("items.yellowcarpet.amount", 1);
        shopConfig.set("items.yellowcarpet.slot", 25);
        shopConfig.set("items.yellowcarpet.page", 4);
        shopConfig.set("items.yellowcarpet.category", "Colored Blocks");

        shopConfig.set("items.limeconcrete.material", "LIME_CONCRETE");
        shopConfig.set("items.limeconcrete.price", 80);
        shopConfig.set("items.limeconcrete.sellPrice", 25);
        shopConfig.set("items.limeconcrete.amount", 1);
        shopConfig.set("items.limeconcrete.slot", 28);
        shopConfig.set("items.limeconcrete.page", 4);
        shopConfig.set("items.limeconcrete.category", "Colored Blocks");

        shopConfig.set("items.greenconcrete.material", "GREEN_CONCRETE");
        shopConfig.set("items.greenconcrete.price", 80);
        shopConfig.set("items.greenconcrete.sellPrice", 25);
        shopConfig.set("items.greenconcrete.amount", 1);
        shopConfig.set("items.greenconcrete.slot", 30);
        shopConfig.set("items.greenconcrete.page", 4);
        shopConfig.set("items.greenconcrete.category", "Colored Blocks");

        shopConfig.set("items.cyanconcrete.material", "CYAN_CONCRETE");
        shopConfig.set("items.cyanconcrete.price", 80);
        shopConfig.set("items.cyanconcrete.sellPrice", 25);
        shopConfig.set("items.cyanconcrete.amount", 1);
        shopConfig.set("items.cyanconcrete.slot", 32);
        shopConfig.set("items.cyanconcrete.page", 4);
        shopConfig.set("items.cyanconcrete.category", "Colored Blocks");

        shopConfig.set("items.lightblueconcrete.material", "LIGHT_BLUE_CONCRETE");
        shopConfig.set("items.lightblueconcrete.price", 80);
        shopConfig.set("items.lightblueconcrete.sellPrice", 25);
        shopConfig.set("items.lightblueconcrete.amount", 1);
        shopConfig.set("items.lightblueconcrete.slot", 34);
        shopConfig.set("items.lightblueconcrete.page", 4);
        shopConfig.set("items.lightblueconcrete.category", "Colored Blocks");

        shopConfig.set("items.blueconcrete.material", "BLUE_CONCRETE");
        shopConfig.set("items.blueconcrete.price", 80);
        shopConfig.set("items.blueconcrete.sellPrice", 25);
        shopConfig.set("items.blueconcrete.amount", 1);
        shopConfig.set("items.blueconcrete.slot", 10);
        shopConfig.set("items.blueconcrete.page", 5);
        shopConfig.set("items.blueconcrete.category", "Colored Blocks");

        shopConfig.set("items.purpleconcrete.material", "PURPLE_CONCRETE");
        shopConfig.set("items.purpleconcrete.price", 80);
        shopConfig.set("items.purpleconcrete.sellPrice", 25);
        shopConfig.set("items.purpleconcrete.amount", 1);
        shopConfig.set("items.purpleconcrete.slot", 12);
        shopConfig.set("items.purpleconcrete.page", 5);
        shopConfig.set("items.purpleconcrete.category", "Colored Blocks");

        shopConfig.set("items.magentaconcrete.material", "MAGENTA_CONCRETE");
        shopConfig.set("items.magentaconcrete.price", 80);
        shopConfig.set("items.magentaconcrete.sellPrice", 25);
        shopConfig.set("items.magentaconcrete.amount", 1);
        shopConfig.set("items.magentaconcrete.slot", 14);
        shopConfig.set("items.magentaconcrete.page", 5);
        shopConfig.set("items.magentaconcrete.category", "Colored Blocks");

        shopConfig.set("items.pinkconcrete.material", "PINK_CONCRETE");
        shopConfig.set("items.pinkconcrete.price", 80);
        shopConfig.set("items.pinkconcrete.sellPrice", 25);
        shopConfig.set("items.pinkconcrete.amount", 1);
        shopConfig.set("items.pinkconcrete.slot", 16);
        shopConfig.set("items.pinkconcrete.page", 5);
        shopConfig.set("items.pinkconcrete.category", "Colored Blocks");

        shopConfig.set("items.whiteconcretepowder.material", "WHITE_CONCRETE_POWDER");
        shopConfig.set("items.whiteconcretepowder.price", 75);
        shopConfig.set("items.whiteconcretepowder.sellPrice", 20);
        shopConfig.set("items.whiteconcretepowder.amount", 1);
        shopConfig.set("items.whiteconcretepowder.slot", 19);
        shopConfig.set("items.whiteconcretepowder.page", 5);
        shopConfig.set("items.whiteconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.lightgrayconcretepowder.material", "LIGHT_GRAY_CONCRETE_POWDER");
        shopConfig.set("items.lightgrayconcretepowder.price", 75);
        shopConfig.set("items.lightgrayconcretepowder.sellPrice", 20);
        shopConfig.set("items.lightgrayconcretepowder.amount", 1);
        shopConfig.set("items.lightgrayconcretepowder.slot", 21);
        shopConfig.set("items.lightgrayconcretepowder.page", 5);
        shopConfig.set("items.lightgrayconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.grayconcretepowder.material", "GRAY_CONCRETE_POWDER");
        shopConfig.set("items.grayconcretepowder.price", 75);
        shopConfig.set("items.grayconcretepowder.sellPrice", 20);
        shopConfig.set("items.grayconcretepowder.amount", 1);
        shopConfig.set("items.grayconcretepowder.slot", 23);
        shopConfig.set("items.grayconcretepowder.page", 5);
        shopConfig.set("items.grayconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.blackconcretepowder.material", "BLACK_CONCRETE_POWDER");
        shopConfig.set("items.blackconcretepowder.price", 75);
        shopConfig.set("items.blackconcretepowder.sellPrice", 20);
        shopConfig.set("items.blackconcretepowder.amount", 1);
        shopConfig.set("items.blackconcretepowder.slot", 25);
        shopConfig.set("items.blackconcretepowder.page", 5);
        shopConfig.set("items.blackconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.brownconcretepowder.material", "BROWN_CONCRETE_POWDER");
        shopConfig.set("items.brownconcretepowder.price", 75);
        shopConfig.set("items.brownconcretepowder.sellPrice", 20);
        shopConfig.set("items.brownconcretepowder.amount", 1);
        shopConfig.set("items.brownconcretepowder.slot", 28);
        shopConfig.set("items.brownconcretepowder.page", 5);
        shopConfig.set("items.brownconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.redconcretepowder.material", "RED_CONCRETE_POWDER");
        shopConfig.set("items.redconcretepowder.price", 75);
        shopConfig.set("items.redconcretepowder.sellPrice", 20);
        shopConfig.set("items.redconcretepowder.amount", 1);
        shopConfig.set("items.redconcretepowder.slot", 30);
        shopConfig.set("items.redconcretepowder.page", 5);
        shopConfig.set("items.redconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.orangeconcretepowder.material", "ORANGE_CONCRETE_POWDER");
        shopConfig.set("items.orangeconcretepowder.price", 75);
        shopConfig.set("items.orangeconcretepowder.sellPrice", 20);
        shopConfig.set("items.orangeconcretepowder.amount", 1);
        shopConfig.set("items.orangeconcretepowder.slot", 32);
        shopConfig.set("items.orangeconcretepowder.page", 5);
        shopConfig.set("items.orangeconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.yellowconcretepowder.material", "YELLOW_CONCRETE_POWDER");
        shopConfig.set("items.yellowconcretepowder.price", 75);
        shopConfig.set("items.yellowconcretepowder.sellPrice", 20);
        shopConfig.set("items.yellowconcretepowder.amount", 1);
        shopConfig.set("items.yellowconcretepowder.slot", 34);
        shopConfig.set("items.yellowconcretepowder.page", 5);
        shopConfig.set("items.yellowconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.limeconcretepowder.material", "LIME_CONCRETE_POWDER");
        shopConfig.set("items.limeconcretepowder.price", 75);
        shopConfig.set("items.limeconcretepowder.sellPrice", 20);
        shopConfig.set("items.limeconcretepowder.amount", 1);
        shopConfig.set("items.limeconcretepowder.slot", 10);
        shopConfig.set("items.limeconcretepowder.page", 6);
        shopConfig.set("items.limeconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.greenconcretepowder.material", "GREEN_CONCRETE_POWDER");
        shopConfig.set("items.greenconcretepowder.price", 75);
        shopConfig.set("items.greenconcretepowder.sellPrice", 20);
        shopConfig.set("items.greenconcretepowder.amount", 1);
        shopConfig.set("items.greenconcretepowder.slot", 12);
        shopConfig.set("items.greenconcretepowder.page", 6);
        shopConfig.set("items.greenconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.cyanconcretepowder.material", "CYAN_CONCRETE_POWDER");
        shopConfig.set("items.cyanconcretepowder.price", 75);
        shopConfig.set("items.cyanconcretepowder.sellPrice", 20);
        shopConfig.set("items.cyanconcretepowder.amount", 1);
        shopConfig.set("items.cyanconcretepowder.slot", 14);
        shopConfig.set("items.cyanconcretepowder.page", 6);
        shopConfig.set("items.cyanconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.lightblueconcretepowder.material", "LIGHT_BLUE_CONCRETE_POWDER");
        shopConfig.set("items.lightblueconcretepowder.price", 75);
        shopConfig.set("items.lightblueconcretepowder.sellPrice", 20);
        shopConfig.set("items.lightblueconcretepowder.amount", 1);
        shopConfig.set("items.lightblueconcretepowder.slot", 16);
        shopConfig.set("items.lightblueconcretepowder.page", 6);
        shopConfig.set("items.lightblueconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.blueconcretepowder.material", "BLUE_CONCRETE_POWDER");
        shopConfig.set("items.blueconcretepowder.price", 75);
        shopConfig.set("items.blueconcretepowder.sellPrice", 20);
        shopConfig.set("items.blueconcretepowder.amount", 1);
        shopConfig.set("items.blueconcretepowder.slot", 19);
        shopConfig.set("items.blueconcretepowder.page", 6);
        shopConfig.set("items.blueconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.purpleconcretepowder.material", "PURPLE_CONCRETE_POWDER");
        shopConfig.set("items.purpleconcretepowder.price", 75);
        shopConfig.set("items.purpleconcretepowder.sellPrice", 20);
        shopConfig.set("items.purpleconcretepowder.amount", 1);
        shopConfig.set("items.purpleconcretepowder.slot", 21);
        shopConfig.set("items.purpleconcretepowder.page", 6);
        shopConfig.set("items.purpleconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.maagentaconcretepowder.material", "MAGENTA_CONCRETE_POWDER");
        shopConfig.set("items.maagentaconcretepowder.price", 75);
        shopConfig.set("items.maagentaconcretepowder.sellPrice", 20);
        shopConfig.set("items.maagentaconcretepowder.amount", 1);
        shopConfig.set("items.maagentaconcretepowder.slot", 23);
        shopConfig.set("items.maagentaconcretepowder.page", 6);
        shopConfig.set("items.maagentaconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.pinkconcretepowder.material", "PINK_CONCRETE_POWDER");
        shopConfig.set("items.pinkconcretepowder.price", 75);
        shopConfig.set("items.pinkconcretepowder.sellPrice", 20);
        shopConfig.set("items.pinkconcretepowder.amount", 1);
        shopConfig.set("items.pinkconcretepowder.slot", 25);
        shopConfig.set("items.pinkconcretepowder.page", 6);
        shopConfig.set("items.pinkconcretepowder.category", "Colored Blocks");

        shopConfig.set("items.whiteglazedterracotta.material", "WHITE_GLAZED_TERRACOTTA");
        shopConfig.set("items.whiteglazedterracotta.price", 60);
        shopConfig.set("items.whiteglazedterracotta.sellPrice", 15);
        shopConfig.set("items.whiteglazedterracotta.amount", 1);
        shopConfig.set("items.whiteglazedterracotta.slot", 28);
        shopConfig.set("items.whiteglazedterracotta.page", 6);
        shopConfig.set("items.whiteglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.lightgrayglazesterracotta.material", "LIGHT_GRAY_GLAZED_TERRACOTTA");
        shopConfig.set("items.lightgrayglazesterracotta.price", 60);
        shopConfig.set("items.lightgrayglazesterracotta.sellPrice", 15);
        shopConfig.set("items.lightgrayglazesterracotta.amount", 1);
        shopConfig.set("items.lightgrayglazesterracotta.slot", 30);
        shopConfig.set("items.lightgrayglazesterracotta.page", 6);
        shopConfig.set("items.lightgrayglazesterracotta.category", "Colored Blocks");

        shopConfig.set("items.grayglazedterracotta.material", "GRAY_GLAZED_TERRACOTTA");
        shopConfig.set("items.grayglazedterracotta.price", 60);
        shopConfig.set("items.grayglazedterracotta.sellPrice", 15);
        shopConfig.set("items.grayglazedterracotta.amount", 1);
        shopConfig.set("items.grayglazedterracotta.slot", 32);
        shopConfig.set("items.grayglazedterracotta.page", 6);
        shopConfig.set("items.grayglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.blackglazzedterracotta.material", "BLACK_GLAZED_TERRACOTTA");
        shopConfig.set("items.blackglazzedterracotta.price", 60);
        shopConfig.set("items.blackglazzedterracotta.sellPrice", 15);
        shopConfig.set("items.blackglazzedterracotta.amount", 1);
        shopConfig.set("items.blackglazzedterracotta.slot", 34);
        shopConfig.set("items.blackglazzedterracotta.page", 6);
        shopConfig.set("items.blackglazzedterracotta.category", "Colored Blocks");

        shopConfig.set("items.brownglazedterracotta.material", "BROWN_GLAZED_TERRACOTTA");
        shopConfig.set("items.brownglazedterracotta.price", 60);
        shopConfig.set("items.brownglazedterracotta.sellPrice", 15);
        shopConfig.set("items.brownglazedterracotta.amount", 1);
        shopConfig.set("items.brownglazedterracotta.slot", 10);
        shopConfig.set("items.brownglazedterracotta.page", 7);
        shopConfig.set("items.brownglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.redglazedterracotta.material", "RED_GLAZED_TERRACOTTA");
        shopConfig.set("items.redglazedterracotta.price", 60);
        shopConfig.set("items.redglazedterracotta.sellPrice", 15);
        shopConfig.set("items.redglazedterracotta.amount", 1);
        shopConfig.set("items.redglazedterracotta.slot", 12);
        shopConfig.set("items.redglazedterracotta.page", 7);
        shopConfig.set("items.redglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.orangeglazedterracotta.material", "ORANGE_GLAZED_TERRACOTTA");
        shopConfig.set("items.orangeglazedterracotta.price", 60);
        shopConfig.set("items.orangeglazedterracotta.sellPrice", 15);
        shopConfig.set("items.orangeglazedterracotta.amount", 1);
        shopConfig.set("items.orangeglazedterracotta.slot", 14);
        shopConfig.set("items.orangeglazedterracotta.page", 7);
        shopConfig.set("items.orangeglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.yellowglazedterracotta.material", "YELLOW_GLAZED_TERRACOTTA");
        shopConfig.set("items.yellowglazedterracotta.price", 60);
        shopConfig.set("items.yellowglazedterracotta.sellPrice", 15);
        shopConfig.set("items.yellowglazedterracotta.amount", 1);
        shopConfig.set("items.yellowglazedterracotta.slot", 16);
        shopConfig.set("items.yellowglazedterracotta.page", 7);
        shopConfig.set("items.yellowglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.limeglazedterracotta.material", "LIME_GLAZED_TERRACOTTA");
        shopConfig.set("items.limeglazedterracotta.price", 60);
        shopConfig.set("items.limeglazedterracotta.sellPrice", 15);
        shopConfig.set("items.limeglazedterracotta.amount", 1);
        shopConfig.set("items.limeglazedterracotta.slot", 19);
        shopConfig.set("items.limeglazedterracotta.page", 7);
        shopConfig.set("items.limeglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.greenglazedterracotta.material", "GREEN_GLAZED_TERRACOTTA");
        shopConfig.set("items.greenglazedterracotta.price", 60);
        shopConfig.set("items.greenglazedterracotta.sellPrice", 15);
        shopConfig.set("items.greenglazedterracotta.amount", 1);
        shopConfig.set("items.greenglazedterracotta.slot", 21);
        shopConfig.set("items.greenglazedterracotta.page", 7);
        shopConfig.set("items.greenglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.cyanglazedterracotta.material", "CYAN_GLAZED_TERRACOTTA");
        shopConfig.set("items.cyanglazedterracotta.price", 60);
        shopConfig.set("items.cyanglazedterracotta.sellPrice", 15);
        shopConfig.set("items.cyanglazedterracotta.amount", 1);
        shopConfig.set("items.cyanglazedterracotta.slot", 23);
        shopConfig.set("items.cyanglazedterracotta.page", 7);
        shopConfig.set("items.cyanglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.lightblueglazedterracotta.material", "LIGHT_BLUE_GLAZED_TERRACOTTA");
        shopConfig.set("items.lightblueglazedterracotta.price", 60);
        shopConfig.set("items.lightblueglazedterracotta.sellPrice", 15);
        shopConfig.set("items.lightblueglazedterracotta.amount", 1);
        shopConfig.set("items.lightblueglazedterracotta.slot", 25);
        shopConfig.set("items.lightblueglazedterracotta.page", 7);
        shopConfig.set("items.lightblueglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.blueglazedterracotta.material", "BLUE_GLAZED_TERRACOTTA");
        shopConfig.set("items.blueglazedterracotta.price", 60);
        shopConfig.set("items.blueglazedterracotta.sellPrice", 15);
        shopConfig.set("items.blueglazedterracotta.amount", 1);
        shopConfig.set("items.blueglazedterracotta.slot", 28);
        shopConfig.set("items.blueglazedterracotta.page", 7);
        shopConfig.set("items.blueglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.purpleglazedterracotta.material", "PURPLE_GLAZED_TERRACOTTA");
        shopConfig.set("items.purpleglazedterracotta.price", 60);
        shopConfig.set("items.purpleglazedterracotta.sellPrice", 15);
        shopConfig.set("items.purpleglazedterracotta.amount", 1);
        shopConfig.set("items.purpleglazedterracotta.slot", 30);
        shopConfig.set("items.purpleglazedterracotta.page", 7);
        shopConfig.set("items.purpleglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.megentaglazedterracotta.material", "MAGENTA_GLAZED_TERRACOTTA");
        shopConfig.set("items.megentaglazedterracotta.price", 60);
        shopConfig.set("items.megentaglazedterracotta.sellPrice", 15);
        shopConfig.set("items.megentaglazedterracotta.amount", 1);
        shopConfig.set("items.megentaglazedterracotta.slot", 32);
        shopConfig.set("items.megentaglazedterracotta.page", 7);
        shopConfig.set("items.megentaglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.pinkglazedterracotta.material", "PINK_GLAZED_TERRACOTTA");
        shopConfig.set("items.pinkglazedterracotta.price", 60);
        shopConfig.set("items.pinkglazedterracotta.sellPrice", 15);
        shopConfig.set("items.pinkglazedterracotta.amount", 1);
        shopConfig.set("items.pinkglazedterracotta.slot", 34);
        shopConfig.set("items.pinkglazedterracotta.page", 7);
        shopConfig.set("items.pinkglazedterracotta.category", "Colored Blocks");

        shopConfig.set("items.whitestainedglass.material", "WHITE_STAINED_GLASS");
        shopConfig.set("items.whitestainedglass.price", 30);
        shopConfig.set("items.whitestainedglass.sellPrice", 10);
        shopConfig.set("items.whitestainedglass.amount", 1);
        shopConfig.set("items.whitestainedglass.slot", 10);
        shopConfig.set("items.whitestainedglass.page", 8);
        shopConfig.set("items.whitestainedglass.category", "Colored Blocks");

        shopConfig.set("items.lightgraystainedglass.material", "LIGHT_GRAY_STAINED_GLASS");
        shopConfig.set("items.lightgraystainedglass.price", 30);
        shopConfig.set("items.lightgraystainedglass.sellPrice", 10);
        shopConfig.set("items.lightgraystainedglass.amount", 1);
        shopConfig.set("items.lightgraystainedglass.slot", 12);
        shopConfig.set("items.lightgraystainedglass.page", 8);
        shopConfig.set("items.lightgraystainedglass.category", "Colored Blocks");

        shopConfig.set("items.graystainedglass.material", "GRAY_STAINED_GLASS");
        shopConfig.set("items.graystainedglass.price", 30);
        shopConfig.set("items.graystainedglass.sellPrice", 10);
        shopConfig.set("items.graystainedglass.amount", 1);
        shopConfig.set("items.graystainedglass.slot", 14);
        shopConfig.set("items.graystainedglass.page", 8);
        shopConfig.set("items.graystainedglass.category", "Colored Blocks");

        shopConfig.set("items.blackstainedglass.material", "BLACK_STAINED_GLASS");
        shopConfig.set("items.blackstainedglass.price", 30);
        shopConfig.set("items.blackstainedglass.sellPrice", 10);
        shopConfig.set("items.blackstainedglass.amount", 1);
        shopConfig.set("items.blackstainedglass.slot", 16);
        shopConfig.set("items.blackstainedglass.page", 8);
        shopConfig.set("items.blackstainedglass.category", "Colored Blocks");

        shopConfig.set("items.brownstainedglass.material", "BROWN_STAINED_GLASS");
        shopConfig.set("items.brownstainedglass.price", 30);
        shopConfig.set("items.brownstainedglass.sellPrice", 10);
        shopConfig.set("items.brownstainedglass.amount", 1);
        shopConfig.set("items.brownstainedglass.slot", 19);
        shopConfig.set("items.brownstainedglass.page", 8);
        shopConfig.set("items.brownstainedglass.category", "Colored Blocks");

        shopConfig.set("items.redstainedglass.material", "RED_STAINED_GLASS");
        shopConfig.set("items.redstainedglass.price", 30);
        shopConfig.set("items.redstainedglass.sellPrice", 10);
        shopConfig.set("items.redstainedglass.amount", 1);
        shopConfig.set("items.redstainedglass.slot", 21);
        shopConfig.set("items.redstainedglass.page", 8);
        shopConfig.set("items.redstainedglass.category", "Colored Blocks");

        shopConfig.set("items.orangestainedglass.material", "ORANGE_STAINED_GLASS");
        shopConfig.set("items.orangestainedglass.price", 30);
        shopConfig.set("items.orangestainedglass.sellPrice", 10);
        shopConfig.set("items.orangestainedglass.amount", 1);
        shopConfig.set("items.orangestainedglass.slot", 23);
        shopConfig.set("items.orangestainedglass.page", 8);
        shopConfig.set("items.orangestainedglass.category", "Colored Blocks");

        shopConfig.set("items.yellowstainedglass.material", "YELLOW_STAINED_GLASS");
        shopConfig.set("items.yellowstainedglass.price", 30);
        shopConfig.set("items.yellowstainedglass.sellPrice", 10);
        shopConfig.set("items.yellowstainedglass.amount", 1);
        shopConfig.set("items.yellowstainedglass.slot", 25);
        shopConfig.set("items.yellowstainedglass.page", 8);
        shopConfig.set("items.yellowstainedglass.category", "Colored Blocks");

        shopConfig.set("items.limestainedglass.material", "LIME_STAINED_GLASS");
        shopConfig.set("items.limestainedglass.price", 30);
        shopConfig.set("items.limestainedglass.sellPrice", 10);
        shopConfig.set("items.limestainedglass.amount", 1);
        shopConfig.set("items.limestainedglass.slot", 28);
        shopConfig.set("items.limestainedglass.page", 8);
        shopConfig.set("items.limestainedglass.category", "Colored Blocks");

        shopConfig.set("items.greenstainedglass.material", "GREEN_STAINED_GLASS");
        shopConfig.set("items.greenstainedglass.price", 30);
        shopConfig.set("items.greenstainedglass.sellPrice", 10);
        shopConfig.set("items.greenstainedglass.amount", 1);
        shopConfig.set("items.greenstainedglass.slot", 30);
        shopConfig.set("items.greenstainedglass.page", 8);
        shopConfig.set("items.greenstainedglass.category", "Colored Blocks");

        shopConfig.set("items.cyanstainedglass.material", "CYAN_STAINED_GLASS");
        shopConfig.set("items.cyanstainedglass.price", 30);
        shopConfig.set("items.cyanstainedglass.sellPrice", 10);
        shopConfig.set("items.cyanstainedglass.amount", 1);
        shopConfig.set("items.cyanstainedglass.slot", 32);
        shopConfig.set("items.cyanstainedglass.page", 8);
        shopConfig.set("items.cyanstainedglass.category", "Colored Blocks");

        shopConfig.set("items.lightbluestainedglass.material", "LIGHT_BLUE_STAINED_GLASS");
        shopConfig.set("items.lightbluestainedglass.price", 30);
        shopConfig.set("items.lightbluestainedglass.sellPrice", 10);
        shopConfig.set("items.lightbluestainedglass.amount", 1);
        shopConfig.set("items.lightbluestainedglass.slot", 34);
        shopConfig.set("items.lightbluestainedglass.page", 8);
        shopConfig.set("items.lightbluestainedglass.category", "Colored Blocks");

        shopConfig.set("items.bluestainedglass.material", "BLUE_STAINED_GLASS");
        shopConfig.set("items.bluestainedglass.price", 30);
        shopConfig.set("items.bluestainedglass.sellPrice", 10);
        shopConfig.set("items.bluestainedglass.amount", 1);
        shopConfig.set("items.bluestainedglass.slot", 10);
        shopConfig.set("items.bluestainedglass.page", 9);
        shopConfig.set("items.bluestainedglass.category", "Colored Blocks");

        shopConfig.set("items.purplestainedglass.material", "PURPLE_STAINED_GLASS");
        shopConfig.set("items.purplestainedglass.price", 30);
        shopConfig.set("items.purplestainedglass.sellPrice", 10);
        shopConfig.set("items.purplestainedglass.amount", 1);
        shopConfig.set("items.purplestainedglass.slot", 12);
        shopConfig.set("items.purplestainedglass.page", 9);
        shopConfig.set("items.purplestainedglass.category", "Colored Blocks");

        shopConfig.set("items.magentastainedglass.material", "MAGENTA_STAINED_GLASS");
        shopConfig.set("items.magentastainedglass.price", 30);
        shopConfig.set("items.magentastainedglass.sellPrice", 10);
        shopConfig.set("items.magentastainedglass.amount", 1);
        shopConfig.set("items.magentastainedglass.slot", 14);
        shopConfig.set("items.magentastainedglass.page", 9);
        shopConfig.set("items.magentastainedglass.category", "Colored Blocks");

        shopConfig.set("items.pinkstainedglass.material", "PINK_STAINED_GLASS");
        shopConfig.set("items.pinkstainedglass.price", 30);
        shopConfig.set("items.pinkstainedglass.sellPrice", 10);
        shopConfig.set("items.pinkstainedglass.amount", 1);
        shopConfig.set("items.pinkstainedglass.slot", 16);
        shopConfig.set("items.pinkstainedglass.page", 9);
        shopConfig.set("items.pinkstainedglass.category", "Colored Blocks");

        shopConfig.set("items.whitestainedglasspane.material", "WHITE_STAINED_GLASS_PANE");
        shopConfig.set("items.whitestainedglasspane.price", 20);
        shopConfig.set("items.whitestainedglasspane.sellPrice", 5);
        shopConfig.set("items.whitestainedglasspane.amount", 1);
        shopConfig.set("items.whitestainedglasspane.slot", 19);
        shopConfig.set("items.whitestainedglasspane.page", 9);
        shopConfig.set("items.whitestainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.lightgraystainedglasspane.material", "LIGHT_GRAY_STAINED_GLASS_PANE");
        shopConfig.set("items.lightgraystainedglasspane.price", 20);
        shopConfig.set("items.lightgraystainedglasspane.sellPrice", 5);
        shopConfig.set("items.lightgraystainedglasspane.amount", 1);
        shopConfig.set("items.lightgraystainedglasspane.slot", 21);
        shopConfig.set("items.lightgraystainedglasspane.page", 9);
        shopConfig.set("items.lightgraystainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.graystainedglasspane.material", "GRAY_STAINED_GLASS_PANE");
        shopConfig.set("items.graystainedglasspane.price", 20);
        shopConfig.set("items.graystainedglasspane.sellPrice", 5);
        shopConfig.set("items.graystainedglasspane.amount", 1);
        shopConfig.set("items.graystainedglasspane.slot", 23);
        shopConfig.set("items.graystainedglasspane.page", 9);
        shopConfig.set("items.graystainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.blackstainedglasspane.material", "BLACK_STAINED_GLASS_PANE");
        shopConfig.set("items.blackstainedglasspane.price", 20);
        shopConfig.set("items.blackstainedglasspane.sellPrice", 5);
        shopConfig.set("items.blackstainedglasspane.amount", 1);
        shopConfig.set("items.blackstainedglasspane.slot", 25);
        shopConfig.set("items.blackstainedglasspane.page", 9);
        shopConfig.set("items.blackstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.brownstainedglasspane.material", "BROWN_STAINED_GLASS_PANE");
        shopConfig.set("items.brownstainedglasspane.price", 20);
        shopConfig.set("items.brownstainedglasspane.sellPrice", 5);
        shopConfig.set("items.brownstainedglasspane.amount", 1);
        shopConfig.set("items.brownstainedglasspane.slot", 28);
        shopConfig.set("items.brownstainedglasspane.page", 9);
        shopConfig.set("items.brownstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.redstainedglasspane.material", "RED_STAINED_GLASS_PANE");
        shopConfig.set("items.redstainedglasspane.price", 20);
        shopConfig.set("items.redstainedglasspane.sellPrice", 5);
        shopConfig.set("items.redstainedglasspane.amount", 1);
        shopConfig.set("items.redstainedglasspane.slot", 30);
        shopConfig.set("items.redstainedglasspane.page", 9);
        shopConfig.set("items.redstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.orangestainedglasspane.material", "ORANGE_STAINED_GLASS_PANE");
        shopConfig.set("items.orangestainedglasspane.price", 20);
        shopConfig.set("items.orangestainedglasspane.sellPrice", 5);
        shopConfig.set("items.orangestainedglasspane.amount", 1);
        shopConfig.set("items.orangestainedglasspane.slot", 32);
        shopConfig.set("items.orangestainedglasspane.page", 9);
        shopConfig.set("items.orangestainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.yellowstainedglasspane.material", "YELLOW_STAINED_GLASS_PANE");
        shopConfig.set("items.yellowstainedglasspane.price", 20);
        shopConfig.set("items.yellowstainedglasspane.sellPrice", 5);
        shopConfig.set("items.yellowstainedglasspane.amount", 1);
        shopConfig.set("items.yellowstainedglasspane.slot", 34);
        shopConfig.set("items.yellowstainedglasspane.page", 9);
        shopConfig.set("items.yellowstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.limestainedglasspane.material", "LIME_STAINED_GLASS_PANE");
        shopConfig.set("items.limestainedglasspane.price", 20);
        shopConfig.set("items.limestainedglasspane.sellPrice", 5);
        shopConfig.set("items.limestainedglasspane.amount", 1);
        shopConfig.set("items.limestainedglasspane.slot", 10);
        shopConfig.set("items.limestainedglasspane.page", 10);
        shopConfig.set("items.limestainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.greenstainedglasspane.material", "GREEN_STAINED_GLASS_PANE");
        shopConfig.set("items.greenstainedglasspane.price", 20);
        shopConfig.set("items.greenstainedglasspane.sellPrice", 5);
        shopConfig.set("items.greenstainedglasspane.amount", 1);
        shopConfig.set("items.greenstainedglasspane.slot", 12);
        shopConfig.set("items.greenstainedglasspane.page", 10);
        shopConfig.set("items.greenstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.cyanstainedglasspane.material", "CYAN_STAINED_GLASS_PANE");
        shopConfig.set("items.cyanstainedglasspane.price", 20);
        shopConfig.set("items.cyanstainedglasspane.sellPrice", 5);
        shopConfig.set("items.cyanstainedglasspane.amount", 1);
        shopConfig.set("items.cyanstainedglasspane.slot", 14);
        shopConfig.set("items.cyanstainedglasspane.page", 10);
        shopConfig.set("items.cyanstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.lightbluestainedglasspane.material", "LIGHT_BLUE_STAINED_GLASS_PANE");
        shopConfig.set("items.lightbluestainedglasspane.price", 20);
        shopConfig.set("items.lightbluestainedglasspane.sellPrice", 5);
        shopConfig.set("items.lightbluestainedglasspane.amount", 1);
        shopConfig.set("items.lightbluestainedglasspane.slot", 16);
        shopConfig.set("items.lightbluestainedglasspane.page", 10);
        shopConfig.set("items.lightbluestainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.bluetainedglasspane.material", "BLUE_STAINED_GLASS_PANE");
        shopConfig.set("items.bluetainedglasspane.price", 20);
        shopConfig.set("items.bluetainedglasspane.sellPrice", 5);
        shopConfig.set("items.bluetainedglasspane.amount", 1);
        shopConfig.set("items.bluetainedglasspane.slot", 19);
        shopConfig.set("items.bluetainedglasspane.page", 10);
        shopConfig.set("items.bluetainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.purplestaainedglasspane.material", "PURPLE_STAINED_GLASS_PANE");
        shopConfig.set("items.purplestaainedglasspane.price", 20);
        shopConfig.set("items.purplestaainedglasspane.sellPrice", 5);
        shopConfig.set("items.purplestaainedglasspane.amount", 1);
        shopConfig.set("items.purplestaainedglasspane.slot", 21);
        shopConfig.set("items.purplestaainedglasspane.page", 10);
        shopConfig.set("items.purplestaainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.magentastainedglasspane.material", "MAGENTA_STAINED_GLASS_PANE");
        shopConfig.set("items.magentastainedglasspane.price", 20);
        shopConfig.set("items.magentastainedglasspane.sellPrice", 5);
        shopConfig.set("items.magentastainedglasspane.amount", 1);
        shopConfig.set("items.magentastainedglasspane.slot", 23);
        shopConfig.set("items.magentastainedglasspane.page", 10);
        shopConfig.set("items.magentastainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.pinkstainedglasspane.material", "PINK_STAINED_GLASS_PANE");
        shopConfig.set("items.pinkstainedglasspane.price", 20);
        shopConfig.set("items.pinkstainedglasspane.sellPrice", 5);
        shopConfig.set("items.pinkstainedglasspane.amount", 1);
        shopConfig.set("items.pinkstainedglasspane.slot", 25);
        shopConfig.set("items.pinkstainedglasspane.page", 10);
        shopConfig.set("items.pinkstainedglasspane.category", "Colored Blocks");

        shopConfig.set("items.yellowcarpet.material", "YELLOW_CARPET");
        shopConfig.set("items.yellowcarpet.price", 10);
        shopConfig.set("items.yellowcarpet.sellPrice", 2);
        shopConfig.set("items.yellowcarpet.amount", 1);
        shopConfig.set("items.yellowcarpet.slot", 34);
        shopConfig.set("items.yellowcarpet.page", 2);
        shopConfig.set("items.yellowcarpet.category", "Colored Blocks");

        shopConfig.set("items.whitebed.material", "WHITE_BED");
        shopConfig.set("items.whitebed.price", 110);
        shopConfig.set("items.whitebed.sellPrice", 35);
        shopConfig.set("items.whitebed.amount", 1);
        shopConfig.set("items.whitebed.slot", 28);
        shopConfig.set("items.whitebed.page", 10);
        shopConfig.set("items.whitebed.category", "Colored Blocks");

        shopConfig.set("items.lightgraybed.material", "LIGHT_GRAY_BED");
        shopConfig.set("items.lightgraybed.price", 110);
        shopConfig.set("items.lightgraybed.sellPrice", 35);
        shopConfig.set("items.lightgraybed.amount", 1);
        shopConfig.set("items.lightgraybed.slot", 30);
        shopConfig.set("items.lightgraybed.page", 10);
        shopConfig.set("items.lightgraybed.category", "Colored Blocks");

        shopConfig.set("items.graybed.material", "GRAY_BED");
        shopConfig.set("items.graybed.price", 110);
        shopConfig.set("items.graybed.sellPrice", 35);
        shopConfig.set("items.graybed.amount", 1);
        shopConfig.set("items.graybed.slot", 32);
        shopConfig.set("items.graybed.page", 10);
        shopConfig.set("items.graybed.category", "Colored Blocks");

        shopConfig.set("items.blackbed.material", "BLACK_BED");
        shopConfig.set("items.blackbed.price", 110);
        shopConfig.set("items.blackbed.sellPrice", 35);
        shopConfig.set("items.blackbed.amount", 1);
        shopConfig.set("items.blackbed.slot", 34);
        shopConfig.set("items.blackbed.page", 10);
        shopConfig.set("items.blackbed.category", "Colored Blocks");

        shopConfig.set("items.brownbed.material", "BROWN_BED");
        shopConfig.set("items.brownbed.price", 110);
        shopConfig.set("items.brownbed.sellPrice", 35);
        shopConfig.set("items.brownbed.amount", 1);
        shopConfig.set("items.brownbed.slot", 10);
        shopConfig.set("items.brownbed.page", 11);
        shopConfig.set("items.brownbed.category", "Colored Blocks");

        shopConfig.set("items.redbed.material", "RED_BED");
        shopConfig.set("items.redbed.price", 110);
        shopConfig.set("items.redbed.sellPrice", 35);
        shopConfig.set("items.redbed.amount", 1);
        shopConfig.set("items.redbed.slot", 12);
        shopConfig.set("items.redbed.page", 11);
        shopConfig.set("items.redbed.category", "Colored Blocks");

        shopConfig.set("items.ornagebed.material", "ORANGE_BED");
        shopConfig.set("items.ornagebed.price", 110);
        shopConfig.set("items.ornagebed.sellPrice", 35);
        shopConfig.set("items.ornagebed.amount", 1);
        shopConfig.set("items.ornagebed.slot", 14);
        shopConfig.set("items.ornagebed.page", 11);
        shopConfig.set("items.ornagebed.category", "Colored Blocks");

        shopConfig.set("items.yellowbed.material", "YELLOW_BED");
        shopConfig.set("items.yellowbed.price", 110);
        shopConfig.set("items.yellowbed.sellPrice", 35);
        shopConfig.set("items.yellowbed.amount", 1);
        shopConfig.set("items.yellowbed.slot", 16);
        shopConfig.set("items.yellowbed.page", 11);
        shopConfig.set("items.yellowbed.category", "Colored Blocks");

        shopConfig.set("items.limebed.material", "LIME_BED");
        shopConfig.set("items.limebed.price", 110);
        shopConfig.set("items.limebed.sellPrice", 35);
        shopConfig.set("items.limebed.amount", 1);
        shopConfig.set("items.limebed.slot", 19);
        shopConfig.set("items.limebed.page", 11);
        shopConfig.set("items.limebed.category", "Colored Blocks");

        shopConfig.set("items.greenbed.material", "GREEN_BED");
        shopConfig.set("items.greenbed.price", 110);
        shopConfig.set("items.greenbed.sellPrice", 35);
        shopConfig.set("items.greenbed.amount", 1);
        shopConfig.set("items.greenbed.slot", 21);
        shopConfig.set("items.greenbed.page", 11);
        shopConfig.set("items.greenbed.category", "Colored Blocks");

        shopConfig.set("items.cyanbed.material", "CYAN_BED");
        shopConfig.set("items.cyanbed.price", 110);
        shopConfig.set("items.cyanbed.sellPrice", 35);
        shopConfig.set("items.cyanbed.amount", 1);
        shopConfig.set("items.cyanbed.slot", 23);
        shopConfig.set("items.cyanbed.page", 11);
        shopConfig.set("items.cyanbed.category", "Colored Blocks");

        shopConfig.set("items.lightbluebed.material", "LIGHT_BLUE_BED");
        shopConfig.set("items.lightbluebed.price", 110);
        shopConfig.set("items.lightbluebed.sellPrice", 35);
        shopConfig.set("items.lightbluebed.amount", 1);
        shopConfig.set("items.lightbluebed.slot", 25);
        shopConfig.set("items.lightbluebed.page", 11);
        shopConfig.set("items.lightbluebed.category", "Colored Blocks");

        shopConfig.set("items.bluebed.material", "BLUE_BED");
        shopConfig.set("items.bluebed.price", 110);
        shopConfig.set("items.bluebed.sellPrice", 35);
        shopConfig.set("items.bluebed.amount", 1);
        shopConfig.set("items.bluebed.slot", 28);
        shopConfig.set("items.bluebed.page", 11);
        shopConfig.set("items.bluebed.category", "Colored Blocks");

        shopConfig.set("items.purplebed.material", "PURPLE_BED");
        shopConfig.set("items.purplebed.price", 110);
        shopConfig.set("items.purplebed.sellPrice", 35);
        shopConfig.set("items.purplebed.amount", 1);
        shopConfig.set("items.purplebed.slot", 30);
        shopConfig.set("items.purplebed.page", 11);
        shopConfig.set("items.purplebed.category", "Colored Blocks");

        shopConfig.set("items.magentabed.material", "MAGENTA_BED");
        shopConfig.set("items.magentabed.price", 110);
        shopConfig.set("items.magentabed.sellPrice", 35);
        shopConfig.set("items.magentabed.amount", 1);
        shopConfig.set("items.magentabed.slot", 32);
        shopConfig.set("items.magentabed.page", 11);
        shopConfig.set("items.magentabed.category", "Colored Blocks");

        shopConfig.set("items.pinkbed.material", "PINK_BED");
        shopConfig.set("items.pinkbed.price", 110);
        shopConfig.set("items.pinkbed.sellPrice", 35);
        shopConfig.set("items.pinkbed.amount", 1);
        shopConfig.set("items.pinkbed.slot", 34);
        shopConfig.set("items.pinkbed.page", 11);
        shopConfig.set("items.pinkbed.category", "Colored Blocks");

        shopConfig.set("items.whitecandle.material", "WHITE_CANDLE");
        shopConfig.set("items.whitecandle.price", 110);
        shopConfig.set("items.whitecandle.sellPrice", 35);
        shopConfig.set("items.whitecandle.amount", 1);
        shopConfig.set("items.whitecandle.slot", 10);
        shopConfig.set("items.whitecandle.page", 12);
        shopConfig.set("items.whitecandle.category", "Colored Blocks");

        shopConfig.set("items.lightgraycandle.material", "LIGHT_GRAY_CANDLE");
        shopConfig.set("items.lightgraycandle.price", 110);
        shopConfig.set("items.lightgraycandle.sellPrice", 35);
        shopConfig.set("items.lightgraycandle.amount", 1);
        shopConfig.set("items.lightgraycandle.slot", 12);
        shopConfig.set("items.lightgraycandle.page", 12);
        shopConfig.set("items.lightgraycandle.category", "Colored Blocks");

        shopConfig.set("items.graycandle.material", "GRAY_CANDLE");
        shopConfig.set("items.graycandle.price", 110);
        shopConfig.set("items.graycandle.sellPrice", 35);
        shopConfig.set("items.graycandle.amount", 1);
        shopConfig.set("items.graycandle.slot", 14);
        shopConfig.set("items.graycandle.page", 12);
        shopConfig.set("items.graycandle.category", "Colored Blocks");

        shopConfig.set("items.blackcandle.material", "BLACK_CANDLE");
        shopConfig.set("items.blackcandle.price", 110);
        shopConfig.set("items.blackcandle.sellPrice", 35);
        shopConfig.set("items.blackcandle.amount", 1);
        shopConfig.set("items.blackcandle.slot", 16);
        shopConfig.set("items.blackcandle.page", 12);
        shopConfig.set("items.blackcandle.category", "Colored Blocks");

        shopConfig.set("items.browncandle.material", "BROWN_CANDLE");
        shopConfig.set("items.browncandle.price", 110);
        shopConfig.set("items.browncandle.sellPrice", 35);
        shopConfig.set("items.browncandle.amount", 1);
        shopConfig.set("items.browncandle.slot", 19);
        shopConfig.set("items.browncandle.page", 12);
        shopConfig.set("items.browncandle.category", "Colored Blocks");

        shopConfig.set("items.redcandle.material", "RED_CANDLE");
        shopConfig.set("items.redcandle.price", 110);
        shopConfig.set("items.redcandle.sellPrice", 35);
        shopConfig.set("items.redcandle.amount", 1);
        shopConfig.set("items.redcandle.slot", 21);
        shopConfig.set("items.redcandle.page", 12);
        shopConfig.set("items.redcandle.category", "Colored Blocks");

        shopConfig.set("items.orangecandle.material", "ORANGE_CANDLE");
        shopConfig.set("items.orangecandle.price", 110);
        shopConfig.set("items.orangecandle.sellPrice", 35);
        shopConfig.set("items.orangecandle.amount", 1);
        shopConfig.set("items.orangecandle.slot", 23);
        shopConfig.set("items.orangecandle.page", 12);
        shopConfig.set("items.orangecandle.category", "Colored Blocks");

        shopConfig.set("items.yellowcandle.material", "YELLOW_CANDLE");
        shopConfig.set("items.yellowcandle.price", 110);
        shopConfig.set("items.yellowcandle.sellPrice", 35);
        shopConfig.set("items.yellowcandle.amount", 1);
        shopConfig.set("items.yellowcandle.slot", 25);
        shopConfig.set("items.yellowcandle.page", 12);
        shopConfig.set("items.yellowcandle.category", "Colored Blocks");

        shopConfig.set("items.liemcandle.material", "LIME_CANDLE");
        shopConfig.set("items.liemcandle.price", 110);
        shopConfig.set("items.liemcandle.sellPrice", 35);
        shopConfig.set("items.liemcandle.amount", 1);
        shopConfig.set("items.liemcandle.slot", 28);
        shopConfig.set("items.liemcandle.page", 12);
        shopConfig.set("items.liemcandle.category", "Colored Blocks");

        shopConfig.set("items.greencandle.material", "GREEN_CANDLE");
        shopConfig.set("items.greencandle.price", 110);
        shopConfig.set("items.greencandle.sellPrice", 35);
        shopConfig.set("items.greencandle.amount", 1);
        shopConfig.set("items.greencandle.slot", 30);
        shopConfig.set("items.greencandle.page", 12);
        shopConfig.set("items.greencandle.category", "Colored Blocks");

        shopConfig.set("items.cyancandle.material", "CYAN_CANDLE");
        shopConfig.set("items.cyancandle.price", 110);
        shopConfig.set("items.cyancandle.sellPrice", 35);
        shopConfig.set("items.cyancandle.amount", 1);
        shopConfig.set("items.cyancandle.slot", 32);
        shopConfig.set("items.cyancandle.page", 12);
        shopConfig.set("items.cyancandle.category", "Colored Blocks");

        shopConfig.set("items.lightbluecandle.material", "LIGHT_BLUE_CANDLE");
        shopConfig.set("items.lightbluecandle.price", 110);
        shopConfig.set("items.lightbluecandle.sellPrice", 35);
        shopConfig.set("items.lightbluecandle.amount", 1);
        shopConfig.set("items.lightbluecandle.slot", 34);
        shopConfig.set("items.lightbluecandle.page", 12);
        shopConfig.set("items.lightbluecandle.category", "Colored Blocks");

        shopConfig.set("items.bluecandle.material", "BLUE_CANDLE");
        shopConfig.set("items.bluecandle.price", 110);
        shopConfig.set("items.bluecandle.sellPrice", 35);
        shopConfig.set("items.bluecandle.amount", 1);
        shopConfig.set("items.bluecandle.slot", 10);
        shopConfig.set("items.bluecandle.page", 13);
        shopConfig.set("items.bluecandle.category", "Colored Blocks");

        shopConfig.set("items.purplecandle.material", "PURPLE_CANDLE");
        shopConfig.set("items.purplecandle.price", 110);
        shopConfig.set("items.purplecandle.sellPrice", 35);
        shopConfig.set("items.purplecandle.amount", 1);
        shopConfig.set("items.purplecandle.slot", 12);
        shopConfig.set("items.purplecandle.page", 13);
        shopConfig.set("items.purplecandle.category", "Colored Blocks");

        shopConfig.set("items.mangentacandle.material", "MAGENTA_CANDLE");
        shopConfig.set("items.mangentacandle.price", 110);
        shopConfig.set("items.mangentacandle.sellPrice", 35);
        shopConfig.set("items.mangentacandle.amount", 1);
        shopConfig.set("items.mangentacandle.slot", 14);
        shopConfig.set("items.mangentacandle.page", 13);
        shopConfig.set("items.mangentacandle.category", "Colored Blocks");

        shopConfig.set("items.pinkcandle.material", "PINK_CANDLE");
        shopConfig.set("items.pinkcandle.price", 110);
        shopConfig.set("items.pinkcandle.sellPrice", 35);
        shopConfig.set("items.pinkcandle.amount", 1);
        shopConfig.set("items.pinkcandle.slot", 16);
        shopConfig.set("items.pinkcandle.page", 13);
        shopConfig.set("items.pinkcandle.category", "Colored Blocks");

        shopConfig.set("items.whitebanner.material", "WHITE_BANNER");
        shopConfig.set("items.whitebanner.price", 45);
        shopConfig.set("items.whitebanner.sellPrice", 5);
        shopConfig.set("items.whitebanner.amount", 1);
        shopConfig.set("items.whitebanner.slot", 19);
        shopConfig.set("items.whitebanner.page", 13);
        shopConfig.set("items.whitebanner.category", "Colored Blocks");

        shopConfig.set("items.lightgraybanner.material", "LIGHT_GRAY_BANNER");
        shopConfig.set("items.lightgraybanner.price", 45);
        shopConfig.set("items.lightgraybanner.sellPrice", 5);
        shopConfig.set("items.lightgraybanner.amount", 1);
        shopConfig.set("items.lightgraybanner.slot", 21);
        shopConfig.set("items.lightgraybanner.page", 13);
        shopConfig.set("items.lightgraybanner.category", "Colored Blocks");

        shopConfig.set("items.graybanner.material", "GRAY_BANNER");
        shopConfig.set("items.graybanner.price", 45);
        shopConfig.set("items.graybanner.sellPrice", 5);
        shopConfig.set("items.graybanner.amount", 1);
        shopConfig.set("items.graybanner.slot", 23);
        shopConfig.set("items.graybanner.page", 13);
        shopConfig.set("items.graybanner.category", "Colored Blocks");

        shopConfig.set("items.blackbanner.material", "BLACK_BANNER");
        shopConfig.set("items.blackbanner.price", 45);
        shopConfig.set("items.blackbanner.sellPrice", 5);
        shopConfig.set("items.blackbanner.amount", 1);
        shopConfig.set("items.blackbanner.slot", 25);
        shopConfig.set("items.blackbanner.page", 13);
        shopConfig.set("items.blackbanner.category", "Colored Blocks");

        shopConfig.set("items.brownbanner.material", "BROWN_BANNER");
        shopConfig.set("items.brownbanner.price", 45);
        shopConfig.set("items.brownbanner.sellPrice", 5);
        shopConfig.set("items.brownbanner.amount", 1);
        shopConfig.set("items.brownbanner.slot", 28);
        shopConfig.set("items.brownbanner.page", 13);
        shopConfig.set("items.brownbanner.category", "Colored Blocks");

        shopConfig.set("items.redbanner.material", "RED_BANNER");
        shopConfig.set("items.redbanner.price", 45);
        shopConfig.set("items.redbanner.sellPrice", 5);
        shopConfig.set("items.redbanner.amount", 1);
        shopConfig.set("items.redbanner.slot", 30);
        shopConfig.set("items.redbanner.page", 13);
        shopConfig.set("items.redbanner.category", "Colored Blocks");

        shopConfig.set("items.ornagebanner.material", "ORANGE_BANNER");
        shopConfig.set("items.ornagebanner.price", 45);
        shopConfig.set("items.ornagebanner.sellPrice", 5);
        shopConfig.set("items.ornagebanner.amount", 1);
        shopConfig.set("items.ornagebanner.slot", 32);
        shopConfig.set("items.ornagebanner.page", 13);
        shopConfig.set("items.ornagebanner.category", "Colored Blocks");

        shopConfig.set("items.yellowbanner.material", "YELLOW_BANNER");
        shopConfig.set("items.yellowbanner.price", 45);
        shopConfig.set("items.yellowbanner.sellPrice", 5);
        shopConfig.set("items.yellowbanner.amount", 1);
        shopConfig.set("items.yellowbanner.slot", 34);
        shopConfig.set("items.yellowbanner.page", 13);
        shopConfig.set("items.yellowbanner.category", "Colored Blocks");

        shopConfig.set("items.limebanner.material", "LIME_BANNER");
        shopConfig.set("items.limebanner.price", 45);
        shopConfig.set("items.limebanner.sellPrice", 5);
        shopConfig.set("items.limebanner.amount", 1);
        shopConfig.set("items.limebanner.slot", 10);
        shopConfig.set("items.limebanner.page", 14);
        shopConfig.set("items.limebanner.category", "Colored Blocks");

        shopConfig.set("items.greenbanner.material", "GREEN_BANNER");
        shopConfig.set("items.greenbanner.price", 45);
        shopConfig.set("items.greenbanner.sellPrice", 5);
        shopConfig.set("items.greenbanner.amount", 1);
        shopConfig.set("items.greenbanner.slot", 12);
        shopConfig.set("items.greenbanner.page", 14);
        shopConfig.set("items.greenbanner.category", "Colored Blocks");

        shopConfig.set("items.cyanbanner.material", "CYAN_BANNER");
        shopConfig.set("items.cyanbanner.price", 45);
        shopConfig.set("items.cyanbanner.sellPrice", 5);
        shopConfig.set("items.cyanbanner.amount", 1);
        shopConfig.set("items.cyanbanner.slot", 14);
        shopConfig.set("items.cyanbanner.page", 14);
        shopConfig.set("items.cyanbanner.category", "Colored Blocks");

        shopConfig.set("items.lightbluebanner.material", "LIGHT_BLUE_BANNER");
        shopConfig.set("items.lightbluebanner.price", 45);
        shopConfig.set("items.lightbluebanner.sellPrice", 5);
        shopConfig.set("items.lightbluebanner.amount", 1);
        shopConfig.set("items.lightbluebanner.slot", 16);
        shopConfig.set("items.lightbluebanner.page", 14);
        shopConfig.set("items.lightbluebanner.category", "Colored Blocks");

        shopConfig.set("items.bluebanner.material", "BLUE_BANNER");
        shopConfig.set("items.bluebanner.price", 45);
        shopConfig.set("items.bluebanner.sellPrice", 5);
        shopConfig.set("items.bluebanner.amount", 1);
        shopConfig.set("items.bluebanner.slot", 19);
        shopConfig.set("items.bluebanner.page", 14);
        shopConfig.set("items.bluebanner.category", "Colored Blocks");

        shopConfig.set("items.purplebaner.material", "PURPLE_BANNER");
        shopConfig.set("items.purplebaner.price", 45);
        shopConfig.set("items.purplebaner.sellPrice", 5);
        shopConfig.set("items.purplebaner.amount", 1);
        shopConfig.set("items.purplebaner.slot", 21);
        shopConfig.set("items.purplebaner.page", 14);
        shopConfig.set("items.purplebaner.category", "Colored Blocks");

        shopConfig.set("items.magentabanner.material", "MAGENTA_BANNER");
        shopConfig.set("items.magentabanner.price", 45);
        shopConfig.set("items.magentabanner.sellPrice", 5);
        shopConfig.set("items.magentabanner.amount", 1);
        shopConfig.set("items.magentabanner.slot", 23);
        shopConfig.set("items.magentabanner.page", 14);
        shopConfig.set("items.magentabanner.category", "Colored Blocks");

        shopConfig.set("items.pinkbanner.material", "PINK_BANNER");
        shopConfig.set("items.pinkbanner.price", 45);
        shopConfig.set("items.pinkbanner.sellPrice", 5);
        shopConfig.set("items.pinkbanner.amount", 1);
        shopConfig.set("items.pinkbanner.slot", 25);
        shopConfig.set("items.pinkbanner.page", 14);
        shopConfig.set("items.pinkbanner.category", "Colored Blocks");


        shopConfig.set("items.wheatseeds.material", "WHEAT_SEEDS");
        shopConfig.set("items.wheatseeds.price", 5);
        shopConfig.set("items.wheatseeds.sellPrice", 1.5);
        shopConfig.set("items.wheatseeds.amount", 1);
        shopConfig.set("items.wheatseeds.slot", 10);
        shopConfig.set("items.wheatseeds.page", 1);
        shopConfig.set("items.wheatseeds.category", "Farming");

        shopConfig.set("items.beetrootseeds.material", "BEETROOT_SEEDS");
        shopConfig.set("items.beetrootseeds.price", 6);
        shopConfig.set("items.beetrootseeds.sellPrice", 1.75);
        shopConfig.set("items.beetrootseeds.amount", 1);
        shopConfig.set("items.beetrootseeds.slot", 12);
        shopConfig.set("items.beetrootseeds.page", 1);
        shopConfig.set("items.beetrootseeds.category", "Farming");

        shopConfig.set("items.melonseeds.material", "MELON_SEEDS");
        shopConfig.set("items.melonseeds.price", 8);
        shopConfig.set("items.melonseeds.sellPrice", 2.50);
        shopConfig.set("items.melonseeds.amount", 1);
        shopConfig.set("items.melonseeds.slot", 14);
        shopConfig.set("items.melonseeds.page", 1);
        shopConfig.set("items.melonseeds.category", "Farming");

        shopConfig.set("items.pumpkinseeds.material", "PUMPKIN_SEEDS");
        shopConfig.set("items.pumpkinseeds.price", 8);
        shopConfig.set("items.pumpkinseeds.sellPrice", 2.50);
        shopConfig.set("items.pumpkinseeds.amount", 1);
        shopConfig.set("items.pumpkinseeds.slot", 16);
        shopConfig.set("items.pumpkinseeds.page", 1);
        shopConfig.set("items.pumpkinseeds.category", "Farming");

        shopConfig.set("items.cocoabeans.material", "COCOA_BEANS");
        shopConfig.set("items.cocoabeans.price", 10);
        shopConfig.set("items.cocoabeans.sellPrice", 3);
        shopConfig.set("items.cocoabeans.amount", 1);
        shopConfig.set("items.cocoabeans.slot", 19);
        shopConfig.set("items.cocoabeans.page", 1);
        shopConfig.set("items.cocoabeans.category", "Farming");

        shopConfig.set("items.netherwart.material", "NETHER_WART");
        shopConfig.set("items.netherwart.price", 15);
        shopConfig.set("items.netherwart.sellPrice", 5);
        shopConfig.set("items.netherwart.amount", 1);
        shopConfig.set("items.netherwart.slot", 21);
        shopConfig.set("items.netherwart.page", 1);
        shopConfig.set("items.netherwart.category", "Farming");

        shopConfig.set("items.torchflowerseeds.material", "TORCHFLOWER_SEEDS");
        shopConfig.set("items.torchflowerseeds.price", 100);
        shopConfig.set("items.torchflowerseeds.sellPrice", 25);
        shopConfig.set("items.torchflowerseeds.amount", 1);
        shopConfig.set("items.torchflowerseeds.slot", 23);
        shopConfig.set("items.torchflowerseeds.page", 1);
        shopConfig.set("items.torchflowerseeds.category", "Farming");

        shopConfig.set("items.pitcherpod.material", "PITCHER_POD");
        shopConfig.set("items.pitcherpod.price", 100);
        shopConfig.set("items.pitcherpod.sellPrice", 25);
        shopConfig.set("items.pitcherpod.amount", 1);
        shopConfig.set("items.pitcherpod.slot", 25);
        shopConfig.set("items.pitcherpod.page", 1);
        shopConfig.set("items.pitcherpod.category", "Farming");

        shopConfig.set("items.bamboo.material", "BAMBOO");
        shopConfig.set("items.bamboo.price", 10);
        shopConfig.set("items.bamboo.sellPrice", 2);
        shopConfig.set("items.bamboo.amount", 1);
        shopConfig.set("items.bamboo.slot", 28);
        shopConfig.set("items.bamboo.page", 1);
        shopConfig.set("items.bamboo.category", "Farming");

        shopConfig.set("items.sweetberries.material", "SWEET_BERRIES");
        shopConfig.set("items.sweetberries.price", 8);
        shopConfig.set("items.sweetberries.sellPrice", 2.5);
        shopConfig.set("items.sweetberries.amount", 1);
        shopConfig.set("items.sweetberries.slot", 30);
        shopConfig.set("items.sweetberries.page", 1);
        shopConfig.set("items.sweetberries.category", "Farming");

        shopConfig.set("items.glowberries.material", "GLOW_BERRIES");
        shopConfig.set("items.glowberries.price", 8);
        shopConfig.set("items.glowberries.sellPrice", 3.5);
        shopConfig.set("items.glowberries.amount", 1);
        shopConfig.set("items.glowberries.slot", 32);
        shopConfig.set("items.glowberries.page", 1);
        shopConfig.set("items.glowberries.category", "Farming");

        shopConfig.set("items.kelp.material", "KELP");
        shopConfig.set("items.kelp.price", 4);
        shopConfig.set("items.kelp.sellPrice", 1);
        shopConfig.set("items.kelp.amount", 1);
        shopConfig.set("items.kelp.slot", 34);
        shopConfig.set("items.kelp.page", 1);
        shopConfig.set("items.kelp.category", "Farming");

        shopConfig.set("items.wheat.material", "WHEAT");
        shopConfig.set("items.wheat.price", 12);
        shopConfig.set("items.wheat.sellPrice", 4);
        shopConfig.set("items.wheat.amount", 1);
        shopConfig.set("items.wheat.slot", 10);
        shopConfig.set("items.wheat.page", 2);
        shopConfig.set("items.wheat.category", "Farming");

        shopConfig.set("items.carrot.material", "CARROT");
        shopConfig.set("items.carrot.price", 10);
        shopConfig.set("items.carrot.sellPrice", 3);
        shopConfig.set("items.carrot.amount", 1);
        shopConfig.set("items.carrot.slot", 12);
        shopConfig.set("items.carrot.page", 2);
        shopConfig.set("items.carrot.category", "Farming");

        shopConfig.set("items.potato.material", "POTATO");
        shopConfig.set("items.potato.price", 10);
        shopConfig.set("items.potato.sellPrice", 3);
        shopConfig.set("items.potato.amount", 1);
        shopConfig.set("items.potato.slot", 14);
        shopConfig.set("items.potato.page", 2);
        shopConfig.set("items.potato.category", "Farming");

        shopConfig.set("items.beetroot.material", "BEETROOT");
        shopConfig.set("items.beetroot.price", 10);
        shopConfig.set("items.beetroot.sellPrice", 2.5);
        shopConfig.set("items.beetroot.amount", 1);
        shopConfig.set("items.beetroot.slot", 16);
        shopConfig.set("items.beetroot.page", 2);
        shopConfig.set("items.beetroot.category", "Farming");

        shopConfig.set("items.melonslice.material", "MELON_SLICE");
        shopConfig.set("items.melonslice.price", 6);
        shopConfig.set("items.melonslice.sellPrice", 1.75);
        shopConfig.set("items.melonslice.amount", 1);
        shopConfig.set("items.melonslice.slot", 19);
        shopConfig.set("items.melonslice.page", 2);
        shopConfig.set("items.melonslice.category", "Farming");

        shopConfig.set("items.pumpkin.material", "PUMPKIN");
        shopConfig.set("items.pumpkin.price", 15);
        shopConfig.set("items.pumpkin.sellPrice", 4);
        shopConfig.set("items.pumpkin.amount", 1);
        shopConfig.set("items.pumpkin.slot", 21);
        shopConfig.set("items.pumpkin.page", 2);
        shopConfig.set("items.pumpkin.category", "Farming");

        shopConfig.set("items.carvedpumpkin.material", "CARVED_PUMPKIN");
        shopConfig.set("items.carvedpumpkin.price", 20);
        shopConfig.set("items.carvedpumpkin.sellPrice", 6);
        shopConfig.set("items.carvedpumpkin.amount", 1);
        shopConfig.set("items.carvedpumpkin.slot", 23);
        shopConfig.set("items.carvedpumpkin.page", 2);
        shopConfig.set("items.carvedpumpkin.category", "Farming");

        shopConfig.set("items.sugarcane.material", "SUGAR_CANE");
        shopConfig.set("items.sugarcane.price", 8);
        shopConfig.set("items.sugarcane.sellPrice", 2);
        shopConfig.set("items.sugarcane.amount", 1);
        shopConfig.set("items.sugarcane.slot", 25);
        shopConfig.set("items.sugarcane.page", 2);
        shopConfig.set("items.sugarcane.category", "Farming");

        shopConfig.set("items.cactus.material", "CACTUS");
        shopConfig.set("items.cactus.price", 10);
        shopConfig.set("items.cactus.sellPrice", 2.5);
        shopConfig.set("items.cactus.amount", 1);
        shopConfig.set("items.cactus.slot", 28);
        shopConfig.set("items.cactus.page", 2);
        shopConfig.set("items.cactus.category", "Farming");

        shopConfig.set("items.seapickle.material", "SEA_PICKLE");
        shopConfig.set("items.seapickle.price", 12);
        shopConfig.set("items.seapickle.sellPrice", 3);
        shopConfig.set("items.seapickle.amount", 1);
        shopConfig.set("items.seapickle.slot", 28);
        shopConfig.set("items.seapickle.page", 2);
        shopConfig.set("items.seapickle.category", "Farming");


        shopConfig.set("items.cookedporckchop.material", "COOKED_PORKCHOP");
        shopConfig.set("items.cookedporckchop.price", 40);
        shopConfig.set("items.cookedporckchop.sellPrice", 10);
        shopConfig.set("items.cookedporckchop.amount", 1);
        shopConfig.set("items.cookedporckchop.slot", 10);
        shopConfig.set("items.cookedporckchop.page", 1);
        shopConfig.set("items.cookedporckchop.category", "Food");

        shopConfig.set("items.cookedbeef.material", "COOKED_BEEF");
        shopConfig.set("items.cookedbeef.price", 45);
        shopConfig.set("items.cookedbeef.sellPrice", 11);
        shopConfig.set("items.cookedbeef.amount", 1);
        shopConfig.set("items.cookedbeef.slot", 12);
        shopConfig.set("items.cookedbeef.page", 1);
        shopConfig.set("items.cookedbeef.category", "Food");

        shopConfig.set("items.cookedchicken.material", "COOKED_CHICKEN");
        shopConfig.set("items.cookedchicken.price", 35);
        shopConfig.set("items.cookedchicken.sellPrice", 8);
        shopConfig.set("items.cookedchicken.amount", 1);
        shopConfig.set("items.cookedchicken.slot", 14);
        shopConfig.set("items.cookedchicken.page", 1);
        shopConfig.set("items.cookedchicken.category", "Food");

        shopConfig.set("items.cookedmutton.material", "COOKED_MUTTON");
        shopConfig.set("items.cookedmutton.price", 38);
        shopConfig.set("items.cookedmutton.sellPrice", 9);
        shopConfig.set("items.cookedmutton.amount", 1);
        shopConfig.set("items.cookedmutton.slot", 16);
        shopConfig.set("items.cookedmutton.page", 1);
        shopConfig.set("items.cookedmutton.category", "Food");

        shopConfig.set("items.cookedrabbit.material", "COOKED_RABBIT");
        shopConfig.set("items.cookedrabbit.price", 30);
        shopConfig.set("items.cookedrabbit.sellPrice", 7.5);
        shopConfig.set("items.cookedrabbit.amount", 1);
        shopConfig.set("items.cookedrabbit.slot", 19);
        shopConfig.set("items.cookedrabbit.page", 1);
        shopConfig.set("items.cookedrabbit.category", "Food");

        shopConfig.set("items.cookedcod.material", "COOKED_COD");
        shopConfig.set("items.cookedcod.price", 25);
        shopConfig.set("items.cookedcod.sellPrice", 6);
        shopConfig.set("items.cookedcod.amount", 1);
        shopConfig.set("items.cookedcod.slot", 21);
        shopConfig.set("items.cookedcod.page", 1);
        shopConfig.set("items.cookedcod.category", "Food");

        shopConfig.set("items.cookedsalmon.material", "COOKED_SALMON");
        shopConfig.set("items.cookedsalmon.price", 26);
        shopConfig.set("items.cookedsalmon.sellPrice", 7);
        shopConfig.set("items.cookedsalmon.amount", 1);
        shopConfig.set("items.cookedsalmon.slot", 23);
        shopConfig.set("items.cookedsalmon.page", 1);
        shopConfig.set("items.cookedsalmon.category", "Food");

        shopConfig.set("items.rawporkchop.material", "PORKCHOP");
        shopConfig.set("items.rawporkchop.price", 20);
        shopConfig.set("items.rawporkchop.sellPrice", 5);
        shopConfig.set("items.rawporkchop.amount", 1);
        shopConfig.set("items.rawporkchop.slot", 25);
        shopConfig.set("items.rawporkchop.page", 1);
        shopConfig.set("items.rawporkchop.category", "Food");

        shopConfig.set("items.rawbeef.material", "BEEF");
        shopConfig.set("items.rawbeef.price", 22);
        shopConfig.set("items.rawbeef.sellPrice", 5.5);
        shopConfig.set("items.rawbeef.amount", 1);
        shopConfig.set("items.rawbeef.slot", 28);
        shopConfig.set("items.rawbeef.page", 1);
        shopConfig.set("items.rawbeef.category", "Food");

        shopConfig.set("items.rawchicken.material", "CHICKEN");
        shopConfig.set("items.rawchicken.price", 18);
        shopConfig.set("items.rawchicken.sellPrice", 4);
        shopConfig.set("items.rawchicken.amount", 1);
        shopConfig.set("items.rawchicken.slot", 30);
        shopConfig.set("items.rawchicken.page", 1);
        shopConfig.set("items.rawchicken.category", "Food");

        shopConfig.set("items.rawmutton.material", "MUTTON");
        shopConfig.set("items.rawmutton.price", 19);
        shopConfig.set("items.rawmutton.sellPrice", 4.5);
        shopConfig.set("items.rawmutton.amount", 1);
        shopConfig.set("items.rawmutton.slot", 32);
        shopConfig.set("items.rawmutton.page", 1);
        shopConfig.set("items.rawmutton.category", "Food");

        shopConfig.set("items.rawrabbit.material", "RABBIT");
        shopConfig.set("items.rawrabbit.price", 15);
        shopConfig.set("items.rawrabbit.sellPrice", 3.5);
        shopConfig.set("items.rawrabbit.amount", 1);
        shopConfig.set("items.rawrabbit.slot", 34);
        shopConfig.set("items.rawrabbit.page", 1);
        shopConfig.set("items.rawrabbit.category", "Food");

        shopConfig.set("items.rawcod.material", "COD");
        shopConfig.set("items.rawcod.price", 12);
        shopConfig.set("items.rawcod.sellPrice", 3);
        shopConfig.set("items.rawcod.amount", 1);
        shopConfig.set("items.rawcod.slot", 10);
        shopConfig.set("items.rawcod.page", 2);
        shopConfig.set("items.rawcod.category", "Food");

        shopConfig.set("items.rawsalmon.material", "SALMON");
        shopConfig.set("items.rawsalmon.price", 14);
        shopConfig.set("items.rawsalmon.sellPrice", 3.5);
        shopConfig.set("items.rawsalmon.amount", 1);
        shopConfig.set("items.rawsalmon.slot", 12);
        shopConfig.set("items.rawsalmon.page", 2);
        shopConfig.set("items.rawsalmon.category", "Food");

        shopConfig.set("items.bread.material", "BREAD");
        shopConfig.set("items.bread.price", 15);
        shopConfig.set("items.bread.sellPrice", 3.5);
        shopConfig.set("items.bread.amount", 1);
        shopConfig.set("items.bread.slot", 14);
        shopConfig.set("items.bread.page", 2);
        shopConfig.set("items.bread.category", "Food");

        shopConfig.set("items.bakedpotato.material", "BAKED_POTATO");
        shopConfig.set("items.bakedpotato.price", 12);
        shopConfig.set("items.bakedpotato.sellPrice", 3);
        shopConfig.set("items.bakedpotato.amount", 1);
        shopConfig.set("items.bakedpotato.slot", 16);
        shopConfig.set("items.bakedpotato.page", 2);
        shopConfig.set("items.bakedpotato.category", "Food");

        shopConfig.set("items.carrot1.material", "CARROT");
        shopConfig.set("items.carrot1.price", 10);
        shopConfig.set("items.carrot1.sellPrice", 2.5);
        shopConfig.set("items.carrot1.amount", 1);
        shopConfig.set("items.carrot1.slot", 19);
        shopConfig.set("items.carrot1.page", 2);
        shopConfig.set("items.carrot1.category", "Food");

        shopConfig.set("items.potato1.material", "POTATO");
        shopConfig.set("items.potato1.price", 8);
        shopConfig.set("items.potato1.sellPrice", 2);
        shopConfig.set("items.potato1.amount", 1);
        shopConfig.set("items.potato1.slot", 21);
        shopConfig.set("items.potato1.page", 2);
        shopConfig.set("items.potato1.category", "Food");

        shopConfig.set("items.beetroot1.material", "BEETROOT");
        shopConfig.set("items.beetroot1.price", 6);
        shopConfig.set("items.beetroot1.sellPrice", 1.5);
        shopConfig.set("items.beetroot1.amount", 1);
        shopConfig.set("items.beetroot1.slot", 23);
        shopConfig.set("items.beetroot1.page", 2);
        shopConfig.set("items.beetroot1.category", "Food");

        shopConfig.set("items.melonslic1e.material", "MELON_SLICE");
        shopConfig.set("items.melonslic1e.price", 5);
        shopConfig.set("items.melonslic1e.sellPrice", 1);
        shopConfig.set("items.melonslic1e.amount", 1);
        shopConfig.set("items.melonslic1e.slot", 25);
        shopConfig.set("items.melonslic1e.page", 2);
        shopConfig.set("items.melonslic1e.category", "Food");

        shopConfig.set("items.pumpkinpie.material", "PUMPKIN_PIE");
        shopConfig.set("items.pumpkinpie.price", 20);
        shopConfig.set("items.pumpkinpie.sellPrice", 5);
        shopConfig.set("items.pumpkinpie.amount", 1);
        shopConfig.set("items.pumpkinpie.slot", 28);
        shopConfig.set("items.pumpkinpie.page", 2);
        shopConfig.set("items.pumpkinpie.category", "Food");

        shopConfig.set("items.apple1.material", "APPLE");
        shopConfig.set("items.apple1.price", 12);
        shopConfig.set("items.apple1.sellPrice", 3);
        shopConfig.set("items.apple1.amount", 1);
        shopConfig.set("items.apple1.slot", 30);
        shopConfig.set("items.apple1.page", 2);
        shopConfig.set("items.apple1.category", "Food");

        shopConfig.set("items.sweetberries1.material", "SWEET_BERRIES");
        shopConfig.set("items.sweetberries1.price", 6);
        shopConfig.set("items.sweetberries1.sellPrice", 1.5);
        shopConfig.set("items.sweetberries1.amount", 1);
        shopConfig.set("items.sweetberries1.slot", 32);
        shopConfig.set("items.sweetberries1.page", 2);
        shopConfig.set("items.sweetberries1.category", "Food");

        shopConfig.set("items.glowberries1.material", "GLOW_BERRIES");
        shopConfig.set("items.glowberries1.price", 8);
        shopConfig.set("items.glowberries1.sellPrice", 2);
        shopConfig.set("items.glowberries1.amount", 1);
        shopConfig.set("items.glowberries1.slot", 34);
        shopConfig.set("items.glowberries1.page", 2);
        shopConfig.set("items.glowberries1.category", "Food");

        shopConfig.set("items.chorusfruit1.material", "CHORUS_FRUIT");
        shopConfig.set("items.chorusfruit1.price", 10);
        shopConfig.set("items.chorusfruit1.sellPrice", 2.5);
        shopConfig.set("items.chorusfruit1.amount", 1);
        shopConfig.set("items.chorusfruit1.slot", 10);
        shopConfig.set("items.chorusfruit1.page", 3);
        shopConfig.set("items.chorusfruit1.category", "Food");

        shopConfig.set("items.driedkelp.material", "DRIED_KELP");
        shopConfig.set("items.driedkelp.price", 4);
        shopConfig.set("items.driedkelp.sellPrice", 1);
        shopConfig.set("items.driedkelp.amount", 1);
        shopConfig.set("items.driedkelp.slot", 12);
        shopConfig.set("items.driedkelp.page", 3);
        shopConfig.set("items.driedkelp.category", "Food");

        shopConfig.set("items.cake.material", "CAKE");
        shopConfig.set("items.cake.price", 50);
        shopConfig.set("items.cake.sellPrice", 12);
        shopConfig.set("items.cake.amount", 1);
        shopConfig.set("items.cake.slot", 14);
        shopConfig.set("items.cake.page", 3);
        shopConfig.set("items.cake.category", "Food");

        shopConfig.set("items.cookie.material", "COOKIE");
        shopConfig.set("items.cookie.price", 8);
        shopConfig.set("items.cookie.sellPrice", 2);
        shopConfig.set("items.cookie.amount", 1);
        shopConfig.set("items.cookie.slot", 16);
        shopConfig.set("items.cookie.page", 3);
        shopConfig.set("items.cookie.category", "Food");

        shopConfig.set("items.mushroomstew.material", "MUSHROOM_STEW");
        shopConfig.set("items.mushroomstew.price", 15);
        shopConfig.set("items.mushroomstew.sellPrice", 3);
        shopConfig.set("items.mushroomstew.amount", 1);
        shopConfig.set("items.mushroomstew.slot", 19);
        shopConfig.set("items.mushroomstew.page", 3);
        shopConfig.set("items.mushroomstew.category", "Food");

        shopConfig.set("items.beetrootsoupo.material", "BEETROOT_SOUP");
        shopConfig.set("items.beetrootsoupo.price", 14);
        shopConfig.set("items.beetrootsoupo.sellPrice", 3);
        shopConfig.set("items.beetrootsoupo.amount", 1);
        shopConfig.set("items.beetrootsoupo.slot", 21);
        shopConfig.set("items.beetrootsoupo.page", 3);
        shopConfig.set("items.beetrootsoupo.category", "Food");

        shopConfig.set("items.rabbitstew.material", "RABBIT_STEW");
        shopConfig.set("items.rabbitstew.price", 25);
        shopConfig.set("items.rabbitstew.sellPrice", 6);
        shopConfig.set("items.rabbitstew.amount", 1);
        shopConfig.set("items.rabbitstew.slot", 23);
        shopConfig.set("items.rabbitstew.page", 3);
        shopConfig.set("items.rabbitstew.category", "Food");

        shopConfig.set("items.goldencarrot.material", "GOLDEN_CARROT");
        shopConfig.set("items.goldencarrot.price", 100);
        shopConfig.set("items.goldencarrot.sellPrice", 20);
        shopConfig.set("items.goldencarrot.amount", 1);
        shopConfig.set("items.goldencarrot.slot", 25);
        shopConfig.set("items.goldencarrot.page", 3);
        shopConfig.set("items.goldencarrot.category", "Food");

        shopConfig.set("items.goldenapple.material", "GOLDEN_APPLE");
        shopConfig.set("items.goldenapple.price", 500);
        shopConfig.set("items.goldenapple.sellPrice", 120);
        shopConfig.set("items.goldenapple.amount", 1);
        shopConfig.set("items.goldenapple.slot", 28);
        shopConfig.set("items.goldenapple.page", 3);
        shopConfig.set("items.goldenapple.category", "Food");

        shopConfig.set("items.enchantedgoldenapple.material", "ENCHANTED_GOLDEN_APPLE");
        shopConfig.set("items.enchantedgoldenapple.price", 3000);
        shopConfig.set("items.enchantedgoldenapple.sellPrice", -1);
        shopConfig.set("items.enchantedgoldenapple.amount", 1);
        shopConfig.set("items.enchantedgoldenapple.slot", 30);
        shopConfig.set("items.enchantedgoldenapple.page", 3);
        shopConfig.set("items.enchantedgoldenapple.category", "Food");

        shopConfig.set("items.honeybottle.material", "HONEY_BOTTLE");
        shopConfig.set("items.honeybottle.price", 20);
        shopConfig.set("items.honeybottle.sellPrice", 5);
        shopConfig.set("items.honeybottle.amount", 1);
        shopConfig.set("items.honeybottle.slot", 32);
        shopConfig.set("items.honeybottle.page", 3);
        shopConfig.set("items.honeybottle.category", "Food");

        shopConfig.set("items.pufferfish.material", "PUFFERFISH");
        shopConfig.set("items.pufferfish.price", 15);
        shopConfig.set("items.pufferfish.sellPrice", 3);
        shopConfig.set("items.pufferfish.amount", 1);
        shopConfig.set("items.pufferfish.slot", 34);
        shopConfig.set("items.pufferfish.page", 3);
        shopConfig.set("items.pufferfish.category", "Food");



        shopConfig.set("items.redstonedust.material", "REDSTONE");
        shopConfig.set("items.redstonedust.price", 10);
        shopConfig.set("items.redstonedust.sellPrice", 2);
        shopConfig.set("items.redstonedust.amount", 1);
        shopConfig.set("items.redstonedust.slot", 10);
        shopConfig.set("items.redstonedust.page", 1);
        shopConfig.set("items.redstonedust.category", "Redstone");

        shopConfig.set("items.redstonetorch.material", "REDSTONE_TORCH");
        shopConfig.set("items.redstonetorch.price", 12);
        shopConfig.set("items.redstonetorch.sellPrice", 3);
        shopConfig.set("items.redstonetorch.amount", 1);
        shopConfig.set("items.redstonetorch.slot", 12);
        shopConfig.set("items.redstonetorch.page", 1);
        shopConfig.set("items.redstonetorch.category", "Redstone");

        shopConfig.set("items.redstoneblock.material", "REDSTONE_BLOCK");
        shopConfig.set("items.redstoneblock.price", 90);
        shopConfig.set("items.redstoneblock.sellPrice", 25);
        shopConfig.set("items.redstoneblock.amount", 1);
        shopConfig.set("items.redstoneblock.slot", 14);
        shopConfig.set("items.redstoneblock.page", 1);
        shopConfig.set("items.redstoneblock.category", "Redstone");

        shopConfig.set("items.repeater.material", "REPEATER");
        shopConfig.set("items.repeater.price", 25);
        shopConfig.set("items.repeater.sellPrice", 6);
        shopConfig.set("items.repeater.amount", 1);
        shopConfig.set("items.repeater.slot", 16);
        shopConfig.set("items.repeater.page", 1);
        shopConfig.set("items.repeater.category", "Redstone");

        shopConfig.set("items.comparator.material", "COMPARATOR");
        shopConfig.set("items.comparator.price", 30);
        shopConfig.set("items.comparator.sellPrice", 7);
        shopConfig.set("items.comparator.amount", 1);
        shopConfig.set("items.comparator.slot", 19);
        shopConfig.set("items.comparator.page", 1);
        shopConfig.set("items.comparator.category", "Redstone");

        shopConfig.set("items.piston.material", "PISTON");
        shopConfig.set("items.piston.price", 20);
        shopConfig.set("items.piston.sellPrice", 5);
        shopConfig.set("items.piston.amount", 1);
        shopConfig.set("items.piston.slot", 21);
        shopConfig.set("items.piston.page", 1);
        shopConfig.set("items.piston.category", "Redstone");

        shopConfig.set("items.stickypiston.material", "STICKY_PISTON");
        shopConfig.set("items.stickypiston.price", 30);
        shopConfig.set("items.stickypiston.sellPrice", 7);
        shopConfig.set("items.stickypiston.amount", 1);
        shopConfig.set("items.stickypiston.slot", 23);
        shopConfig.set("items.stickypiston.page", 1);
        shopConfig.set("items.stickypiston.category", "Redstone");

        shopConfig.set("items.observer.material", "OBSERVER");
        shopConfig.set("items.observer.price", 35);
        shopConfig.set("items.observer.sellPrice", 8);
        shopConfig.set("items.observer.amount", 1);
        shopConfig.set("items.observer.slot", 25);
        shopConfig.set("items.observer.page", 1);
        shopConfig.set("items.observer.category", "Redstone");

        shopConfig.set("items.hopper.material", "HOPPER");
        shopConfig.set("items.hopper.price", 50);
        shopConfig.set("items.hopper.sellPrice", 12);
        shopConfig.set("items.hopper.amount", 1);
        shopConfig.set("items.hopper.slot", 28);
        shopConfig.set("items.hopper.page", 1);
        shopConfig.set("items.hopper.category", "Redstone");

        shopConfig.set("items.dropper.material", "DROPPER");
        shopConfig.set("items.dropper.price", 20);
        shopConfig.set("items.dropper.sellPrice", 5);
        shopConfig.set("items.dropper.amount", 1);
        shopConfig.set("items.dropper.slot", 30);
        shopConfig.set("items.dropper.page", 1);
        shopConfig.set("items.dropper.category", "Redstone");

        shopConfig.set("items.dispenser.material", "DISPENSER");
        shopConfig.set("items.dispenser.price", 25);
        shopConfig.set("items.dispenser.sellPrice", 6);
        shopConfig.set("items.dispenser.amount", 1);
        shopConfig.set("items.dispenser.slot", 32);
        shopConfig.set("items.dispenser.page", 1);
        shopConfig.set("items.dispenser.category", "Redstone");

        shopConfig.set("items.tntminecart.material", "TNT_MINECART");
        shopConfig.set("items.tntminecart.price", 55);
        shopConfig.set("items.tntminecart.sellPrice", 13);
        shopConfig.set("items.tntminecart.amount", 1);
        shopConfig.set("items.tntminecart.slot", 34);
        shopConfig.set("items.tntminecart.page", 1);
        shopConfig.set("items.tntminecart.category", "Redstone");

        shopConfig.set("items.lever.material", "LEVER");
        shopConfig.set("items.lever.price", 6);
        shopConfig.set("items.lever.sellPrice", 1.5);
        shopConfig.set("items.lever.amount", 1);
        shopConfig.set("items.lever.slot", 10);
        shopConfig.set("items.lever.page", 2);
        shopConfig.set("items.lever.category", "Redstone");

        shopConfig.set("items.stonebutton.material", "STONE_BUTTON");
        shopConfig.set("items.stonebutton.price", 4);
        shopConfig.set("items.stonebutton.sellPrice", 1);
        shopConfig.set("items.stonebutton.amount", 1);
        shopConfig.set("items.stonebutton.slot", 12);
        shopConfig.set("items.stonebutton.page", 2);
        shopConfig.set("items.stonebutton.category", "Redstone");

        shopConfig.set("items.oakpressureplate.material", "OAK_PRESSURE_PLATE");
        shopConfig.set("items.oakpressureplate.price", 6);
        shopConfig.set("items.oakpressureplate.sellPrice", 1.5);
        shopConfig.set("items.oakpressureplate.amount", 1);
        shopConfig.set("items.oakpressureplate.slot", 14);
        shopConfig.set("items.oakpressureplate.page", 2);
        shopConfig.set("items.oakpressureplate.category", "Redstone");

        shopConfig.set("items.tripwirehook.material", "TRIPWIRE_HOOK");
        shopConfig.set("items.tripwirehook.price", 8);
        shopConfig.set("items.tripwirehook.sellPrice", 2);
        shopConfig.set("items.tripwirehook.amount", 1);
        shopConfig.set("items.tripwirehook.slot", 16);
        shopConfig.set("items.tripwirehook.page", 2);
        shopConfig.set("items.tripwirehook.category", "Redstone");

        shopConfig.set("items.daylightdetector.material", "DAYLIGHT_DETECTOR");
        shopConfig.set("items.daylightdetector.price", 30);
        shopConfig.set("items.daylightdetector.sellPrice", 7.5);
        shopConfig.set("items.daylightdetector.amount", 1);
        shopConfig.set("items.daylightdetector.slot", 19);
        shopConfig.set("items.daylightdetector.page", 2);
        shopConfig.set("items.daylightdetector.category", "Redstone");

        shopConfig.set("items.targetblock.material", "TARGET");
        shopConfig.set("items.targetblock.price", 18);
        shopConfig.set("items.targetblock.sellPrice", 4.5);
        shopConfig.set("items.targetblock.amount", 1);
        shopConfig.set("items.targetblock.slot", 21);
        shopConfig.set("items.targetblock.page", 2);
        shopConfig.set("items.targetblock.category", "Redstone");

        shopConfig.set("items.trappedchest.material", "TRAPPED_CHEST");
        shopConfig.set("items.trappedchest.price", 25);
        shopConfig.set("items.trappedchest.sellPrice", 6.25);
        shopConfig.set("items.trappedchest.amount", 1);
        shopConfig.set("items.trappedchest.slot", 23);
        shopConfig.set("items.trappedchest.page", 2);
        shopConfig.set("items.trappedchest.category", "Redstone");

        shopConfig.set("items.poweredrail.material", "POWERED_RAIL");
        shopConfig.set("items.poweredrail.price", 15);
        shopConfig.set("items.poweredrail.sellPrice", 4);
        shopConfig.set("items.poweredrail.amount", 1);
        shopConfig.set("items.poweredrail.slot", 25);
        shopConfig.set("items.poweredrail.page", 2);
        shopConfig.set("items.poweredrail.category", "Redstone");

        shopConfig.set("items.activatorrail.material", "ACTIVATOR_RAIL");
        shopConfig.set("items.activatorrail.price", 12);
        shopConfig.set("items.activatorrail.sellPrice", 3);
        shopConfig.set("items.activatorrail.amount", 1);
        shopConfig.set("items.activatorrail.slot", 28);
        shopConfig.set("items.activatorrail.page", 2);
        shopConfig.set("items.activatorrail.category", "Redstone");

        shopConfig.set("items.detectorrail.material", "DETECTOR_RAIL");
        shopConfig.set("items.detectorrail.price", 12);
        shopConfig.set("items.detectorrail.sellPrice", 3);
        shopConfig.set("items.detectorrail.amount", 1);
        shopConfig.set("items.detectorrail.slot", 30);
        shopConfig.set("items.detectorrail.page", 2);
        shopConfig.set("items.detectorrail.category", "Redstone");

        shopConfig.set("items.rail.material", "RAIL");
        shopConfig.set("items.rail.price", 6);
        shopConfig.set("items.rail.sellPrice", 1.5);
        shopConfig.set("items.rail.amount", 1);
        shopConfig.set("items.rail.slot", 32);
        shopConfig.set("items.rail.page", 2);
        shopConfig.set("items.rail.category", "Redstone");

        shopConfig.set("items.bell.material", "BELL");
        shopConfig.set("items.bell.price", 15);
        shopConfig.set("items.bell.sellPrice", 4);
        shopConfig.set("items.bell.amount", 1);
        shopConfig.set("items.bell.slot", 34);
        shopConfig.set("items.bell.page", 2);
        shopConfig.set("items.bell.category", "Redstone");

        shopConfig.set("items.noteblock.material", "NOTE_BLOCK");
        shopConfig.set("items.noteblock.price", 20);
        shopConfig.set("items.noteblock.sellPrice", 5);
        shopConfig.set("items.noteblock.amount", 1);
        shopConfig.set("items.noteblock.slot", 10);
        shopConfig.set("items.noteblock.page", 3);
        shopConfig.set("items.noteblock.category", "Redstone");

        shopConfig.set("items.tnt1.material", "TNT");
        shopConfig.set("items.tnt1.price", 50);
        shopConfig.set("items.tnt1.sellPrice", 12.5);
        shopConfig.set("items.tnt1.amount", 1);
        shopConfig.set("items.tnt1.slot", 12);
        shopConfig.set("items.tnt1.page", 3);
        shopConfig.set("items.tnt1.category", "Redstone");

        shopConfig.set("items.lightningrod.material", "LIGHTNING_ROD");
        shopConfig.set("items.lightningrod.price", 15);
        shopConfig.set("items.lightningrod.sellPrice", 3.5);
        shopConfig.set("items.lightningrod.amount", 1);
        shopConfig.set("items.lightningrod.slot", 14);
        shopConfig.set("items.lightningrod.page", 3);
        shopConfig.set("items.lightningrod.category", "Redstone");

        shopConfig.set("items.sculksensor.material", "SCULK_SENSOR");
        shopConfig.set("items.sculksensor.price", 80);
        shopConfig.set("items.sculksensor.sellPrice", 20);
        shopConfig.set("items.sculksensor.amount", 1);
        shopConfig.set("items.sculksensor.slot", 16);
        shopConfig.set("items.sculksensor.page", 3);
        shopConfig.set("items.sculksensor.category", "Redstone");

        shopConfig.set("items.minecart.material", "MINECART");
        shopConfig.set("items.minecart.price", 25);
        shopConfig.set("items.minecart.sellPrice", 6);
        shopConfig.set("items.minecart.amount", 1);
        shopConfig.set("items.minecart.slot", 19);
        shopConfig.set("items.minecart.page", 3);
        shopConfig.set("items.minecart.category", "Redstone");

        shopConfig.set("items.furnaceminecart.material", "FURNACE_MINECART");
        shopConfig.set("items.furnaceminecart.price", 30);
        shopConfig.set("items.furnaceminecart.sellPrice", 7);
        shopConfig.set("items.furnaceminecart.amount", 1);
        shopConfig.set("items.furnaceminecart.slot", 21);
        shopConfig.set("items.furnaceminecart.page", 3);
        shopConfig.set("items.furnaceminecart.category", "Redstone");

        shopConfig.set("items.chestminecart.material", "CHEST_MINECART");
        shopConfig.set("items.chestminecart.price", 32);
        shopConfig.set("items.chestminecart.sellPrice", 8);
        shopConfig.set("items.chestminecart.amount", 1);
        shopConfig.set("items.chestminecart.slot", 23);
        shopConfig.set("items.chestminecart.page", 3);
        shopConfig.set("items.chestminecart.category", "Redstone");

        shopConfig.set("items.hopperminecart.material", "HOPPER_MINECART");
        shopConfig.set("items.hopperminecart.price", 45);
        shopConfig.set("items.hopperminecart.sellPrice", 11);
        shopConfig.set("items.hopperminecart.amount", 1);
        shopConfig.set("items.hopperminecart.slot", 25);
        shopConfig.set("items.hopperminecart.page", 3);
        shopConfig.set("items.hopperminecart.category", "Redstone");



        shopConfig.set("items.oaklog.material", "OAK_LOG");
        shopConfig.set("items.oaklog.price", 12);
        shopConfig.set("items.oaklog.sellPrice", 3);
        shopConfig.set("items.oaklog.amount", 1);
        shopConfig.set("items.oaklog.slot", 10);
        shopConfig.set("items.oaklog.page", 1);
        shopConfig.set("items.oaklog.category", "Wood");

        shopConfig.set("items.strippedoaklog.material", "STRIPPED_OAK_LOG");
        shopConfig.set("items.strippedoaklog.price", 13);
        shopConfig.set("items.strippedoaklog.sellPrice", 3.5);
        shopConfig.set("items.strippedoaklog.amount", 1);
        shopConfig.set("items.strippedoaklog.slot", 12);
        shopConfig.set("items.strippedoaklog.page", 1);
        shopConfig.set("items.strippedoaklog.category", "Wood");

        shopConfig.set("items.oakwood.material", "OAK_WOOD");
        shopConfig.set("items.oakwood.price", 12);
        shopConfig.set("items.oakwood.sellPrice", 3);
        shopConfig.set("items.oakwood.amount", 1);
        shopConfig.set("items.oakwood.slot", 14);
        shopConfig.set("items.oakwood.page", 1);
        shopConfig.set("items.oakwood.category", "Wood");

        shopConfig.set("items.strippedoakwood.material", "STRIPPED_OAK_WOOD");
        shopConfig.set("items.strippedoakwood.price", 13);
        shopConfig.set("items.strippedoakwood.sellPrice", 3.5);
        shopConfig.set("items.strippedoakwood.amount", 1);
        shopConfig.set("items.strippedoakwood.slot", 16);
        shopConfig.set("items.strippedoakwood.page", 1);
        shopConfig.set("items.strippedoakwood.category", "Wood");

        shopConfig.set("items.oakplanks.material", "OAK_PLANKS");
        shopConfig.set("items.oakplanks.price", 4);
        shopConfig.set("items.oakplanks.sellPrice", 1);
        shopConfig.set("items.oakplanks.amount", 1);
        shopConfig.set("items.oakplanks.slot", 19);
        shopConfig.set("items.oakplanks.page", 1);
        shopConfig.set("items.oakplanks.category", "Wood");

        shopConfig.set("items.oakslab.material", "OAK_SLAB");
        shopConfig.set("items.oakslab.price", 2);
        shopConfig.set("items.oakslab.sellPrice", 0.5);
        shopConfig.set("items.oakslab.amount", 1);
        shopConfig.set("items.oakslab.slot", 21);
        shopConfig.set("items.oakslab.page", 1);
        shopConfig.set("items.oakslab.category", "Wood");

        shopConfig.set("items.oakstairs.material", "OAK_STAIRS");
        shopConfig.set("items.oakstairs.price", 5);
        shopConfig.set("items.oakstairs.sellPrice", 1.25);
        shopConfig.set("items.oakstairs.amount", 1);
        shopConfig.set("items.oakstairs.slot", 23);
        shopConfig.set("items.oakstairs.page", 1);
        shopConfig.set("items.oakstairs.category", "Wood");

        shopConfig.set("items.oakfence.material", "OAK_FENCE");
        shopConfig.set("items.oakfence.price", 4);
        shopConfig.set("items.oakfence.sellPrice", 1);
        shopConfig.set("items.oakfence.amount", 1);
        shopConfig.set("items.oakfence.slot", 25);
        shopConfig.set("items.oakfence.page", 1);
        shopConfig.set("items.oakfence.category", "Wood");

        shopConfig.set("items.oakfencegate.material", "OAK_FENCE_GATE");
        shopConfig.set("items.oakfencegate.price", 6);
        shopConfig.set("items.oakfencegate.sellPrice", 1.5);
        shopConfig.set("items.oakfencegate.amount", 1);
        shopConfig.set("items.oakfencegate.slot", 28);
        shopConfig.set("items.oakfencegate.page", 1);
        shopConfig.set("items.oakfencegate.category", "Wood");

        shopConfig.set("items.oakdoor.material", "OAK_DOOR");
        shopConfig.set("items.oakdoor.price", 5);
        shopConfig.set("items.oakdoor.sellPrice", 1.25);
        shopConfig.set("items.oakdoor.amount", 1);
        shopConfig.set("items.oakdoor.slot", 30);
        shopConfig.set("items.oakdoor.page", 1);
        shopConfig.set("items.oakdoor.category", "Wood");

        shopConfig.set("items.oaktrapdoor.material", "OAK_TRAPDOOR");
        shopConfig.set("items.oaktrapdoor.price", 5);
        shopConfig.set("items.oaktrapdoor.sellPrice", 1.25);
        shopConfig.set("items.oaktrapdoor.amount", 1);
        shopConfig.set("items.oaktrapdoor.slot", 32);
        shopConfig.set("items.oaktrapdoor.page", 1);
        shopConfig.set("items.oaktrapdoor.category", "Wood");

        shopConfig.set("items.oakbutton.material", "OAK_BUTTON");
        shopConfig.set("items.oakbutton.price", 1);
        shopConfig.set("items.oakbutton.sellPrice", 0.25);
        shopConfig.set("items.oakbutton.amount", 1);
        shopConfig.set("items.oakbutton.slot", 34);
        shopConfig.set("items.oakbutton.page", 1);
        shopConfig.set("items.oakbutton.category", "Wood");

        shopConfig.set("items.sprucelog.material", "SPRUCE_LOG");
        shopConfig.set("items.sprucelog.price", 12);
        shopConfig.set("items.sprucelog.sellPrice", 3);
        shopConfig.set("items.sprucelog.amount", 1);
        shopConfig.set("items.sprucelog.slot", 10);
        shopConfig.set("items.sprucelog.page", 2);
        shopConfig.set("items.sprucelog.category", "Wood");

        shopConfig.set("items.strippedsprucelog.material", "STRIPPED_SPRUCE_LOG");
        shopConfig.set("items.strippedsprucelog.price", 13);
        shopConfig.set("items.strippedsprucelog.sellPrice", 3.5);
        shopConfig.set("items.strippedsprucelog.amount", 1);
        shopConfig.set("items.strippedsprucelog.slot", 12);
        shopConfig.set("items.strippedsprucelog.page", 2);
        shopConfig.set("items.strippedsprucelog.category", "Wood");

        shopConfig.set("items.sprucewood.material", "SPRUCE_WOOD");
        shopConfig.set("items.sprucewood.price", 12);
        shopConfig.set("items.sprucewood.sellPrice", 3);
        shopConfig.set("items.sprucewood.amount", 1);
        shopConfig.set("items.sprucewood.slot", 14);
        shopConfig.set("items.sprucewood.page", 2);
        shopConfig.set("items.sprucewood.category", "Wood");

        shopConfig.set("items.strippedsprucewood.material", "STRIPPED_SPRUCE_WOOD");
        shopConfig.set("items.strippedsprucewood.price", 13);
        shopConfig.set("items.strippedsprucewood.sellPrice", 3.5);
        shopConfig.set("items.strippedsprucewood.amount", 1);
        shopConfig.set("items.strippedsprucewood.slot", 16);
        shopConfig.set("items.strippedsprucewood.page", 2);
        shopConfig.set("items.strippedsprucewood.category", "Wood");

        shopConfig.set("items.spruceplanks.material", "SPRUCE_PLANKS");
        shopConfig.set("items.spruceplanks.price", 4);
        shopConfig.set("items.spruceplanks.sellPrice", 1);
        shopConfig.set("items.spruceplanks.amount", 1);
        shopConfig.set("items.spruceplanks.slot", 19);
        shopConfig.set("items.spruceplanks.page", 2);
        shopConfig.set("items.spruceplanks.category", "Wood");

        shopConfig.set("items.spruceslab.material", "SPRUCE_SLAB");
        shopConfig.set("items.spruceslab.price", 2);
        shopConfig.set("items.spruceslab.sellPrice", 0.5);
        shopConfig.set("items.spruceslab.amount", 1);
        shopConfig.set("items.spruceslab.slot", 21);
        shopConfig.set("items.spruceslab.page", 2);
        shopConfig.set("items.spruceslab.category", "Wood");

        shopConfig.set("items.sprucestairs.material", "SPRUCE_STAIRS");
        shopConfig.set("items.sprucestairs.price", 5);
        shopConfig.set("items.sprucestairs.sellPrice", 1.25);
        shopConfig.set("items.sprucestairs.amount", 1);
        shopConfig.set("items.sprucestairs.slot", 23);
        shopConfig.set("items.sprucestairs.page", 2);
        shopConfig.set("items.sprucestairs.category", "Wood");

        shopConfig.set("items.sprucefence.material", "SPRUCE_FENCE");
        shopConfig.set("items.sprucefence.price", 4);
        shopConfig.set("items.sprucefence.sellPrice", 1);
        shopConfig.set("items.sprucefence.amount", 1);
        shopConfig.set("items.sprucefence.slot", 25);
        shopConfig.set("items.sprucefence.page", 2);
        shopConfig.set("items.sprucefence.category", "Wood");

        shopConfig.set("items.sprucefencegate.material", "SPRUCE_FENCE_GATE");
        shopConfig.set("items.sprucefencegate.price", 6);
        shopConfig.set("items.sprucefencegate.sellPrice", 1.5);
        shopConfig.set("items.sprucefencegate.amount", 1);
        shopConfig.set("items.sprucefencegate.slot", 28);
        shopConfig.set("items.sprucefencegate.page", 2);
        shopConfig.set("items.sprucefencegate.category", "Wood");

        shopConfig.set("items.sprucedoor.material", "SPRUCE_DOOR");
        shopConfig.set("items.sprucedoor.price", 5);
        shopConfig.set("items.sprucedoor.sellPrice", 1.25);
        shopConfig.set("items.sprucedoor.amount", 1);
        shopConfig.set("items.sprucedoor.slot", 30);
        shopConfig.set("items.sprucedoor.page", 2);
        shopConfig.set("items.sprucedoor.category", "Wood");

        shopConfig.set("items.sprucetrapdoor.material", "SPRUCE_TRAPDOOR");
        shopConfig.set("items.sprucetrapdoor.price", 5);
        shopConfig.set("items.sprucetrapdoor.sellPrice", 1.25);
        shopConfig.set("items.sprucetrapdoor.amount", 1);
        shopConfig.set("items.sprucetrapdoor.slot", 32);
        shopConfig.set("items.sprucetrapdoor.page", 2);
        shopConfig.set("items.sprucetrapdoor.category", "Wood");

        shopConfig.set("items.sprucebutton.material", "SPRUCE_BUTTON");
        shopConfig.set("items.sprucebutton.price", 1);
        shopConfig.set("items.sprucebutton.sellPrice", 0.25);
        shopConfig.set("items.sprucebutton.amount", 1);
        shopConfig.set("items.sprucebutton.slot", 34);
        shopConfig.set("items.sprucebutton.page", 2);
        shopConfig.set("items.sprucebutton.category", "Wood");

        shopConfig.set("items.birchlog.material", "BIRCH_LOG");
        shopConfig.set("items.birchlog.price", 12);
        shopConfig.set("items.birchlog.sellPrice", 3);
        shopConfig.set("items.birchlog.amount", 1);
        shopConfig.set("items.birchlog.slot", 10);
        shopConfig.set("items.birchlog.page", 3);
        shopConfig.set("items.birchlog.category", "Wood");

        shopConfig.set("items.strippedbirchlog.material", "STRIPPED_BIRCH_LOG");
        shopConfig.set("items.strippedbirchlog.price", 13);
        shopConfig.set("items.strippedbirchlog.sellPrice", 3.5);
        shopConfig.set("items.strippedbirchlog.amount", 1);
        shopConfig.set("items.strippedbirchlog.slot", 12);
        shopConfig.set("items.strippedbirchlog.page", 3);
        shopConfig.set("items.strippedbirchlog.category", "Wood");

        shopConfig.set("items.birchwood.material", "BIRCH_WOOD");
        shopConfig.set("items.birchwood.price", 12);
        shopConfig.set("items.birchwood.sellPrice", 3);
        shopConfig.set("items.birchwood.amount", 1);
        shopConfig.set("items.birchwood.slot", 14);
        shopConfig.set("items.birchwood.page", 3);
        shopConfig.set("items.birchwood.category", "Wood");

        shopConfig.set("items.strippedbirchwood.material", "STRIPPED_BIRCH_WOOD");
        shopConfig.set("items.strippedbirchwood.price", 13);
        shopConfig.set("items.strippedbirchwood.sellPrice", 3.5);
        shopConfig.set("items.strippedbirchwood.amount", 1);
        shopConfig.set("items.strippedbirchwood.slot", 16);
        shopConfig.set("items.strippedbirchwood.page", 3);
        shopConfig.set("items.strippedbirchwood.category", "Wood");

        shopConfig.set("items.birchplanks.material", "BIRCH_PLANKS");
        shopConfig.set("items.birchplanks.price", 4);
        shopConfig.set("items.birchplanks.sellPrice", 1);
        shopConfig.set("items.birchplanks.amount", 1);
        shopConfig.set("items.birchplanks.slot", 19);
        shopConfig.set("items.birchplanks.page", 3);
        shopConfig.set("items.birchplanks.category", "Wood");

        shopConfig.set("items.birchslab.material", "BIRCH_SLAB");
        shopConfig.set("items.birchslab.price", 2);
        shopConfig.set("items.birchslab.sellPrice", 0.5);
        shopConfig.set("items.birchslab.amount", 1);
        shopConfig.set("items.birchslab.slot", 21);
        shopConfig.set("items.birchslab.page", 3);
        shopConfig.set("items.birchslab.category", "Wood");

        shopConfig.set("items.birchstairs.material", "BIRCH_STAIRS");
        shopConfig.set("items.birchstairs.price", 5);
        shopConfig.set("items.birchstairs.sellPrice", 1.25);
        shopConfig.set("items.birchstairs.amount", 1);
        shopConfig.set("items.birchstairs.slot", 23);
        shopConfig.set("items.birchstairs.page", 3);
        shopConfig.set("items.birchstairs.category", "Wood");

        shopConfig.set("items.birchfence.material", "BIRCH_FENCE");
        shopConfig.set("items.birchfence.price", 4);
        shopConfig.set("items.birchfence.sellPrice", 1);
        shopConfig.set("items.birchfence.amount", 1);
        shopConfig.set("items.birchfence.slot", 25);
        shopConfig.set("items.birchfence.page", 3);
        shopConfig.set("items.birchfence.category", "Wood");

        shopConfig.set("items.birchfencegate.material", "BIRCH_FENCE_GATE");
        shopConfig.set("items.birchfencegate.price", 6);
        shopConfig.set("items.birchfencegate.sellPrice", 1.5);
        shopConfig.set("items.birchfencegate.amount", 1);
        shopConfig.set("items.birchfencegate.slot", 28);
        shopConfig.set("items.birchfencegate.page", 3);
        shopConfig.set("items.birchfencegate.category", "Wood");

        shopConfig.set("items.birchdoor.material", "BIRCH_DOOR");
        shopConfig.set("items.birchdoor.price", 5);
        shopConfig.set("items.birchdoor.sellPrice", 1.25);
        shopConfig.set("items.birchdoor.amount", 1);
        shopConfig.set("items.birchdoor.slot", 30);
        shopConfig.set("items.birchdoor.page", 3);
        shopConfig.set("items.birchdoor.category", "Wood");

        shopConfig.set("items.birchtrapdoor.material", "BIRCH_TRAPDOOR");
        shopConfig.set("items.birchtrapdoor.price", 5);
        shopConfig.set("items.birchtrapdoor.sellPrice", 1.25);
        shopConfig.set("items.birchtrapdoor.amount", 1);
        shopConfig.set("items.birchtrapdoor.slot", 32);
        shopConfig.set("items.birchtrapdoor.page", 3);
        shopConfig.set("items.birchtrapdoor.category", "Wood");

        shopConfig.set("items.birchbutton.material", "BIRCH_BUTTON");
        shopConfig.set("items.birchbutton.price", 1);
        shopConfig.set("items.birchbutton.sellPrice", 0.25);
        shopConfig.set("items.birchbutton.amount", 1);
        shopConfig.set("items.birchbutton.slot", 34);
        shopConfig.set("items.birchbutton.page", 3);
        shopConfig.set("items.birchbutton.category", "Wood");

        shopConfig.set("items.junglelog.material", "JUNGLE_LOG");
        shopConfig.set("items.junglelog.price", 12);
        shopConfig.set("items.junglelog.sellPrice", 3);
        shopConfig.set("items.junglelog.amount", 1);
        shopConfig.set("items.junglelog.slot", 10);
        shopConfig.set("items.junglelog.page", 4);
        shopConfig.set("items.junglelog.category", "Wood");

        shopConfig.set("items.strippedjunglelog.material", "STRIPPED_JUNGLE_LOG");
        shopConfig.set("items.strippedjunglelog.price", 13);
        shopConfig.set("items.strippedjunglelog.sellPrice", 3.5);
        shopConfig.set("items.strippedjunglelog.amount", 1);
        shopConfig.set("items.strippedjunglelog.slot", 12);
        shopConfig.set("items.strippedjunglelog.page", 4);
        shopConfig.set("items.strippedjunglelog.category", "Wood");

        shopConfig.set("items.junglewood.material", "JUNGLE_WOOD");
        shopConfig.set("items.junglewood.price", 12);
        shopConfig.set("items.junglewood.sellPrice", 3);
        shopConfig.set("items.junglewood.amount", 1);
        shopConfig.set("items.junglewood.slot", 14);
        shopConfig.set("items.junglewood.page", 4);
        shopConfig.set("items.junglewood.category", "Wood");

        shopConfig.set("items.strippedjunglewood.material", "STRIPPED_JUNGLE_WOOD");
        shopConfig.set("items.strippedjunglewood.price", 13);
        shopConfig.set("items.strippedjunglewood.sellPrice", 3.5);
        shopConfig.set("items.strippedjunglewood.amount", 1);
        shopConfig.set("items.strippedjunglewood.slot", 16);
        shopConfig.set("items.strippedjunglewood.page", 4);
        shopConfig.set("items.strippedjunglewood.category", "Wood");

        shopConfig.set("items.jungleplanks.material", "JUNGLE_PLANKS");
        shopConfig.set("items.jungleplanks.price", 4);
        shopConfig.set("items.jungleplanks.sellPrice", 1);
        shopConfig.set("items.jungleplanks.amount", 1);
        shopConfig.set("items.jungleplanks.slot", 19);
        shopConfig.set("items.jungleplanks.page", 4);
        shopConfig.set("items.jungleplanks.category", "Wood");

        shopConfig.set("items.jungleslab.material", "JUNGLE_SLAB");
        shopConfig.set("items.jungleslab.price", 2);
        shopConfig.set("items.jungleslab.sellPrice", 0.5);
        shopConfig.set("items.jungleslab.amount", 1);
        shopConfig.set("items.jungleslab.slot", 21);
        shopConfig.set("items.jungleslab.page", 4);
        shopConfig.set("items.jungleslab.category", "Wood");

        shopConfig.set("items.junglestairs.material", "JUNGLE_STAIRS");
        shopConfig.set("items.junglestairs.price", 5);
        shopConfig.set("items.junglestairs.sellPrice", 1.25);
        shopConfig.set("items.junglestairs.amount", 1);
        shopConfig.set("items.junglestairs.slot", 23);
        shopConfig.set("items.junglestairs.page", 4);
        shopConfig.set("items.junglestairs.category", "Wood");

        shopConfig.set("items.junglefence.material", "JUNGLE_FENCE");
        shopConfig.set("items.junglefence.price", 4);
        shopConfig.set("items.junglefence.sellPrice", 1);
        shopConfig.set("items.junglefence.amount", 1);
        shopConfig.set("items.junglefence.slot", 25);
        shopConfig.set("items.junglefence.page", 4);
        shopConfig.set("items.junglefence.category", "Wood");

        shopConfig.set("items.junglefencegate.material", "JUNGLE_FENCE_GATE");
        shopConfig.set("items.junglefencegate.price", 6);
        shopConfig.set("items.junglefencegate.sellPrice", 1.5);
        shopConfig.set("items.junglefencegate.amount", 1);
        shopConfig.set("items.junglefencegate.slot", 28);
        shopConfig.set("items.junglefencegate.page", 4);
        shopConfig.set("items.junglefencegate.category", "Wood");

        shopConfig.set("items.jungledoor.material", "JUNGLE_DOOR");
        shopConfig.set("items.jungledoor.price", 5);
        shopConfig.set("items.jungledoor.sellPrice", 1.25);
        shopConfig.set("items.jungledoor.amount", 1);
        shopConfig.set("items.jungledoor.slot", 30);
        shopConfig.set("items.jungledoor.page", 4);
        shopConfig.set("items.jungledoor.category", "Wood");

        shopConfig.set("items.jungletrapdoor.material", "JUNGLE_TRAPDOOR");
        shopConfig.set("items.jungletrapdoor.price", 5);
        shopConfig.set("items.jungletrapdoor.sellPrice", 1.25);
        shopConfig.set("items.jungletrapdoor.amount", 1);
        shopConfig.set("items.jungletrapdoor.slot", 32);
        shopConfig.set("items.jungletrapdoor.page", 4);
        shopConfig.set("items.jungletrapdoor.category", "Wood");

        shopConfig.set("items.junglebutton.material", "JUNGLE_BUTTON");
        shopConfig.set("items.junglebutton.price", 1);
        shopConfig.set("items.junglebutton.sellPrice", 0.25);
        shopConfig.set("items.junglebutton.amount", 1);
        shopConfig.set("items.junglebutton.slot", 34);
        shopConfig.set("items.junglebutton.page", 4);
        shopConfig.set("items.junglebutton.category", "Wood");

        shopConfig.set("items.acacialog.material", "ACACIA_LOG");
        shopConfig.set("items.acacialog.price", 12);
        shopConfig.set("items.acacialog.sellPrice", 3);
        shopConfig.set("items.acacialog.amount", 1);
        shopConfig.set("items.acacialog.slot", 10);
        shopConfig.set("items.acacialog.page", 5);
        shopConfig.set("items.acacialog.category", "Wood");

        shopConfig.set("items.strippedacacialog.material", "STRIPPED_ACACIA_LOG");
        shopConfig.set("items.strippedacacialog.price", 13);
        shopConfig.set("items.strippedacacialog.sellPrice", 3.5);
        shopConfig.set("items.strippedacacialog.amount", 1);
        shopConfig.set("items.strippedacacialog.slot", 12);
        shopConfig.set("items.strippedacacialog.page", 5);
        shopConfig.set("items.strippedacacialog.category", "Wood");

        shopConfig.set("items.acaciawood.material", "ACACIA_WOOD");
        shopConfig.set("items.acaciawood.price", 12);
        shopConfig.set("items.acaciawood.sellPrice", 3);
        shopConfig.set("items.acaciawood.amount", 1);
        shopConfig.set("items.acaciawood.slot", 14);
        shopConfig.set("items.acaciawood.page", 5);
        shopConfig.set("items.acaciawood.category", "Wood");

        shopConfig.set("items.strippedacaciawood.material", "STRIPPED_ACACIA_WOOD");
        shopConfig.set("items.strippedacaciawood.price", 13);
        shopConfig.set("items.strippedacaciawood.sellPrice", 3.5);
        shopConfig.set("items.strippedacaciawood.amount", 1);
        shopConfig.set("items.strippedacaciawood.slot", 16);
        shopConfig.set("items.strippedacaciawood.page", 5);
        shopConfig.set("items.strippedacaciawood.category", "Wood");

        shopConfig.set("items.acaciaplanks.material", "ACACIA_PLANKS");
        shopConfig.set("items.acaciaplanks.price", 4);
        shopConfig.set("items.acaciaplanks.sellPrice", 1);
        shopConfig.set("items.acaciaplanks.amount", 1);
        shopConfig.set("items.acaciaplanks.slot", 19);
        shopConfig.set("items.acaciaplanks.page", 5);
        shopConfig.set("items.acaciaplanks.category", "Wood");

        shopConfig.set("items.acaciaslab.material", "ACACIA_SLAB");
        shopConfig.set("items.acaciaslab.price", 2);
        shopConfig.set("items.acaciaslab.sellPrice", 0.5);
        shopConfig.set("items.acaciaslab.amount", 1);
        shopConfig.set("items.acaciaslab.slot", 21);
        shopConfig.set("items.acaciaslab.page", 5);
        shopConfig.set("items.acaciaslab.category", "Wood");

        shopConfig.set("items.acaciastairs.material", "ACACIA_STAIRS");
        shopConfig.set("items.acaciastairs.price", 5);
        shopConfig.set("items.acaciastairs.sellPrice", 1.25);
        shopConfig.set("items.acaciastairs.amount", 1);
        shopConfig.set("items.acaciastairs.slot", 23);
        shopConfig.set("items.acaciastairs.page", 5);
        shopConfig.set("items.acaciastairs.category", "Wood");

        shopConfig.set("items.acaciafence.material", "ACACIA_FENCE");
        shopConfig.set("items.acaciafence.price", 4);
        shopConfig.set("items.acaciafence.sellPrice", 1);
        shopConfig.set("items.acaciafence.amount", 1);
        shopConfig.set("items.acaciafence.slot", 25);
        shopConfig.set("items.acaciafence.page", 5);
        shopConfig.set("items.acaciafence.category", "Wood");

        shopConfig.set("items.acaciafencegate.material", "ACACIA_FENCE_GATE");
        shopConfig.set("items.acaciafencegate.price", 6);
        shopConfig.set("items.acaciafencegate.sellPrice", 1.5);
        shopConfig.set("items.acaciafencegate.amount", 1);
        shopConfig.set("items.acaciafencegate.slot", 28);
        shopConfig.set("items.acaciafencegate.page", 5);
        shopConfig.set("items.acaciafencegate.category", "Wood");

        shopConfig.set("items.acaciadoor.material", "ACACIA_DOOR");
        shopConfig.set("items.acaciadoor.price", 5);
        shopConfig.set("items.acaciadoor.sellPrice", 1.25);
        shopConfig.set("items.acaciadoor.amount", 1);
        shopConfig.set("items.acaciadoor.slot", 30);
        shopConfig.set("items.acaciadoor.page", 5);
        shopConfig.set("items.acaciadoor.category", "Wood");

        shopConfig.set("items.acaciatrapdoor.material", "ACACIA_TRAPDOOR");
        shopConfig.set("items.acaciatrapdoor.price", 5);
        shopConfig.set("items.acaciatrapdoor.sellPrice", 1.25);
        shopConfig.set("items.acaciatrapdoor.amount", 1);
        shopConfig.set("items.acaciatrapdoor.slot", 32);
        shopConfig.set("items.acaciatrapdoor.page", 5);
        shopConfig.set("items.acaciatrapdoor.category", "Wood");

        shopConfig.set("items.acaciabutton.material", "ACACIA_BUTTON");
        shopConfig.set("items.acaciabutton.price", 1);
        shopConfig.set("items.acaciabutton.sellPrice", 0.25);
        shopConfig.set("items.acaciabutton.amount", 1);
        shopConfig.set("items.acaciabutton.slot", 34);
        shopConfig.set("items.acaciabutton.page", 5);
        shopConfig.set("items.acaciabutton.category", "Wood");

        shopConfig.set("items.darkoaklog.material", "DARK_OAK_LOG");
        shopConfig.set("items.darkoaklog.price", 12);
        shopConfig.set("items.darkoaklog.sellPrice", 3);
        shopConfig.set("items.darkoaklog.amount", 1);
        shopConfig.set("items.darkoaklog.slot", 10);
        shopConfig.set("items.darkoaklog.page", 6);
        shopConfig.set("items.darkoaklog.category", "Wood");

        shopConfig.set("items.strippeddarkoaklog.material", "STRIPPED_DARK_OAK_LOG");
        shopConfig.set("items.strippeddarkoaklog.price", 13);
        shopConfig.set("items.strippeddarkoaklog.sellPrice", 3.5);
        shopConfig.set("items.strippeddarkoaklog.amount", 1);
        shopConfig.set("items.strippeddarkoaklog.slot", 12);
        shopConfig.set("items.strippeddarkoaklog.page", 6);
        shopConfig.set("items.strippeddarkoaklog.category", "Wood");

        shopConfig.set("items.darkoakwood.material", "DARK_OAK_WOOD");
        shopConfig.set("items.darkoakwood.price", 12);
        shopConfig.set("items.darkoakwood.sellPrice", 3);
        shopConfig.set("items.darkoakwood.amount", 1);
        shopConfig.set("items.darkoakwood.slot", 14);
        shopConfig.set("items.darkoakwood.page", 6);
        shopConfig.set("items.darkoakwood.category", "Wood");

        shopConfig.set("items.strippeddarkoakwood.material", "STRIPPED_DARK_OAK_WOOD");
        shopConfig.set("items.strippeddarkoakwood.price", 13);
        shopConfig.set("items.strippeddarkoakwood.sellPrice", 3.5);
        shopConfig.set("items.strippeddarkoakwood.amount", 1);
        shopConfig.set("items.strippeddarkoakwood.slot", 16);
        shopConfig.set("items.strippeddarkoakwood.page", 6);
        shopConfig.set("items.strippeddarkoakwood.category", "Wood");

        shopConfig.set("items.darkoakplanks.material", "DARK_OAK_PLANKS");
        shopConfig.set("items.darkoakplanks.price", 4);
        shopConfig.set("items.darkoakplanks.sellPrice", 1);
        shopConfig.set("items.darkoakplanks.amount", 1);
        shopConfig.set("items.darkoakplanks.slot", 19);
        shopConfig.set("items.darkoakplanks.page", 6);
        shopConfig.set("items.darkoakplanks.category", "Wood");

        shopConfig.set("items.darkoakslab.material", "DARK_OAK_SLAB");
        shopConfig.set("items.darkoakslab.price", 2);
        shopConfig.set("items.darkoakslab.sellPrice", 0.5);
        shopConfig.set("items.darkoakslab.amount", 1);
        shopConfig.set("items.darkoakslab.slot", 21);
        shopConfig.set("items.darkoakslab.page", 6);
        shopConfig.set("items.darkoakslab.category", "Wood");

        shopConfig.set("items.darkoakstairs.material", "DARK_OAK_STAIRS");
        shopConfig.set("items.darkoakstairs.price", 5);
        shopConfig.set("items.darkoakstairs.sellPrice", 1.25);
        shopConfig.set("items.darkoakstairs.amount", 1);
        shopConfig.set("items.darkoakstairs.slot", 23);
        shopConfig.set("items.darkoakstairs.page", 6);
        shopConfig.set("items.darkoakstairs.category", "Wood");

        shopConfig.set("items.darkoakfence.material", "DARK_OAK_FENCE");
        shopConfig.set("items.darkoakfence.price", 4);
        shopConfig.set("items.darkoakfence.sellPrice", 1);
        shopConfig.set("items.darkoakfence.amount", 1);
        shopConfig.set("items.darkoakfence.slot", 25);
        shopConfig.set("items.darkoakfence.page", 6);
        shopConfig.set("items.darkoakfence.category", "Wood");

        shopConfig.set("items.darkoakfencegate.material", "DARK_OAK_FENCE_GATE");
        shopConfig.set("items.darkoakfencegate.price", 6);
        shopConfig.set("items.darkoakfencegate.sellPrice", 1.5);
        shopConfig.set("items.darkoakfencegate.amount", 1);
        shopConfig.set("items.darkoakfencegate.slot", 28);
        shopConfig.set("items.darkoakfencegate.page", 6);
        shopConfig.set("items.darkoakfencegate.category", "Wood");

        shopConfig.set("items.darkoakdoor.material", "DARK_OAK_DOOR");
        shopConfig.set("items.darkoakdoor.price", 5);
        shopConfig.set("items.darkoakdoor.sellPrice", 1.25);
        shopConfig.set("items.darkoakdoor.amount", 1);
        shopConfig.set("items.darkoakdoor.slot", 30);
        shopConfig.set("items.darkoakdoor.page", 6);
        shopConfig.set("items.darkoakdoor.category", "Wood");

        shopConfig.set("items.darkoaktrapdoor.material", "DARK_OAK_TRAPDOOR");
        shopConfig.set("items.darkoaktrapdoor.price", 5);
        shopConfig.set("items.darkoaktrapdoor.sellPrice", 1.25);
        shopConfig.set("items.darkoaktrapdoor.amount", 1);
        shopConfig.set("items.darkoaktrapdoor.slot", 32);
        shopConfig.set("items.darkoaktrapdoor.page", 6);
        shopConfig.set("items.darkoaktrapdoor.category", "Wood");

        shopConfig.set("items.darkoakbutton.material", "DARK_OAK_BUTTON");
        shopConfig.set("items.darkoakbutton.price", 1);
        shopConfig.set("items.darkoakbutton.sellPrice", 0.25);
        shopConfig.set("items.darkoakbutton.amount", 1);
        shopConfig.set("items.darkoakbutton.slot", 34);
        shopConfig.set("items.darkoakbutton.page", 6);
        shopConfig.set("items.darkoakbutton.category", "Wood");

        shopConfig.set("items.mangrovelog.material", "MANGROVE_LOG");
        shopConfig.set("items.mangrovelog.price", 14);
        shopConfig.set("items.mangrovelog.sellPrice", 3.5);
        shopConfig.set("items.mangrovelog.amount", 1);
        shopConfig.set("items.mangrovelog.slot", 10);
        shopConfig.set("items.mangrovelog.page", 7);
        shopConfig.set("items.mangrovelog.category", "Wood");

        shopConfig.set("items.strippedmangrovelog.material", "STRIPPED_MANGROVE_LOG");
        shopConfig.set("items.strippedmangrovelog.price", 15);
        shopConfig.set("items.strippedmangrovelog.sellPrice", 4);
        shopConfig.set("items.strippedmangrovelog.amount", 1);
        shopConfig.set("items.strippedmangrovelog.slot", 12);
        shopConfig.set("items.strippedmangrovelog.page", 7);
        shopConfig.set("items.strippedmangrovelog.category", "Wood");

        shopConfig.set("items.mangrovewood.material", "MANGROVE_WOOD");
        shopConfig.set("items.mangrovewood.price", 14);
        shopConfig.set("items.mangrovewood.sellPrice", 3.5);
        shopConfig.set("items.mangrovewood.amount", 1);
        shopConfig.set("items.mangrovewood.slot", 14);
        shopConfig.set("items.mangrovewood.page", 7);
        shopConfig.set("items.mangrovewood.category", "Wood");

        shopConfig.set("items.strippedmangrovewood.material", "STRIPPED_MANGROVE_WOOD");
        shopConfig.set("items.strippedmangrovewood.price", 15);
        shopConfig.set("items.strippedmangrovewood.sellPrice", 4);
        shopConfig.set("items.strippedmangrovewood.amount", 1);
        shopConfig.set("items.strippedmangrovewood.slot", 16);
        shopConfig.set("items.strippedmangrovewood.page", 7);
        shopConfig.set("items.strippedmangrovewood.category", "Wood");

        shopConfig.set("items.mangroveplanks.material", "MANGROVE_PLANKS");
        shopConfig.set("items.mangroveplanks.price", 5);
        shopConfig.set("items.mangroveplanks.sellPrice", 1.5);
        shopConfig.set("items.mangroveplanks.amount", 1);
        shopConfig.set("items.mangroveplanks.slot", 19);
        shopConfig.set("items.mangroveplanks.page", 7);
        shopConfig.set("items.mangroveplanks.category", "Wood");

        shopConfig.set("items.mangroveslab.material", "MANGROVE_SLAB");
        shopConfig.set("items.mangroveslab.price", 3);
        shopConfig.set("items.mangroveslab.sellPrice", 0.75);
        shopConfig.set("items.mangroveslab.amount", 1);
        shopConfig.set("items.mangroveslab.slot", 21);
        shopConfig.set("items.mangroveslab.page", 7);
        shopConfig.set("items.mangroveslab.category", "Wood");

        shopConfig.set("items.mangrovestairs.material", "MANGROVE_STAIRS");
        shopConfig.set("items.mangrovestairs.price", 6);
        shopConfig.set("items.mangrovestairs.sellPrice", 1.5);
        shopConfig.set("items.mangrovestairs.amount", 1);
        shopConfig.set("items.mangrovestairs.slot", 23);
        shopConfig.set("items.mangrovestairs.page", 7);
        shopConfig.set("items.mangrovestairs.category", "Wood");

        shopConfig.set("items.mangrovefence.material", "MANGROVE_FENCE");
        shopConfig.set("items.mangrovefence.price", 5);
        shopConfig.set("items.mangrovefence.sellPrice", 1.25);
        shopConfig.set("items.mangrovefence.amount", 1);
        shopConfig.set("items.mangrovefence.slot", 25);
        shopConfig.set("items.mangrovefence.page", 7);
        shopConfig.set("items.mangrovefence.category", "Wood");

        shopConfig.set("items.mangrovefencegate.material", "MANGROVE_FENCE_GATE");
        shopConfig.set("items.mangrovefencegate.price", 7);
        shopConfig.set("items.mangrovefencegate.sellPrice", 1.75);
        shopConfig.set("items.mangrovefencegate.amount", 1);
        shopConfig.set("items.mangrovefencegate.slot", 28);
        shopConfig.set("items.mangrovefencegate.page", 7);
        shopConfig.set("items.mangrovefencegate.category", "Wood");

        shopConfig.set("items.mangrovedoor.material", "MANGROVE_DOOR");
        shopConfig.set("items.mangrovedoor.price", 6);
        shopConfig.set("items.mangrovedoor.sellPrice", 1.5);
        shopConfig.set("items.mangrovedoor.amount", 1);
        shopConfig.set("items.mangrovedoor.slot", 30);
        shopConfig.set("items.mangrovedoor.page", 7);
        shopConfig.set("items.mangrovedoor.category", "Wood");

        shopConfig.set("items.mangrovetrapdoor.material", "MANGROVE_TRAPDOOR");
        shopConfig.set("items.mangrovetrapdoor.price", 6);
        shopConfig.set("items.mangrovetrapdoor.sellPrice", 1.5);
        shopConfig.set("items.mangrovetrapdoor.amount", 1);
        shopConfig.set("items.mangrovetrapdoor.slot", 32);
        shopConfig.set("items.mangrovetrapdoor.page", 7);
        shopConfig.set("items.mangrovetrapdoor.category", "Wood");

        shopConfig.set("items.mangrovebutton.material", "MANGROVE_BUTTON");
        shopConfig.set("items.mangrovebutton.price", 2);
        shopConfig.set("items.mangrovebutton.sellPrice", 0.5);
        shopConfig.set("items.mangrovebutton.amount", 1);
        shopConfig.set("items.mangrovebutton.slot", 34);
        shopConfig.set("items.mangrovebutton.page", 7);
        shopConfig.set("items.mangrovebutton.category", "Wood");



        shopConfig.set("items.bamboo.log.material", "BAMBOO_BLOCK");
        shopConfig.set("items.bamboo.log.price", 8);
        shopConfig.set("items.bamboo.log.sellPrice", 2);
        shopConfig.set("items.bamboo.log.amount", 1);
        shopConfig.set("items.bamboo.log.slot", 10);
        shopConfig.set("items.bamboo.log.page", 9);
        shopConfig.set("items.bamboo.log.category", "Wood");


        shopConfig.set("items.bamboo.wood.material", "BAMBOO_PLANKS");
        shopConfig.set("items.bamboo.wood.price", 8);
        shopConfig.set("items.bamboo.wood.sellPrice", 2);
        shopConfig.set("items.bamboo.wood.amount", 1);
        shopConfig.set("items.bamboo.wood.slot", 14);
        shopConfig.set("items.bamboo.wood.page", 9);
        shopConfig.set("items.bamboo.wood.category", "Wood");



        shopConfig.set("items.bamboo.planks.material", "BAMBOO_MOSAIC");
        shopConfig.set("items.bamboo.planks.price", 4);
        shopConfig.set("items.bamboo.planks.sellPrice", 1);
        shopConfig.set("items.bamboo.planks.amount", 1);
        shopConfig.set("items.bamboo.planks.slot", 19);
        shopConfig.set("items.bamboo.planks.page", 9);
        shopConfig.set("items.bamboo.planks.category", "Wood");

        shopConfig.set("items.bamboo.slab.material", "BAMBOO_SLAB");
        shopConfig.set("items.bamboo.slab.price", 2);
        shopConfig.set("items.bamboo.slab.sellPrice", 0.5);
        shopConfig.set("items.bamboo.slab.amount", 1);
        shopConfig.set("items.bamboo.slab.slot", 21);
        shopConfig.set("items.bamboo.slab.page", 9);
        shopConfig.set("items.bamboo.slab.category", "Wood");

        shopConfig.set("items.bamboo.stairs.material", "BAMBOO_STAIRS");
        shopConfig.set("items.bamboo.stairs.price", 5);
        shopConfig.set("items.bamboo.stairs.sellPrice", 1.25);
        shopConfig.set("items.bamboo.stairs.amount", 1);
        shopConfig.set("items.bamboo.stairs.slot", 23);
        shopConfig.set("items.bamboo.stairs.page", 9);
        shopConfig.set("items.bamboo.stairs.category", "Wood");

        shopConfig.set("items.bamboo.fence.material", "BAMBOO_FENCE");
        shopConfig.set("items.bamboo.fence.price", 4);
        shopConfig.set("items.bamboo.fence.sellPrice", 1);
        shopConfig.set("items.bamboo.fence.amount", 1);
        shopConfig.set("items.bamboo.fence.slot", 25);
        shopConfig.set("items.bamboo.fence.page", 9);
        shopConfig.set("items.bamboo.fence.category", "Wood");

        shopConfig.set("items.bamboo.fencegate.material", "BAMBOO_FENCE_GATE");
        shopConfig.set("items.bamboo.fencegate.price", 6);
        shopConfig.set("items.bamboo.fencegate.sellPrice", 1.5);
        shopConfig.set("items.bamboo.fencegate.amount", 1);
        shopConfig.set("items.bamboo.fencegate.slot", 28);
        shopConfig.set("items.bamboo.fencegate.page", 9);
        shopConfig.set("items.bamboo.fencegate.category", "Wood");

        shopConfig.set("items.bamboo.door.material", "BAMBOO_DOOR");
        shopConfig.set("items.bamboo.door.price", 5);
        shopConfig.set("items.bamboo.door.sellPrice", 1.25);
        shopConfig.set("items.bamboo.door.amount", 1);
        shopConfig.set("items.bamboo.door.slot", 30);
        shopConfig.set("items.bamboo.door.page", 9);
        shopConfig.set("items.bamboo.door.category", "Wood");

        shopConfig.set("items.bamboo.trapdoor.material", "BAMBOO_TRAPDOOR");
        shopConfig.set("items.bamboo.trapdoor.price", 5);
        shopConfig.set("items.bamboo.trapdoor.sellPrice", 1.25);
        shopConfig.set("items.bamboo.trapdoor.amount", 1);
        shopConfig.set("items.bamboo.trapdoor.slot", 32);
        shopConfig.set("items.bamboo.trapdoor.page", 9);
        shopConfig.set("items.bamboo.trapdoor.category", "Wood");

        shopConfig.set("items.bamboo.button.material", "BAMBOO_BUTTON");
        shopConfig.set("items.bamboo.button.price", 2);
        shopConfig.set("items.bamboo.button.sellPrice", 0.5);
        shopConfig.set("items.bamboo.button.amount", 1);
        shopConfig.set("items.bamboo.button.slot", 34);
        shopConfig.set("items.bamboo.button.page", 9);
        shopConfig.set("items.bamboo.button.category", "Wood");



        shopConfig.set("items.blaze_powder.material", "BLAZE_POWDER");
        shopConfig.set("items.blaze_powder.price", 5);
        shopConfig.set("items.blaze_powder.sellPrice", 1.25);
        shopConfig.set("items.blaze_powder.amount", 1);
        shopConfig.set("items.blaze_powder.slot", 10);
        shopConfig.set("items.blaze_powder.page", 1);
        shopConfig.set("items.blaze_powder.category", "Brewing");

        shopConfig.set("items.ghast_tear.material", "GHAST_TEAR");
        shopConfig.set("items.ghast_tear.price", 7);
        shopConfig.set("items.ghast_tear.sellPrice", 1.75);
        shopConfig.set("items.ghast_tear.amount", 1);
        shopConfig.set("items.ghast_tear.slot", 12);
        shopConfig.set("items.ghast_tear.page", 1);
        shopConfig.set("items.ghast_tear.category", "Brewing");

        shopConfig.set("items.magma_cream.material", "MAGMA_CREAM");
        shopConfig.set("items.magma_cream.price", 6);
        shopConfig.set("items.magma_cream.sellPrice", 1.5);
        shopConfig.set("items.magma_cream.amount", 1);
        shopConfig.set("items.magma_cream.slot", 14);
        shopConfig.set("items.magma_cream.page", 1);
        shopConfig.set("items.magma_cream.category", "Brewing");

        shopConfig.set("items.spider_eye.material", "SPIDER_EYE");
        shopConfig.set("items.spider_eye.price", 3);
        shopConfig.set("items.spider_eye.sellPrice", 0.75);
        shopConfig.set("items.spider_eye.amount", 1);
        shopConfig.set("items.spider_eye.slot", 16);
        shopConfig.set("items.spider_eye.page", 1);
        shopConfig.set("items.spider_eye.category", "Brewing");

        shopConfig.set("items.potion.material", "POTION");
        shopConfig.set("items.potion.price", 20);
        shopConfig.set("items.potion.sellPrice", 5);
        shopConfig.set("items.potion.amount", 1);
        shopConfig.set("items.potion.slot", 19);
        shopConfig.set("items.potion.page", 1);
        shopConfig.set("items.potion.category", "Brewing");



        shopConfig.set("items.fermented_spider_eye.material", "FERMENTED_SPIDER_EYE");
        shopConfig.set("items.fermented_spider_eye.price", 4);
        shopConfig.set("items.fermented_spider_eye.sellPrice", 1);
        shopConfig.set("items.fermented_spider_eye.amount", 1);
        shopConfig.set("items.fermented_spider_eye.slot", 23);
        shopConfig.set("items.fermented_spider_eye.page", 1);
        shopConfig.set("items.fermented_spider_eye.category", "Brewing");

        shopConfig.set("items.splash_potion.material", "SPLASH_POTION");
        shopConfig.set("items.splash_potion.price", 25);
        shopConfig.set("items.splash_potion.sellPrice", 6.25);
        shopConfig.set("items.splash_potion.amount", 1);
        shopConfig.set("items.splash_potion.slot", 25);
        shopConfig.set("items.splash_potion.page", 1);
        shopConfig.set("items.splash_potion.category", "Brewing");

        shopConfig.set("items.lingering_potion.material", "LINGERING_POTION");
        shopConfig.set("items.lingering_potion.price", 30);
        shopConfig.set("items.lingering_potion.sellPrice", 7.5);
        shopConfig.set("items.lingering_potion.amount", 1);
        shopConfig.set("items.lingering_potion.slot", 28);
        shopConfig.set("items.lingering_potion.page", 1);
        shopConfig.set("items.lingering_potion.category", "Brewing");

        shopConfig.set("items.glass_bottle.material", "GLASS_BOTTLE");
        shopConfig.set("items.glass_bottle.price", 1);
        shopConfig.set("items.glass_bottle.sellPrice", 0.25);
        shopConfig.set("items.glass_bottle.amount", 1);
        shopConfig.set("items.glass_bottle.slot", 30);
        shopConfig.set("items.glass_bottle.page", 1);
        shopConfig.set("items.glass_bottle.category", "Brewing");

        shopConfig.set("items.blaze_rod.material", "BLAZE_ROD");
        shopConfig.set("items.blaze_rod.price", 8);
        shopConfig.set("items.blaze_rod.sellPrice", 2);
        shopConfig.set("items.blaze_rod.amount", 1);
        shopConfig.set("items.blaze_rod.slot", 32);
        shopConfig.set("items.blaze_rod.page", 1);
        shopConfig.set("items.blaze_rod.category", "Brewing");

        shopConfig.set("items.rabbit_foot.material", "RABBIT_FOOT");
        shopConfig.set("items.rabbit_foot.price", 10);
        shopConfig.set("items.rabbit_foot.sellPrice", 2.5);
        shopConfig.set("items.rabbit_foot.amount", 1);
        shopConfig.set("items.rabbit_foot.slot", 34);
        shopConfig.set("items.rabbit_foot.page", 1);
        shopConfig.set("items.rabbit_foot.category", "Brewing");


        shopConfig.set("items.sugar.material", "SUGAR");
        shopConfig.set("items.sugar.price", 2);
        shopConfig.set("items.sugar.sellPrice", 0.5);
        shopConfig.set("items.sugar.amount", 1);
        shopConfig.set("items.sugar.slot", 10);
        shopConfig.set("items.sugar.page", 2);
        shopConfig.set("items.sugar.category", "Brewing");

        shopConfig.set("items.golden_carrot.material", "GOLDEN_CARROT");
        shopConfig.set("items.golden_carrot.price", 6);
        shopConfig.set("items.golden_carrot.sellPrice", 1.5);
        shopConfig.set("items.golden_carrot.amount", 1);
        shopConfig.set("items.golden_carrot.slot", 12);
        shopConfig.set("items.golden_carrot.page", 2);
        shopConfig.set("items.golden_carrot.category", "Brewing");

        shopConfig.set("items.pufferfish.material", "PUFFERFISH");
        shopConfig.set("items.pufferfish.price", 8);
        shopConfig.set("items.pufferfish.sellPrice", 2);
        shopConfig.set("items.pufferfish.amount", 1);
        shopConfig.set("items.pufferfish.slot", 14);
        shopConfig.set("items.pufferfish.page", 2);
        shopConfig.set("items.pufferfish.category", "Brewing");


        shopConfig.set("items.torch.material", "TORCH");
        shopConfig.set("items.torch.price", 8);
        shopConfig.set("items.torch.sellPrice", 2);
        shopConfig.set("items.torch.amount", 1);
        shopConfig.set("items.torch.slot", 10);
        shopConfig.set("items.torch.page", 1);
        shopConfig.set("items.torch.category", "Decorations");

        shopConfig.set("items.lantern.material", "LANTERN");
        shopConfig.set("items.lantern.price", 14);
        shopConfig.set("items.lantern.sellPrice", 3);
        shopConfig.set("items.lantern.amount", 1);
        shopConfig.set("items.lantern.slot", 12);
        shopConfig.set("items.lantern.page", 1);
        shopConfig.set("items.lantern.category", "Decorations");

        shopConfig.set("items.chain.material", "CHAIN");
        shopConfig.set("items.chain.price", 10);
        shopConfig.set("items.chain.sellPrice", 2);
        shopConfig.set("items.chain.amount", 1);
        shopConfig.set("items.chain.slot", 14);
        shopConfig.set("items.chain.page", 1);
        shopConfig.set("items.chain.category", "Decorations");

        shopConfig.set("items.redstonelamp1.material", "REDSTONE_LAMP");
        shopConfig.set("items.redstonelamp1.price", 20);
        shopConfig.set("items.redstonelamp1.sellPrice", 4);
        shopConfig.set("items.redstonelamp1.amount", 1);
        shopConfig.set("items.redstonelamp1.slot", 16);
        shopConfig.set("items.redstonelamp1.page", 1);
        shopConfig.set("items.redstonelamp1.category", "Decorations");

        shopConfig.set("items.craftingtable.material", "CRAFTING_TABLE");
        shopConfig.set("items.craftingtable.price", 12);
        shopConfig.set("items.craftingtable.sellPrice", 3);
        shopConfig.set("items.craftingtable.amount", 1);
        shopConfig.set("items.craftingtable.slot", 19);
        shopConfig.set("items.craftingtable.page", 1);
        shopConfig.set("items.craftingtable.category", "Decorations");

        shopConfig.set("items.stonecutter.material", "STONECUTTER");
        shopConfig.set("items.stonecutter.price", 30);
        shopConfig.set("items.stonecutter.sellPrice", 6);
        shopConfig.set("items.stonecutter.amount", 1);
        shopConfig.set("items.stonecutter.slot", 21);
        shopConfig.set("items.stonecutter.page", 1);
        shopConfig.set("items.stonecutter.category", "Decorations");

        shopConfig.set("items.cartographytable.material", "CARTOGRAPHY_TABLE");
        shopConfig.set("items.cartographytable.price", 40);
        shopConfig.set("items.cartographytable.sellPrice", 10);
        shopConfig.set("items.cartographytable.amount", 1);
        shopConfig.set("items.cartographytable.slot", 23);
        shopConfig.set("items.cartographytable.page", 1);
        shopConfig.set("items.cartographytable.category", "Decorations");

        shopConfig.set("items.fletchingtable.material", "FLETCHING_TABLE");
        shopConfig.set("items.fletchingtable.price", 35);
        shopConfig.set("items.fletchingtable.sellPrice", 8);
        shopConfig.set("items.fletchingtable.amount", 1);
        shopConfig.set("items.fletchingtable.slot", 25);
        shopConfig.set("items.fletchingtable.page", 1);
        shopConfig.set("items.fletchingtable.category", "Decorations");

        shopConfig.set("items.smithingtable.material", "SMITHING_TABLE");
        shopConfig.set("items.smithingtable.price", 50);
        shopConfig.set("items.smithingtable.sellPrice", 12);
        shopConfig.set("items.smithingtable.amount", 1);
        shopConfig.set("items.smithingtable.slot", 28);
        shopConfig.set("items.smithingtable.page", 1);
        shopConfig.set("items.smithingtable.category", "Decorations");

        shopConfig.set("items.grindstone.material", "GRINDSTONE");
        shopConfig.set("items.grindstone.price", 60);
        shopConfig.set("items.grindstone.sellPrice", 15);
        shopConfig.set("items.grindstone.amount", 1);
        shopConfig.set("items.grindstone.slot", 30);
        shopConfig.set("items.grindstone.page", 1);
        shopConfig.set("items.grindstone.category", "Decorations");

        shopConfig.set("items.loom.material", "LOOM");
        shopConfig.set("items.loom.price", 60);
        shopConfig.set("items.loom.sellPrice", 15);
        shopConfig.set("items.loom.amount", 1);
        shopConfig.set("items.loom.slot", 32);
        shopConfig.set("items.loom.page", 1);
        shopConfig.set("items.loom.category", "Decorations");

        shopConfig.set("items.furnace.material", "FURNACE");
        shopConfig.set("items.furnace.price", 50);
        shopConfig.set("items.furnace.sellPrice", 12);
        shopConfig.set("items.furnace.amount", 1);
        shopConfig.set("items.furnace.slot", 34);
        shopConfig.set("items.furnace.page", 1);
        shopConfig.set("items.furnace.category", "Decorations");

        shopConfig.set("items.smoker.material", "SMOKER");
        shopConfig.set("items.smoker.price", 55);
        shopConfig.set("items.smoker.sellPrice", 14);
        shopConfig.set("items.smoker.amount", 1);
        shopConfig.set("items.smoker.slot", 10);
        shopConfig.set("items.smoker.page", 2);
        shopConfig.set("items.smoker.category", "Decorations");



        shopConfig.set("items.blastfurnace.material", "BLAST_FURNACE");
        shopConfig.set("items.blastfurnace.price", 65);
        shopConfig.set("items.blastfurnace.sellPrice", 16);
        shopConfig.set("items.blastfurnace.amount", 1);
        shopConfig.set("items.blastfurnace.slot", 14);
        shopConfig.set("items.blastfurnace.page", 2);
        shopConfig.set("items.blastfurnace.category", "Decorations");

        shopConfig.set("items.campfire.material", "CAMPFIRE");
        shopConfig.set("items.campfire.price", 40);
        shopConfig.set("items.campfire.sellPrice", 10);
        shopConfig.set("items.campfire.amount", 1);
        shopConfig.set("items.campfire.slot", 16);
        shopConfig.set("items.campfire.page", 2);
        shopConfig.set("items.campfire.category", "Decorations");

        shopConfig.set("items.noteblock1.material", "NOTE_BLOCK");
        shopConfig.set("items.noteblock1.price", 50);
        shopConfig.set("items.noteblock1.sellPrice", 12);
        shopConfig.set("items.noteblock1.amount", 1);
        shopConfig.set("items.noteblock1.slot", 19);
        shopConfig.set("items.noteblock1.page", 2);
        shopConfig.set("items.noteblock1.category", "Decorations");

        shopConfig.set("items.jukebox1.material", "JUKEBOX");
        shopConfig.set("items.jukebox1.price", 80);
        shopConfig.set("items.jukebox1.sellPrice", 20);
        shopConfig.set("items.jukebox1.amount", 1);
        shopConfig.set("items.jukebox1.slot", 21);
        shopConfig.set("items.jukebox1.page", 2);
        shopConfig.set("items.jukebox1.category", "Decorations");

        shopConfig.set("items.enchantingtable.material", "ENCHANTING_TABLE");
        shopConfig.set("items.enchantingtable.price", 100);
        shopConfig.set("items.enchantingtable.sellPrice", 25);
        shopConfig.set("items.enchantingtable.amount", 1);
        shopConfig.set("items.enchantingtable.slot", 23);
        shopConfig.set("items.enchantingtable.page", 2);
        shopConfig.set("items.enchantingtable.category", "Decorations");

        shopConfig.set("items.cauldron.material", "CAULDRON");
        shopConfig.set("items.cauldron.price", 65);
        shopConfig.set("items.cauldron.sellPrice", 12);
        shopConfig.set("items.cauldron.amount", 1);
        shopConfig.set("items.cauldron.slot", 25);
        shopConfig.set("items.cauldron.page", 2);
        shopConfig.set("items.cauldron.category", "Decorations");

        shopConfig.set("items.lodestone.material", "LODESTONE");
        shopConfig.set("items.lodestone.price", 200);
        shopConfig.set("items.lodestone.sellPrice", 50);
        shopConfig.set("items.lodestone.amount", 1);
        shopConfig.set("items.lodestone.slot", 28);
        shopConfig.set("items.lodestone.page", 2);
        shopConfig.set("items.lodestone.category", "Decorations");

        shopConfig.set("items.ladder.material", "LADDER");
        shopConfig.set("items.ladder.price", 20);
        shopConfig.set("items.ladder.sellPrice", 5);
        shopConfig.set("items.ladder.amount", 1);
        shopConfig.set("items.ladder.slot", 30);
        shopConfig.set("items.ladder.page", 2);
        shopConfig.set("items.ladder.category", "Decorations");

        shopConfig.set("items.flowerpot.material", "FLOWER_POT");
        shopConfig.set("items.flowerpot.price", 10);
        shopConfig.set("items.flowerpot.sellPrice", 2);
        shopConfig.set("items.flowerpot.amount", 1);
        shopConfig.set("items.flowerpot.slot", 32);
        shopConfig.set("items.flowerpot.page", 2);
        shopConfig.set("items.flowerpot.category", "Decorations");

        shopConfig.set("items.12121212122.material", "PLAYER_HEAD");
        shopConfig.set("items.12121212122.price", 1000);
        shopConfig.set("items.12121212122.sellPrice", 200);
        shopConfig.set("items.12121212122.amount", 1);
        shopConfig.set("items.12121212122.slot", 34);
        shopConfig.set("items.12121212122.page", 2);
        shopConfig.set("items.12121212122.category", "Decorations");

        shopConfig.set("items.decoratedpot.material", "DECORATED_POT");
        shopConfig.set("items.decoratedpot.price", 25);
        shopConfig.set("items.decoratedpot.sellPrice", 5);
        shopConfig.set("items.decoratedpot.amount", 1);
        shopConfig.set("items.decoratedpot.slot", 10);
        shopConfig.set("items.decoratedpot.page", 3);
        shopConfig.set("items.decoratedpot.category", "Decorations");

        shopConfig.set("items.trappedchest.material", "TRAPPED_CHEST");
        shopConfig.set("items.trappedchest.price", 100);
        shopConfig.set("items.trappedchest.sellPrice", 25);
        shopConfig.set("items.trappedchest.amount", 1);
        shopConfig.set("items.trappedchest.slot", 14);
        shopConfig.set("items.trappedchest.page", 3);
        shopConfig.set("items.trappedchest.category", "Decorations");

        shopConfig.set("items.itemframe.material", "ITEM_FRAME");
        shopConfig.set("items.itemframe.price", 50);
        shopConfig.set("items.itemframe.sellPrice", 12);
        shopConfig.set("items.itemframe.amount", 1);
        shopConfig.set("items.itemframe.slot", 16);
        shopConfig.set("items.itemframe.page", 3);
        shopConfig.set("items.itemframe.category", "Decorations");

        shopConfig.set("items.glowitemframe.material", "GLOW_ITEM_FRAME");
        shopConfig.set("items.glowitemframe.price", 60);
        shopConfig.set("items.glowitemframe.sellPrice", 15);
        shopConfig.set("items.glowitemframe.amount", 1);
        shopConfig.set("items.glowitemframe.slot", 19);
        shopConfig.set("items.glowitemframe.page", 3);
        shopConfig.set("items.glowitemframe.category", "Decorations");

        shopConfig.set("items.bookshelf.material", "BOOKSHELF");
        shopConfig.set("items.bookshelf.price", 120);
        shopConfig.set("items.bookshelf.sellPrice", 30);
        shopConfig.set("items.bookshelf.amount", 1);
        shopConfig.set("items.bookshelf.slot", 21);
        shopConfig.set("items.bookshelf.page", 3);
        shopConfig.set("items.bookshelf.category", "Decorations");

        shopConfig.set("items.lectern.material", "LECTERN");
        shopConfig.set("items.lectern.price", 110);
        shopConfig.set("items.lectern.sellPrice", 25);
        shopConfig.set("items.lectern.amount", 1);
        shopConfig.set("items.lectern.slot", 23);
        shopConfig.set("items.lectern.page", 3);
        shopConfig.set("items.lectern.category", "Decorations");

        shopConfig.set("items.redbed.material", "RED_BED");
        shopConfig.set("items.redbed.price", 50);
        shopConfig.set("items.redbed.sellPrice", 10);
        shopConfig.set("items.redbed.amount", 1);
        shopConfig.set("items.redbed.slot", 25);
        shopConfig.set("items.redbed.page", 3);
        shopConfig.set("items.redbed.category", "Decorations");

        shopConfig.set("items.candle.material", "CANDLE");
        shopConfig.set("items.candle.price", 12);
        shopConfig.set("items.candle.sellPrice", 3);
        shopConfig.set("items.candle.amount", 1);
        shopConfig.set("items.candle.slot", 28);
        shopConfig.set("items.candle.page", 3);
        shopConfig.set("items.candle.category", "Decorations");

        shopConfig.set("items.glass.material", "GLASS");
        shopConfig.set("items.glass.price", 20);
        shopConfig.set("items.glass.sellPrice", 5);
        shopConfig.set("items.glass.amount", 1);
        shopConfig.set("items.glass.slot", 30);
        shopConfig.set("items.glass.page", 3);
        shopConfig.set("items.glass.category", "Decorations");

        shopConfig.set("items.chest1.material", "CHEST");
        shopConfig.set("items.chest1.price", 45);
        shopConfig.set("items.chest1.sellPrice", 14);
        shopConfig.set("items.chest1.amount", 1);
        shopConfig.set("items.chest1.slot", 32);
        shopConfig.set("items.chest1.page", 3);
        shopConfig.set("items.chest1.category", "Decorations");

        shopConfig.set("items.enderchest1.material", "ENDER_CHEST");
        shopConfig.set("items.enderchest1.price", 200);
        shopConfig.set("items.enderchest1.sellPrice", 50);
        shopConfig.set("items.enderchest1.amount", 1);
        shopConfig.set("items.enderchest1.slot", 34);
        shopConfig.set("items.enderchest1.page", 3);
        shopConfig.set("items.enderchest1.category", "Decorations");


        shopConfig.set("items.netherrack.material", "NETHERRACK");
        shopConfig.set("items.netherrack.price", 2);
        shopConfig.set("items.netherrack.sellPrice", 0.5);
        shopConfig.set("items.netherrack.amount", 1);
        shopConfig.set("items.netherrack.slot", 10);
        shopConfig.set("items.netherrack.page", 1);
        shopConfig.set("items.netherrack.category", "Nether");

        shopConfig.set("items.soulsoil.material", "SOUL_SOIL");
        shopConfig.set("items.soulsoil.price", 12);
        shopConfig.set("items.soulsoil.sellPrice", 3);
        shopConfig.set("items.soulsoil.amount", 1);
        shopConfig.set("items.soulsoil.slot", 12);
        shopConfig.set("items.soulsoil.page", 1);
        shopConfig.set("items.soulsoil.category", "Nether");

        shopConfig.set("items.soulsand.material", "SOUL_SAND");
        shopConfig.set("items.soulsand.price", 10);
        shopConfig.set("items.soulsand.sellPrice", 2);
        shopConfig.set("items.soulsand.amount", 1);
        shopConfig.set("items.soulsand.slot", 14);
        shopConfig.set("items.soulsand.page", 1);
        shopConfig.set("items.soulsand.category", "Nether");

        shopConfig.set("items.magmablock.material", "MAGMA_BLOCK");
        shopConfig.set("items.magmablock.price", 15);
        shopConfig.set("items.magmablock.sellPrice", 4);
        shopConfig.set("items.magmablock.amount", 1);
        shopConfig.set("items.magmablock.slot", 16);
        shopConfig.set("items.magmablock.page", 1);
        shopConfig.set("items.magmablock.category", "Nether");

        shopConfig.set("items.netherwartblock.material", "NETHER_WART_BLOCK");
        shopConfig.set("items.netherwartblock.price", 30);
        shopConfig.set("items.netherwartblock.sellPrice", 8);
        shopConfig.set("items.netherwartblock.amount", 1);
        shopConfig.set("items.netherwartblock.slot", 19);
        shopConfig.set("items.netherwartblock.page", 1);
        shopConfig.set("items.netherwartblock.category", "Nether");

        shopConfig.set("items.blackstone.material", "BLACKSTONE");
        shopConfig.set("items.blackstone.price", 30);
        shopConfig.set("items.blackstone.sellPrice", 8);
        shopConfig.set("items.blackstone.amount", 1);
        shopConfig.set("items.blackstone.slot", 21);
        shopConfig.set("items.blackstone.page", 1);
        shopConfig.set("items.blackstone.category", "Nether");

        shopConfig.set("items.gildedblackstone.material", "GILDED_BLACKSTONE");
        shopConfig.set("items.gildedblackstone.price", 100);
        shopConfig.set("items.gildedblackstone.sellPrice", 25);
        shopConfig.set("items.gildedblackstone.amount", 1);
        shopConfig.set("items.gildedblackstone.slot", 23);
        shopConfig.set("items.gildedblackstone.page", 1);
        shopConfig.set("items.gildedblackstone.category", "Nether");

        shopConfig.set("items.crimsonstem.material", "CRIMSON_STEM");
        shopConfig.set("items.crimsonstem.price", 30);
        shopConfig.set("items.crimsonstem.sellPrice", 8);
        shopConfig.set("items.crimsonstem.amount", 1);
        shopConfig.set("items.crimsonstem.slot", 25);
        shopConfig.set("items.crimsonstem.page", 1);
        shopConfig.set("items.crimsonstem.category", "Nether");

        shopConfig.set("items.netherwart1.material", "NETHER_WART");
        shopConfig.set("items.netherwart1.price", 20);
        shopConfig.set("items.netherwart1.sellPrice", 5);
        shopConfig.set("items.netherwart1.amount", 1);
        shopConfig.set("items.netherwart1.slot", 28);
        shopConfig.set("items.netherwart1.page", 1);
        shopConfig.set("items.netherwart1.category", "Nether");

        shopConfig.set("items.warpedfungus.material", "WARPED_FUNGUS");
        shopConfig.set("items.warpedfungus.price", 15);
        shopConfig.set("items.warpedfungus.sellPrice", 4);
        shopConfig.set("items.warpedfungus.amount", 1);
        shopConfig.set("items.warpedfungus.slot", 30);
        shopConfig.set("items.warpedfungus.page", 1);
        shopConfig.set("items.warpedfungus.category", "Nether");

        shopConfig.set("items.obsidian1.material", "OBSIDIAN");
        shopConfig.set("items.obsidian1.price", 100);
        shopConfig.set("items.obsidian1.sellPrice", 25);
        shopConfig.set("items.obsidian1.amount", 1);
        shopConfig.set("items.obsidian1.slot", 32);
        shopConfig.set("items.obsidian1.page", 1);
        shopConfig.set("items.obsidian1.category", "Nether");

        shopConfig.set("items.ancientdebris1.material", "ANCIENT_DEBRIS");
        shopConfig.set("items.ancientdebris1.price", 1000);
        shopConfig.set("items.ancientdebris1.sellPrice", 250);
        shopConfig.set("items.ancientdebris1.amount", 1);
        shopConfig.set("items.ancientdebris1.slot", 34);
        shopConfig.set("items.ancientdebris1.page", 1);
        shopConfig.set("items.ancientdebris1.category", "Nether");

        shopConfig.set("items.soultorch.material", "SOUL_TORCH");
        shopConfig.set("items.soultorch.price", 10);
        shopConfig.set("items.soultorch.sellPrice", 2);
        shopConfig.set("items.soultorch.amount", 1);
        shopConfig.set("items.soultorch.slot", 10);
        shopConfig.set("items.soultorch.page", 2);
        shopConfig.set("items.soultorch.category", "Nether");

        shopConfig.set("items.soulcampfire.material", "SOUL_CAMPFIRE");
        shopConfig.set("items.soulcampfire.price", 100);
        shopConfig.set("items.soulcampfire.sellPrice", 25);
        shopConfig.set("items.soulcampfire.amount", 1);
        shopConfig.set("items.soulcampfire.slot", 12);
        shopConfig.set("items.soulcampfire.page", 2);
        shopConfig.set("items.soulcampfire.category", "Nether");

        shopConfig.set("items.soullantern.material", "SOUL_LANTERN");
        shopConfig.set("items.soullantern.price", 15);
        shopConfig.set("items.soullantern.sellPrice", 4);
        shopConfig.set("items.soullantern.amount", 1);
        shopConfig.set("items.soullantern.slot", 14);
        shopConfig.set("items.soullantern.page", 2);
        shopConfig.set("items.soullantern.category", "Nether");

        shopConfig.set("items.warpednylium.material", "WARPED_NYLIUM");
        shopConfig.set("items.warpednylium.price", 20);
        shopConfig.set("items.warpednylium.sellPrice", 5);
        shopConfig.set("items.warpednylium.amount", 1);
        shopConfig.set("items.warpednylium.slot", 16);
        shopConfig.set("items.warpednylium.page", 2);
        shopConfig.set("items.warpednylium.category", "Nether");

        shopConfig.set("items.crimsonnylium.material", "CRIMSON_NYLIUM");
        shopConfig.set("items.crimsonnylium.price", 20);
        shopConfig.set("items.crimsonnylium.sellPrice", 5);
        shopConfig.set("items.crimsonnylium.amount", 1);
        shopConfig.set("items.crimsonnylium.slot", 19);
        shopConfig.set("items.crimsonnylium.page", 2);
        shopConfig.set("items.crimsonnylium.category", "Nether");

        shopConfig.set("items.12134123.material", "WARPED_FUNGUS_ON_A_STICK");
        shopConfig.set("items.12134123.price", 50);
        shopConfig.set("items.12134123.sellPrice", 12);
        shopConfig.set("items.12134123.amount", 1);
        shopConfig.set("items.12134123.slot", 21);
        shopConfig.set("items.12134123.page", 2);
        shopConfig.set("items.12134123.category", "Nether");

        shopConfig.set("items.quartz.material", "QUARTZ");
        shopConfig.set("items.quartz.price", 30);
        shopConfig.set("items.quartz.sellPrice", 8);
        shopConfig.set("items.quartz.amount", 1);
        shopConfig.set("items.quartz.slot", 23);
        shopConfig.set("items.quartz.page", 2);
        shopConfig.set("items.quartz.category", "Nether");

        shopConfig.set("items.ghasttear1.material", "GHAST_TEAR");
        shopConfig.set("items.ghasttear1.price", 100);
        shopConfig.set("items.ghasttear1.sellPrice", 25);
        shopConfig.set("items.ghasttear1.amount", 1);
        shopConfig.set("items.ghasttear1.slot", 25);
        shopConfig.set("items.ghasttear1.page", 2);
        shopConfig.set("items.ghasttear1.category", "Nether");

        shopConfig.set("items.blazerod1.material", "BLAZE_ROD");
        shopConfig.set("items.blazerod1.price", 50);
        shopConfig.set("items.blazerod1.sellPrice", 12);
        shopConfig.set("items.blazerod1.amount", 1);
        shopConfig.set("items.blazerod1.slot", 28);
        shopConfig.set("items.blazerod1.page", 2);
        shopConfig.set("items.blazerod1.category", "Nether");

        shopConfig.set("items.crimsonfungus.material", "CRIMSON_FUNGUS");
        shopConfig.set("items.crimsonfungus.price", 15);
        shopConfig.set("items.crimsonfungus.sellPrice", 4);
        shopConfig.set("items.crimsonfungus.amount", 1);
        shopConfig.set("items.crimsonfungus.slot", 30);
        shopConfig.set("items.crimsonfungus.page", 2);
        shopConfig.set("items.crimsonfungus.category", "Nether");

        shopConfig.set("items.basalt.material", "BASALT");
        shopConfig.set("items.basalt.price", 15);
        shopConfig.set("items.basalt.sellPrice", 4);
        shopConfig.set("items.basalt.amount", 1);
        shopConfig.set("items.basalt.slot", 32);
        shopConfig.set("items.basalt.page", 2);
        shopConfig.set("items.basalt.category", "Nether");

        shopConfig.set("items.warpedstem.material", "WARPED_STEM");
        shopConfig.set("items.warpedstem.price", 30);
        shopConfig.set("items.warpedstem.sellPrice", 8);
        shopConfig.set("items.warpedstem.amount", 1);
        shopConfig.set("items.warpedstem.slot", 34);
        shopConfig.set("items.warpedstem.page", 2);
        shopConfig.set("items.warpedstem.category", "Nether");

        shopConfig.set("items.cryingobsidian.material", "CRYING_OBSIDIAN");
        shopConfig.set("items.cryingobsidian.price", 150);
        shopConfig.set("items.cryingobsidian.sellPrice", 35);
        shopConfig.set("items.cryingobsidian.amount", 1);
        shopConfig.set("items.cryingobsidian.slot", 10);
        shopConfig.set("items.cryingobsidian.page", 3);
        shopConfig.set("items.cryingobsidian.category", "Nether");

        shopConfig.set("items.shroomlight.material", "SHROOMLIGHT");
        shopConfig.set("items.shroomlight.price", 120);
        shopConfig.set("items.shroomlight.sellPrice", 30);
        shopConfig.set("items.shroomlight.amount", 1);
        shopConfig.set("items.shroomlight.slot", 12);
        shopConfig.set("items.shroomlight.page", 3);
        shopConfig.set("items.shroomlight.category", "Nether");

        shopConfig.set("items.witherrose.material", "WITHER_ROSE");
        shopConfig.set("items.witherrose.price", 100);
        shopConfig.set("items.witherrose.sellPrice", 25);
        shopConfig.set("items.witherrose.amount", 1);
        shopConfig.set("items.witherrose.slot", 14);
        shopConfig.set("items.witherrose.page", 3);
        shopConfig.set("items.witherrose.category", "Nether");

        shopConfig.set("items.witherskull1.material", "WITHER_SKELETON_SKULL");
        shopConfig.set("items.witherskull1.price", 1500);
        shopConfig.set("items.witherskull1.sellPrice", 375);
        shopConfig.set("items.witherskull1.amount", 1);
        shopConfig.set("items.witherskull1.slot", 16);
        shopConfig.set("items.witherskull1.page", 3);
        shopConfig.set("items.witherskull1.category", "Nether");

        shopConfig.set("items.netherstar.material", "NETHER_STAR");
        shopConfig.set("items.netherstar.price", 3000);
        shopConfig.set("items.netherstar.sellPrice", 750);
        shopConfig.set("items.netherstar.amount", 1);
        shopConfig.set("items.netherstar.slot", 19);
        shopConfig.set("items.netherstar.page", 3);
        shopConfig.set("items.netherstar.category", "Nether");

        shopConfig.set("items.pigstepdisc.material", "MUSIC_DISC_PIGSTEP");
        shopConfig.set("items.pigstepdisc.price", 1000);
        shopConfig.set("items.pigstepdisc.sellPrice", 250);
        shopConfig.set("items.pigstepdisc.amount", 1);
        shopConfig.set("items.pigstepdisc.slot", 21);
        shopConfig.set("items.pigstepdisc.page", 3);
        shopConfig.set("items.pigstepdisc.category", "Nether");

        shopConfig.set("items.goldnugget.material", "GOLD_NUGGET");
        shopConfig.set("items.goldnugget.price", 8);
        shopConfig.set("items.goldnugget.sellPrice", 2);
        shopConfig.set("items.goldnugget.amount", 1);
        shopConfig.set("items.goldnugget.slot", 23);
        shopConfig.set("items.goldnugget.page", 3);
        shopConfig.set("items.goldnugget.category", "Nether");

        shopConfig.set("items.goldingot1.material", "GOLD_INGOT");
        shopConfig.set("items.goldingot1.price", 80);
        shopConfig.set("items.goldingot1.sellPrice", 20);
        shopConfig.set("items.goldingot1.amount", 1);
        shopConfig.set("items.goldingot1.slot", 25);
        shopConfig.set("items.goldingot1.page", 3);
        shopConfig.set("items.goldingot1.category", "Nether");

        shopConfig.set("items.enderpearl1.material", "ENDER_PEARL");
        shopConfig.set("items.enderpearl1.price", 100);
        shopConfig.set("items.enderpearl1.sellPrice", 25);
        shopConfig.set("items.enderpearl1.amount", 1);
        shopConfig.set("items.enderpearl1.slot", 28);
        shopConfig.set("items.enderpearl1.page", 3);
        shopConfig.set("items.enderpearl1.category", "Nether");

        shopConfig.set("items.lavabucket.material", "LAVA_BUCKET");
        shopConfig.set("items.lavabucket.price", 40);
        shopConfig.set("items.lavabucket.sellPrice", 10);
        shopConfig.set("items.lavabucket.amount", 1);
        shopConfig.set("items.lavabucket.slot", 30);
        shopConfig.set("items.lavabucket.page", 3);
        shopConfig.set("items.lavabucket.category", "Nether");

        shopConfig.set("items.glowstonedust.material", "GLOWSTONE_DUST");
        shopConfig.set("items.glowstonedust.price", 20);
        shopConfig.set("items.glowstonedust.sellPrice", 5);
        shopConfig.set("items.glowstonedust.amount", 1);
        shopConfig.set("items.glowstonedust.slot", 32);
        shopConfig.set("items.glowstonedust.page", 3);
        shopConfig.set("items.glowstonedust.category", "Nether");

        shopConfig.set("items.zombie_spawner.material", "SPAWNER");
        shopConfig.set("items.zombie_spawner.price", 5000);
        shopConfig.set("items.zombie_spawner.sellPrice", 2500);
        shopConfig.set("items.zombie_spawner.amount", 1);
        shopConfig.set("items.zombie_spawner.slot", 16);
        shopConfig.set("items.zombie_spawner.page", 1);
        shopConfig.set("items.zombie_spawner.category", "Spawners");
        shopConfig.set("items.zombie_spawner.mobType", "ZOMBIE");










        try {
            shopConfig.save(shopFile);
            Bukkit.getLogger().info("[ServerEssentials] Generated default shopitems.yml with Pre-Coded Items for all sections.");
        } catch (IOException e) {
            Bukkit.getLogger().warning("[ServerEssentials] Could not save default shopitems.yml: " + e.getMessage());
        }
    }


    // Save shop items to the config file
    public static void saveShopItems() {
        if (shopConfig == null) {
            shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        }

        for (String section : ShopManager.getSections()) {
            for (ShopItem shopItem : ShopManager.getItems(section)) {
                // Use item's material + slot or unique identifier if you want to avoid conflicts
                String key = shopItem.getItem().getType().toString().toLowerCase();

                String path = "items." + key;
                shopConfig.set(path + ".material", shopItem.getItem().getType().toString());
                shopConfig.set(path + ".price", shopItem.getBuyPrice());
                shopConfig.set(path + ".sellPrice", shopItem.getSellPrice());
                shopConfig.set(path + ".amount", shopItem.getItem().getAmount());
                shopConfig.set(path + ".slot", shopItem.getSlot());
                shopConfig.set(path + ".page", shopItem.getPage());
                shopConfig.set(path + ".category", shopItem.getSection());

                // Save mobType if it exists (for spawners)
                if (shopItem.getMobType() != null) {
                    shopConfig.set(path + ".mobType", shopItem.getMobType().toString());
                } else {
                    // Remove mobType key if it exists and this item doesn't have one
                    shopConfig.set(path + ".mobType", null);
                }
            }
        }

        try {
            shopConfig.save(shopFile);
            Bukkit.getLogger().info("[ServerEssentials] Saved shop items to shopitems.yml!");
        } catch (IOException e) {
            Bukkit.getLogger().warning("[ServerEssentials] Could not save shopitems.yml: " + e.getMessage());
        }
    }


    // Reload the config from disk
    public static void reloadShopConfig() {
        if (shopFile == null) {
            shopFile = new File(Bukkit.getServer().getPluginManager().getPlugin("ServerEssentials").getDataFolder(), "shopitems.yml");
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);

        // Reload the items from the config after reloading
        loadShopItems();
    }
}
