package com.troller2705.satisfactory.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = "satisfactory")
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        // Generates the JSONs into src/generated/resources/data/satisfactory/recipe/
        generator.addProvider(
                event.includeServer(),
                new SatisfactoryRecipeProvider(packOutput, "satisfactory")
        );
    }
}