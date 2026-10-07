package com.troller2705.satisfactory.datagen;

import com.troller2705.satisfactory.Satisfactory;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SatisfactoryItemModelProvider extends ItemModelProvider {

    public SatisfactoryItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Satisfactory.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Loop through EVERY item we registered in the main class
        for (DeferredHolder<Item, ? extends Item> item : Satisfactory.ITEMS.getEntries()) {
            String path = item.getId().getPath();

            // Skip buckets because they require a dynamic fluid model
            if (path.contains("bucket") || path.contains("canister")) {
                continue;
            }

            // Generate standard item/generated JSON models for everything else
            simpleItem(path);
        }
    }

    private void simpleItem(String name) {
        withExistingParent(name, ResourceLocation.parse("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "item/" + name));
    }
}