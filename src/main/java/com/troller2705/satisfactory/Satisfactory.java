package com.troller2705.satisfactory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
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

    // 1. Define your item names by Tier or Category
    // Tier 0 - Hub Essentials
    private static final List<String> TIER_0 = List.of(
            "iron_rod", "iron_plate", "screw", "screw_bundle", "reinforced_iron_plate", "wire", "cable", "concrete"
    );

    // Tier 3 & 4 - Coal & Steel
    private static final List<String> TIER_3_4 = List.of(
            "steel_ingot", "steel_beam", "steel_pipe", "encased_industrial_beam", "modular_frame", "rotor", "stator", "motor"
    );

    // Tier 5 & 6 - Oil & Electronics
    private static final List<String> TIER_5_6 = List.of(
            "plastic", "rubber", "circuit_board", "heavy_modular_frame", "computer", "caterium_ingot", "quickwire", "ai_limiter", "high_speed_connector"
    );

    // Tier 7 & 8 - Bauxite & Nuclear
    private static final List<String> TIER_7_8 = List.of(
            "alclad_aluminum_sheet.json", "aluminum_casing", "radio_control_unit", "cooling_system", "supercomputer", "fused_modular_frame", "uranium_fuel_rod"
    );

    // Space Elevator Phases
    private static final List<String> PROJECT_PARTS = List.of(
            "smart_plating", "versatile_framework", "automated_wiring", "modular_engine", "adaptive_wiring", "assembly_director_system"
    );

    // 2. Batch Registering
    static {
        registerBatch(TIER_0);
        registerBatch(TIER_3_4);
        registerBatch(TIER_5_6);
        registerBatch(TIER_7_8);
        registerBatch(PROJECT_PARTS);
    }

    private static void registerBatch(List<String> names) {
        for (String name : names) {
            ITEMS.register(name, () -> new Item(new Item.Properties()));
        }
    }

    // 3. Manual Registration for special items (e.g., radioactive Uranium)
    public static final DeferredItem<Item> URANIUM_WASTE = ITEMS.register("uranium_waste",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> POWER_SHARD = ITEMS.register("power_shard",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredItem<Item> BLUE_POWER_SLUG = ITEMS.register("blue_power_slug",
            () -> new Item(new Item.Properties()));

    // Creates a creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SATISFACTORY_BASE =
            CREATIVE_MODE_TABS.register("tier_0_to_2", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.satisfactory"))
                    .icon(() -> ITEMS.getEntries().iterator().next().get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // This automatically adds every item we registered above!
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Satisfactory(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Satisfactory) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));

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
}
