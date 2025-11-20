package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages custom crafting recipes.
 */
public class RecipeManager {
    private final Ecore plugin;
    private File recipesFile;
    private FileConfiguration recipesConfig;
    private final Map<String, Recipe> customRecipes;
    private final Set<NamespacedKey> registeredKeys;

    public RecipeManager(Ecore plugin) {
        this.plugin = plugin;
        this.customRecipes = new HashMap<>();
        this.registeredKeys = new HashSet<>();
        initializeRecipesConfig();
        loadRecipes();
    }

    private void initializeRecipesConfig() {
        recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            try {
                recipesFile.createNewFile();
                createDefaultRecipes();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create recipes.yml", e);
            }
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
    }

    private void createDefaultRecipes() {
        recipesConfig.set("recipes", null);
        recipesConfig.set("recipes.example-shapeless.name", "Example Shapeless Recipe");
        recipesConfig.set("recipes.example-shapeless.result", "DIAMOND");
        recipesConfig.set("recipes.example-shapeless.ingredients", Arrays.asList("COBBLESTONE", "IRON_INGOT"));
        recipesConfig.set("recipes.example-shapeless.permission", "ecore.recipe.example");
        
        recipesConfig.set("recipes.example-shaped.name", "Example Shaped Recipe");
        recipesConfig.set("recipes.example-shaped.result", "EMERALD");
        recipesConfig.set("recipes.example-shaped.shape", Arrays.asList("ABC", "DEF", "GHI"));
        recipesConfig.set("recipes.example-shaped.ingredients.A", "DIAMOND");
        recipesConfig.set("recipes.example-shaped.ingredients.B", "GOLD_INGOT");
        recipesConfig.set("recipes.example-shaped.permission", "ecore.recipe.example");
        
        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save default recipes", e);
        }
    }

    private void loadRecipes() {
        if (recipesConfig.getConfigurationSection("recipes") == null) return;

        for (String recipeId : recipesConfig.getConfigurationSection("recipes").getKeys(false)) {
            String path = "recipes." + recipeId;
            String name = recipesConfig.getString(path + ".name", recipeId);
            String resultStr = recipesConfig.getString(path + ".result");
            
            if (resultStr == null) continue;
            
            Material resultMaterial;
            try {
                resultMaterial = Material.valueOf(resultStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid result material for recipe " + recipeId + ": " + resultStr);
                continue;
            }

            ItemStack result = new ItemStack(resultMaterial);
            NamespacedKey key = new NamespacedKey(plugin, recipeId.toLowerCase());

            // Check if it's shaped or shapeless
            if (recipesConfig.contains(path + ".shape")) {
                // Shaped recipe
                List<String> shape = recipesConfig.getStringList(path + ".shape");
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(shape.toArray(new String[0]));

                // Set ingredients
                if (recipesConfig.contains(path + ".ingredients")) {
                    for (String ingredientKey : recipesConfig.getConfigurationSection(path + ".ingredients").getKeys(false)) {
                        String ingredientStr = recipesConfig.getString(path + ".ingredients." + ingredientKey);
                        try {
                            Material ingredient = Material.valueOf(ingredientStr.toUpperCase());
                            recipe.setIngredient(ingredientKey.charAt(0), ingredient);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid ingredient for recipe " + recipeId + ": " + ingredientStr);
                        }
                    }
                }

                customRecipes.put(recipeId, recipe);
            } else {
                // Shapeless recipe
                ShapelessRecipe recipe = new ShapelessRecipe(key, result);
                List<String> ingredients = recipesConfig.getStringList(path + ".ingredients");
                
                for (String ingredientStr : ingredients) {
                    try {
                        Material ingredient = Material.valueOf(ingredientStr.toUpperCase());
                        recipe.addIngredient(ingredient);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid ingredient for recipe " + recipeId + ": " + ingredientStr);
                    }
                }

                customRecipes.put(recipeId, recipe);
            }

            // Register recipe
            try {
                plugin.getServer().addRecipe(customRecipes.get(recipeId));
                registeredKeys.add(key);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to register recipe " + recipeId, e);
            }
        }
    }

    /**
     * Creates a new shaped recipe.
     */
    public boolean createShapedRecipe(String recipeId, String name, ItemStack result, List<String> shape, Map<Character, Material> ingredients, String permission) {
        if (customRecipes.containsKey(recipeId)) {
            return false; // Recipe already exists
        }

        NamespacedKey key = new NamespacedKey(plugin, recipeId.toLowerCase());
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }

        // Save to config
        String path = "recipes." + recipeId;
        recipesConfig.set(path + ".name", name);
        recipesConfig.set(path + ".result", result.getType().toString());
        recipesConfig.set(path + ".shape", shape);
        recipesConfig.set(path + ".permission", permission);
        
        Map<String, String> ingredientsMap = new HashMap<>();
        for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
            ingredientsMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        recipesConfig.set(path + ".ingredients", ingredientsMap);

        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save recipe", e);
            return false;
        }

        customRecipes.put(recipeId, recipe);
        plugin.getServer().addRecipe(recipe);
        registeredKeys.add(key);
        return true;
    }

    /**
     * Creates a new shapeless recipe.
     */
    public boolean createShapelessRecipe(String recipeId, String name, ItemStack result, List<Material> ingredients, String permission) {
        if (customRecipes.containsKey(recipeId)) {
            return false; // Recipe already exists
        }

        NamespacedKey key = new NamespacedKey(plugin, recipeId.toLowerCase());
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        
        for (Material ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }

        // Save to config
        String path = "recipes." + recipeId;
        recipesConfig.set(path + ".name", name);
        recipesConfig.set(path + ".result", result.getType().toString());
        recipesConfig.set(path + ".ingredients", ingredients.stream().map(Enum::toString).toList());
        recipesConfig.set(path + ".permission", permission);

        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save recipe", e);
            return false;
        }

        customRecipes.put(recipeId, recipe);
        plugin.getServer().addRecipe(recipe);
        registeredKeys.add(key);
        return true;
    }

    /**
     * Removes a recipe.
     */
    public boolean removeRecipe(String recipeId) {
        Recipe recipe = customRecipes.remove(recipeId);
        if (recipe == null) {
            return false;
        }

        // Remove from server
        NamespacedKey key = new NamespacedKey(plugin, recipeId.toLowerCase());
        Iterator<Recipe> iterator = plugin.getServer().recipeIterator();
        while (iterator.hasNext()) {
            Recipe r = iterator.next();
            if (r instanceof ShapedRecipe && ((ShapedRecipe) r).getKey().equals(key)) {
                iterator.remove();
            } else if (r instanceof ShapelessRecipe && ((ShapelessRecipe) r).getKey().equals(key)) {
                iterator.remove();
            }
        }

        registeredKeys.remove(key);

        // Remove from config
        recipesConfig.set("recipes." + recipeId, null);
        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save recipes config", e);
        }

        return true;
    }

    /**
     * Gets all custom recipe IDs.
     */
    public List<String> getRecipeIds() {
        return new ArrayList<>(customRecipes.keySet());
    }

    /**
     * Gets a recipe by ID.
     */
    public Recipe getRecipe(String recipeId) {
        return customRecipes.get(recipeId);
    }

    /**
     * Checks if a recipe exists.
     */
    public boolean hasRecipe(String recipeId) {
        return customRecipes.containsKey(recipeId);
    }

    /**
     * Reloads recipes from config.
     */
    public void reload() {
        // Remove all registered recipes
        for (NamespacedKey key : registeredKeys) {
            Iterator<Recipe> iterator = plugin.getServer().recipeIterator();
            while (iterator.hasNext()) {
                Recipe r = iterator.next();
                if (r instanceof ShapedRecipe && ((ShapedRecipe) r).getKey().equals(key)) {
                    iterator.remove();
                } else if (r instanceof ShapelessRecipe && ((ShapelessRecipe) r).getKey().equals(key)) {
                    iterator.remove();
                }
            }
        }

        customRecipes.clear();
        registeredKeys.clear();
        
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
        loadRecipes();
    }
}

