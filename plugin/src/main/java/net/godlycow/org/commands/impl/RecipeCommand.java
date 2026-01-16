package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.RecipeConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class RecipeCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "essc.command.recipe";

    private final PlayerLanguageManager langManager;
    private final RecipeConfig config;
    private final CommandDataStorage dataStorage;

    public RecipeCommand(PlayerLanguageManager langManager, RecipeConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.recipe.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.usage",
                    "<red>Usage: <white>/recipe <recipe>"));
            return true;
        }

        NamespacedKey key = NamespacedKey.fromString(args[0].toLowerCase());
        if (key == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.not-found",
                    "<red>Recipe not found!"));
            return true;
        }

        Recipe recipe = Bukkit.getRecipe(key);
        if (recipe == null) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.not-found",
                    "<red>Recipe not found!"));
            return true;
        }

        ItemStack result = recipe.getResult();
        player.sendMessage(langManager.getMessageFor(player, "commands.recipe.result",
                "<green>Recipe for: <yellow>{item}",
                ComponentPlaceholder.of("{item}", result.getType().toString())));

        if (recipe instanceof ShapedRecipe shaped) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.shaped-header",
                    "<aqua>Shaped Recipe Ingredients:"));

            shaped.getIngredientMap().forEach((character, itemStack) -> {
                if (itemStack != null && itemStack.getType() != org.bukkit.Material.AIR) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.recipe.ingredient-line",
                            "<yellow>- {char}: {item} x{amount}",
                            ComponentPlaceholder.of("{char}", character.toString()),
                            ComponentPlaceholder.of("{item}", itemStack.getType().toString()),
                            ComponentPlaceholder.of("{amount}", String.valueOf(itemStack.getAmount()))));
                }
            });
        }
        else if (recipe instanceof ShapelessRecipe shapeless) {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.shapeless-header",
                    "<aqua>Shapeless Recipe Ingredients:"));

            for (ItemStack ingredient : shapeless.getIngredientList()) {
                if (ingredient != null && ingredient.getType() != org.bukkit.Material.AIR) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.recipe.ingredient-line-shapeless",
                            "<yellow>- {item} x{amount}",
                            ComponentPlaceholder.of("{item}", ingredient.getType().toString()),
                            ComponentPlaceholder.of("{amount}", String.valueOf(ingredient.getAmount()))));
                }
            }
        }
        else {
            player.sendMessage(langManager.getMessageFor(player, "commands.recipe.unknown-type",
                    "<red>Unknown recipe type!"));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "recipe", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "recipe", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "recipe", "last_recipe", recipe.getResult().getType().toString());
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> results = new ArrayList<>();

            Bukkit.recipeIterator().forEachRemaining(recipe -> {
                if (recipe instanceof Keyed keyed && recipe.getResult() != null) {
                    NamespacedKey key = keyed.getKey();
                    String name = key.toString();

                    if (name.toLowerCase().startsWith(current)) {
                        results.add(name);
                    }
                }
            });

            return results;
        }

        return Collections.emptyList();
    }
}