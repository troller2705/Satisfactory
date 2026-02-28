package com.troller2705.satisfactory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Satisfactory.MODID)
public class Satisfactory
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "satisfactory";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "satisfactory" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "satisfactory" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "satisfactory" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);

    // 1. Define your item names by Tier or Category
    // Ores and Raw Resources (For your Nodes/Smelter system)
    private static final List<String> RESOURCES = List.of(
            "raw_caterium", "sulfur", "bauxite", "uranium", "sam_ore", "compacted_coal", "quartz_crystal", "flower_petals"
    );

    // Tier 0 - Hub Essentials
    private static final List<String> TIER_0 = List.of(
            "iron_plate", "iron_rod", "screw", "screw_bundle", "reinforced_iron_plate",
            "copper_sheet", "wire", "cable", "concrete", "biomass", "solid_biofuel"
    );

    // Tier 3 & 4 - Coal & Steel
    private static final List<String> TIER_3_4 = List.of(
            "steel_ingot", "steel_beam", "steel_pipe", "encased_industrial_beam",
            "modular_frame", "rotor", "stator", "motor", "black_powder"
    );

    // Tier 5 & 6 - Oil & Electronics
    private static final List<String> TIER_5_6 = List.of(
            "plastic", "rubber", "circuit_board", "heavy_modular_frame", "computer",
            "caterium_ingot", "quickwire", "ai_limiter", "high_speed_connector", "packaged_fuel"
    );

    // Tier 7 & 8 - Bauxite & Nuclear
    private static final List<String> TIER_7_8 = List.of(
            "alclad_aluminum_sheet", "aluminum_casing", "radio_control_unit", "cooling_system",
            "supercomputer", "fused_modular_frame", "uranium_fuel_rod", "electromagnetic_control_rod",
            "pressure_conversion_cube", "turbo_motor"
    );

    // Space Elevator Phases
    private static final List<String> PROJECT_PARTS = List.of(
            "smart_plating", "versatile_framework", "automated_wiring", "modular_engine",
            "adaptive_wiring", "assembly_director_system", "magnetic_field_generator",
            "thermal_propulsion_rocket", "nuclear_pasta"
    );

    // Fluids (Represented as items/buckets for your Mixer/Refinery recipes)
    private static final List<String> FLUIDS_ITEMS = List.of(
            "heavy_oil_residue_bucket", "fuel_bucket", "liquid_biofuel_bucket",
            "alumina_solution_bucket", "sulfuric_acid_bucket", "nitrogen_gas_canister", "nitric_acid_bucket"
    );

    // 2. Batch Registering
    static {
        registerBatch(RESOURCES);
        registerBatch(TIER_0);
        registerBatch(TIER_3_4);
        registerBatch(TIER_5_6);
        registerBatch(TIER_7_8);
        registerBatch(PROJECT_PARTS);
        registerBatch(FLUIDS_ITEMS);
    }

    private static void registerBatch(List<String> names) {
        for (String name : names) {
            ITEMS.register(name, () -> new Item(new Item.Properties()));
        }
    }

    // Special Items
    public static final DeferredItem<Item> URANIUM_WASTE = ITEMS.register("uranium_waste",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> POWER_SHARD = ITEMS.register("power_shard",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<Item> BLUE_POWER_SLUG = ITEMS.register("blue_power_slug",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> YELLOW_POWER_SLUG = ITEMS.register("yellow_power_slug",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> PURPLE_POWER_SLUG = ITEMS.register("purple_power_slug",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // --- FLUID REGISTRATION ---

    public static final DeferredHolder<FluidType, FluidType> CRUDE_OIL_TYPE = FLUID_TYPES.register("crude_oil",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.satisfactory.crude_oil")
                    .viscosity(2000)
                    .density(1200)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    crudeOilClientExtensions(consumer);
                }
            });

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> CRUDE_OIL = FLUIDS.register("crude_oil",
            () -> new BaseFlowingFluid.Source(Satisfactory.CRUDE_OIL_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> CRUDE_OIL_FLOWING = FLUIDS.register("crude_oil_flowing",
            () -> new BaseFlowingFluid.Flowing(Satisfactory.CRUDE_OIL_PROPERTIES));

    // Register Bucket manually to link to the Fluid
    public static final DeferredItem<BucketItem> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(CRUDE_OIL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties CRUDE_OIL_PROPERTIES = new BaseFlowingFluid.Properties(
            CRUDE_OIL_TYPE, CRUDE_OIL, CRUDE_OIL_FLOWING).bucket(CRUDE_OIL_BUCKET);

    private static void crudeOilClientExtensions(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            // You can use vanilla water/lava textures temporarily or custom ones
            private static final ResourceLocation STILL = ResourceLocation.parse("minecraft:block/water_still");
            private static final ResourceLocation FLOW = ResourceLocation.parse("minecraft:block/water_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFF1A1A1A; // Dark grey/black for Crude Oil
            }
        });
    }

    // Creates a creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SATISFACTORY_BASE =
            CREATIVE_MODE_TABS.register("satisfactory_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.satisfactory"))
                    .icon(() -> ITEMS.getEntries().stream()
                            .filter(e -> e.getId().getPath().equals("fused_modular_frame"))
                            .findFirst()
                            .map(holder -> (Item) holder.get()) // Explicit cast to Item
                            .orElse(Items.IRON_INGOT) // Fallback to a vanilla item if the search fails
                            .getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    }).build());

    public Satisfactory(IEventBus modEventBus, ModContainer modContainer)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        generateLangEntries();
    }

    public static void generateLangEntries() {
        ITEMS.getEntries().forEach(item -> {
            String name = item.getId().getPath();
            // Converts "heavy_modular_frame" to "Heavy Modular Frame"
            String clearName = Arrays.stream(name.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));

            System.out.println("\"item." + MODID + "." + name + "\": \"" + clearName + "\",");
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    private static Item getItemByName(String name) {
        return ITEMS.getEntries().stream()
                .filter(e -> e.getId().getPath().equals(name))
                .findFirst()
                .map(holder -> (Item) holder.get())
                .orElse(Items.BARRIER); // Barrier makes it obvious in-game if an item is missing
    }

}
