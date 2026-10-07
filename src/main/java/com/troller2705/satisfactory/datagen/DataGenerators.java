package com.troller2705.satisfactory.datagen;

import com.troller2705.satisfactory.Satisfactory;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Satisfactory.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Your existing Recipe provider
        generator.addProvider(
                event.includeServer(),
                new SatisfactoryRecipeProvider(packOutput, Satisfactory.MODID)
        );

        // NEW: Item Model provider
        generator.addProvider(
                event.includeClient(),
                new SatisfactoryItemModelProvider(packOutput, existingFileHelper)
        );
    }
}