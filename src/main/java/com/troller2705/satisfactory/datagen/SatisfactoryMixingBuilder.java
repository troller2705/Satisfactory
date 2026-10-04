package com.troller2705.satisfactory.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SatisfactoryMixingBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SatisfactoryMixingBuilder.class);

    private final ResourceLocation id;
    private final List<ResourceLocation> ingredients = new ArrayList<>();
    private final List<ResultEntry> results = new ArrayList<>();
    private int processingTime = 100;
    private String heatRequirement = null; // "heated", "superheated", or null

    private record ResultEntry(ResourceLocation itemId, int count) {}

    private SatisfactoryMixingBuilder(ResourceLocation id) {
        this.id = id;
    }

    public static SatisfactoryMixingBuilder create(String modId, String name) {
        return new SatisfactoryMixingBuilder(ResourceLocation.fromNamespaceAndPath(modId, name));
    }

    /**
     * Unrolls the ingredient count into individual items for Create's Basin.
     */
    public SatisfactoryMixingBuilder input(ItemLike item, int count) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.asItem());
        for (int i = 0; i < count; i++) {
            this.ingredients.add(itemId);
        }
        return this;
    }

    public SatisfactoryMixingBuilder input(ItemLike item) {
        return input(item, 1);
    }

    public SatisfactoryMixingBuilder output(ItemLike item, int count) {
        this.results.add(new ResultEntry(BuiltInRegistries.ITEM.getKey(item.asItem()), count));
        return this;
    }

    public SatisfactoryMixingBuilder output(ItemLike item) {
        return output(item, 1);
    }

    /**
     * Calculates processing_time from target items per minute and output batch size.
     */
    public SatisfactoryMixingBuilder rate(double itemsPerMinute, int batchSize) {
        double cyclesPerMinute = itemsPerMinute / (double) batchSize;
        double cycleSeconds = 60.0 / cyclesPerMinute;
        return cycleSeconds(cycleSeconds);
    }

    /**
     * Calculates processing_time based on total seconds per craft cycle.
     * Formula: processing_time = (Seconds * 20 Ticks) / 0.6
     */
    public SatisfactoryMixingBuilder cycleSeconds(double seconds) {
        double ticks = seconds * 20.0;
        this.processingTime = (int) Math.round(ticks / 0.6);
        return this;
    }

    public SatisfactoryMixingBuilder heated() {
        this.heatRequirement = "heated";
        return this;
    }

    public SatisfactoryMixingBuilder superheated() {
        this.heatRequirement = "superheated";
        return this;
    }

    public ResourceLocation getId() {
        return id;
    }

    public JsonObject serializeToJson() {
        if (ingredients.size() > 9) {
            LOGGER.warn("Recipe '{}' has {} unrolled input items! Standard Create Basins only hold 9 items.",
                    id, ingredients.size());
        }

        JsonObject root = new JsonObject();
        root.addProperty("type", "create:mixing");

        // Unrolled ingredients array
        JsonArray ingredientsArray = new JsonArray();
        for (ResourceLocation ingredient : ingredients) {
            JsonObject ingObj = new JsonObject();
            ingObj.addProperty("item", ingredient.toString());
            ingredientsArray.add(ingObj);
        }
        root.add("ingredients", ingredientsArray);

        // Results array
        JsonArray resultsArray = new JsonArray();
        for (ResultEntry res : results) {
            JsonObject resObj = new JsonObject();
            resObj.addProperty("id", res.itemId().toString());
            if (res.count() > 1) {
                resObj.addProperty("count", res.count());
            }
            resultsArray.add(resObj);
        }
        root.add("results", resultsArray);

        // Unclamped processing time
        root.addProperty("processing_time", this.processingTime);

        if (this.heatRequirement != null) {
            root.addProperty("heatRequirement", this.heatRequirement);
        }

        return root;
    }
}