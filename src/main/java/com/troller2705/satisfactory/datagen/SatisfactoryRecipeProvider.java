package com.troller2705.satisfactory.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items; // Replace with your registered Satisfactory items

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SatisfactoryRecipeProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final PackOutput.PathProvider recipePathProvider;
    private final String modId;

    public SatisfactoryRecipeProvider(PackOutput packOutput, String modId) {
        this.recipePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.modId = modId;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<SatisfactoryMixingBuilder> recipes = new ArrayList<>();

        buildRecipes(recipes);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (SatisfactoryMixingBuilder recipe : recipes) {
            Path path = recipePathProvider.json(recipe.getId());
            JsonObject json = recipe.serializeToJson();
            futures.add(DataProvider.saveStable(output, json, path));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void buildRecipes(List<SatisfactoryMixingBuilder> recipes) {
        // Example 1: Heavy Modular Frame (2.0 / min, Batch size 1 = 30s cycle)
        // Automatically unrolls the 42 inputs into 42 individual JSON entries
        recipes.add(
                SatisfactoryMixingBuilder.create(modId, "heavy_modular_frame")
                        .input(Items.IRON_BLOCK, 5)            // replace with ModItems.MODULAR_FRAME
                        .input(Items.IRON_INGOT, 15)           // replace with ModItems.STEEL_PIPE
                        .input(Items.IRON_BARS, 5)             // replace with ModItems.ENCASED_INDUSTRIAL_BEAM
                        .input(Items.NAUTILUS_SHELL, 12)       // replace with ModItems.SCREW_BUNDLE
                        .output(Items.NETHERITE_BLOCK, 1)      // replace with ModItems.HEAVY_MODULAR_FRAME
                        .rate(2.0, 1)                          // 2.0/min -> processing_time: 1000
        );

        // Example 2: Space Elevator Part (0.3 / min, Batch size 1 = 200s cycle)
        recipes.add(
                SatisfactoryMixingBuilder.create(modId, "thermal_propulsion_rocket")
                        .input(Items.COPPER_BLOCK, 3)
                        .input(Items.REDSTONE_BLOCK, 2)
                        .output(Items.BEACON, 1)
                        .rate(0.3, 1)                          // 0.3/min -> processing_time: 6667
                        .superheated()
        );

        // Example 3: High-Speed Burst (1500 / min, Batch size 100 = 4s cycle)
        recipes.add(
                SatisfactoryMixingBuilder.create(modId, "high_speed_connector")
                        .input(Items.GOLD_INGOT, 4)
                        .input(Items.COPPER_INGOT, 1)
                        .output(Items.COMPASS, 100)
                        .rate(1500.0, 100)                     // 1500/min @ 100 per craft -> processing_time: 133
        );

        // Example 4: Fixed 10s Cycle
        recipes.add(
                SatisfactoryMixingBuilder.create(modId, "rotor_standard")
                        .input(Items.IRON_INGOT, 5)
                        .input(Items.CHAIN, 25)
                        .output(Items.CLOCK, 1)
                        .cycleSeconds(10.0)                    // 10s -> processing_time: 333
        );
    }

    @Override
    public String getName() {
        return "Satisfactory Create Mixing Recipes";
    }
}