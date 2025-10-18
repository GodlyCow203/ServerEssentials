package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import serveressentials.serveressentials.util.PlayerMessages;

public class RecipeCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public RecipeCommand(PlayerMessages messages) {
        this.messages = messages;

        // Add defaults if missing
        messages.addDefault("recipe.usage", "<red>Usage: /recipe <recipe>");
        messages.addDefault("recipe.not-found", "<red>Recipe not found!");
        messages.addDefault("recipe.result", "<green>Recipe for: <yellow>{item}");
        messages.addDefault("recipe.shaped-header", "<aqua>Shaped Recipe Ingredients:");
        messages.addDefault("recipe.shapeless-header", "<aqua>Shapeless Recipe Ingredients:");
        messages.addDefault("recipe.ingredient-line", "<yellow>- {char}: {item} x{amount}");
        messages.addDefault("recipe.ingredient-line-shapeless", "<yellow>- {item} x{amount}");
        messages.addDefault("recipe.unknown-type", "<red>Unknown recipe type!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.get("recipe.usage"));
            return true;
        }

        NamespacedKey key = NamespacedKey.minecraft(args[0]);
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
}
