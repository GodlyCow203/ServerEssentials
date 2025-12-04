package net.lunark.io.Player;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import net.lunark.io.util.PlayerMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeCommand implements CommandExecutor, TabCompleter {

    private final PlayerMessages messages;

    public RecipeCommand(PlayerMessages messages, JavaPlugin plugin) {
        this.messages = messages;

        messages.addDefault("recipe.usage", "<red>Usage: /recipe <recipe>");
        messages.addDefault("recipe.not-found", "<red>Recipe not found!");
        messages.addDefault("recipe.result", "<green>Recipe for: <yellow>{item}");
        messages.addDefault("recipe.shaped-header", "<aqua>Shaped Recipe Ingredients:");
        messages.addDefault("recipe.shapeless-header", "<aqua>Shapeless Recipe Ingredients:");
        messages.addDefault("recipe.ingredient-line", "<yellow>- {char}: {item} x{amount}");
        messages.addDefault("recipe.ingredient-line-shapeless", "<yellow>- {item} x{amount}");
        messages.addDefault("recipe.unknown-type", "<red>Unknown recipe type!");

        plugin.getCommand("recipe").setExecutor(this);
        plugin.getCommand("recipe").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.get("recipe.usage"));
            return true;
        }

        NamespacedKey key = NamespacedKey.minecraft(args[0].toLowerCase());
        Recipe recipe = Bukkit.getRecipe(key);

        if (recipe == null) {
            sender.sendMessage(messages.get("recipe.not-found"));
            return true;
        }

        ItemStack result = recipe.getResult();
        sender.sendMessage(messages.get("recipe.result", "{item}", result.getType().toString()));

        if (recipe instanceof ShapedRecipe shaped) {
            sender.sendMessage(messages.get("recipe.shaped-header"));
            shaped.getIngredientMap().forEach((character, itemStack) -> {
                if (itemStack != null)
                    sender.sendMessage(messages.get(
                            "recipe.ingredient-line",
                            "{char}", character.toString(),
                            "{item}", itemStack.getType().toString(),
                            "{amount}", String.valueOf(itemStack.getAmount())
                    ));
            });
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            sender.sendMessage(messages.get("recipe.shapeless-header"));
            for (ItemStack ingredient : shapeless.getIngredientList()) {
                if (ingredient != null)
                    sender.sendMessage(messages.get(
                            "recipe.ingredient-line-shapeless",
                            "{item}", ingredient.getType().toString(),
                            "{amount}", String.valueOf(ingredient.getAmount())
                    ));
            }
        } else {
            sender.sendMessage(messages.get("recipe.unknown-type"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> results = new ArrayList<>();

            for (Recipe recipe : Bukkit.getRecipesFor(new ItemStack(org.bukkit.Material.CRAFTING_TABLE))) {
            }

            Bukkit.recipeIterator().forEachRemaining(recipe -> {
                if (recipe == null || recipe.getResult() == null) return;

                NamespacedKey key = null;

                if (recipe instanceof Keyed keyed) {
                    key = keyed.getKey();
                }

                if (key != null) {
                    String name = key.getKey();
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
