package com.troller2705.satisfactory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
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

@Mod(Satisfactory.MODID)
public class Satisfactory
{
    public static final String MODID = "satisfactory";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, MODID);

    // Register our Hub Menu
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.troller2705.satisfactory.menu.HubTerminalMenu>> HUB_TERMINAL_MENU =
            MENUS.register("hub_terminal",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.troller2705.satisfactory.menu.HubTerminalMenu::new));

    // 2. Register the Block
    public static final DeferredBlock<Block> HUB_TERMINAL_BLOCK = BLOCKS.register("hub_terminal",
            () -> new com.troller2705.satisfactory.blocks.HubTerminalBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
            ));

    // 3. Register the Item
    public static final DeferredItem<BlockItem> HUB_TERMINAL_ITEM = ITEMS.register("hub_terminal",
            () -> new BlockItem(HUB_TERMINAL_BLOCK.get(), new Item.Properties()));

    // 4. Register the Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.troller2705.satisfactory.blockentities.HubTerminalBlockEntity>> HUB_TERMINAL_BE =
            BLOCK_ENTITIES.register("hub_terminal",
                    () -> BlockEntityType.Builder.of(
                            com.troller2705.satisfactory.blockentities.HubTerminalBlockEntity::new,
                            HUB_TERMINAL_BLOCK.get()
                    ).build(null));

    // --- HELPER METHODS ---
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, java.util.function.Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    // --- BATCH ITEM LISTS ---

    private static final List<String> RESOURCES = List.of(
            "raw_caterium", "sulfur", "raw_bauxite", "raw_uranium","sam",
            "compacted_coal", "quartz_crystal", "flower_petals", "iron_ore", "copper_ore", "limestone", "coal", "raw_quartz"
    );

    private static final List<String> TIER_0 = List.of(
            "iron_ingot", "copper_ingot", "iron_plate", "iron_rod", "screw", "screw_bundle", "reinforced_iron_plate",
            "copper_sheet", "wire", "cable", "concrete", "biomass", "solid_biofuel"
    );

    private static final List<String> TIER_3_4 = List.of(
            "steel_ingot", "steel_beam", "steel_pipe", "encased_industrial_beam",
            "modular_frame", "rotor", "stator", "motor", "black_powder"
    );

    private static final List<String> TIER_5_6 = List.of(
            "plastic", "rubber", "circuit_board", "heavy_modular_frame", "computer",
            "caterium_ingot", "quickwire", "ai_limiter", "high_speed_connector", "packaged_fuel"
    );

    private static final List<String> TIER_7_8 = List.of(
            "alclad_aluminum_sheet", "aluminum_casing", "radio_control_unit", "cooling_system",
            "supercomputer", "fused_modular_frame", "uranium_fuel_rod", "electromagnetic_control_rod",
            "pressure_conversion_cube", "turbo_motor"
    );

    private static final List<String> PROJECT_PARTS = List.of(
            "smart_plating", "versatile_framework", "automated_wiring", "modular_engine",
            "adaptive_control_unit", "assembly_director_system", "magnetic_field_generator",
            "thermal_propulsion_rocket", "nuclear_pasta"
    );

    private static final List<String> INTERMEDIATES = List.of(
            "silica", "polymer_resin", "petroleum_coke", "empty_canister", "fabric",
            "aluminum_scrap", "aluminum_ingot", "heat_sink", "battery", "copper_powder"
    );

    private static final List<String> NUCLEAR_CHAIN = List.of(
            "non_fissile_uranium", "plutonium_pellet", "encased_plutonium_cell",
            "plutonium_fuel_rod", "plutonium_waste"
    );

    private static final List<String> EXPLORATION = List.of(
            "leaves", "wood", "mycelia", "alien_protein", "alien_dna_capsule",
            "somersloop", "mercer_sphere"
    );

    private static final List<String> FLUIDS_ITEMS = List.of(
            "heavy_oil_residue_bucket", "fuel_bucket", "liquid_biofuel_bucket",
            "alumina_solution_bucket", "sulfuric_acid_bucket", "nitrogen_gas_canister", "nitric_acid_bucket",
            "water_bucket", "turbofuel_bucket"
    );

    // 2. Batch Registering
    static {
        registerBatch(RESOURCES);
        registerBatch(TIER_0);
        registerBatch(TIER_3_4);
        registerBatch(TIER_5_6);
        registerBatch(TIER_7_8);
        registerBatch(PROJECT_PARTS);
        registerBatch(INTERMEDIATES);
        registerBatch(NUCLEAR_CHAIN);
        registerBatch(EXPLORATION);
        registerBatch(FLUIDS_ITEMS);
    }

    private static void registerBatch(List<String> names) {
        for (String name : names) {
            ITEMS.register(name, () -> new Item(new Item.Properties()));
        }
    }

    // --- MANUAL SPECIAL ITEM REGISTRATION ---
    // (These were removed from the string lists above to prevent duplicate registry crashes)

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

    public static final DeferredItem<BucketItem> CRUDE_OIL_BUCKET = ITEMS.register("crude_oil_bucket",
            () -> new BucketItem(CRUDE_OIL.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties CRUDE_OIL_PROPERTIES = new BaseFlowingFluid.Properties(
            CRUDE_OIL_TYPE, CRUDE_OIL, CRUDE_OIL_FLOWING).bucket(CRUDE_OIL_BUCKET);

    private static void crudeOilClientExtensions(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.parse("minecraft:block/water_still");
            private static final ResourceLocation FLOW = ResourceLocation.parse("minecraft:block/water_flow");

            @Override
            public ResourceLocation getStillTexture() { return STILL; }

            @Override
            public ResourceLocation getFlowingTexture() { return FLOW; }

            @Override
            public int getTintColor() { return 0xFF1A1A1A; }
        });
    }

    // --- ORE BLOCKS ---
    public static final DeferredBlock<Block> CATERIUM_ORE = registerBlock("caterium_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> SULFUR_ORE = registerBlock("sulfur_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BAUXITE_ORE = registerBlock("bauxite_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> QUARTZ_ORE = registerBlock("quartz_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> URANIUM_ORE = registerBlock("uranium_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 7), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(4.0f, 4.0f)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> SAM_ORE = registerBlock("sam_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 7), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(4.0f, 4.0f)
                    .requiresCorrectToolForDrops()));

    // --- CREATIVE TAB ---

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SATISFACTORY_BASE =
            CREATIVE_MODE_TABS.register("satisfactory_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.satisfactory"))
                    .icon(() -> ITEMS.getEntries().stream()
                            .filter(e -> e.getId().getPath().equals("fused_modular_frame"))
                            .findFirst()
                            .map(holder -> (Item) holder.get())
                            .orElse(Items.IRON_INGOT)
                            .getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    }).build());

    public Satisfactory(IEventBus modEventBus, ModContainer modContainer)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);

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
            String clearName = Arrays.stream(name.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));

            System.out.println("\"item." + MODID + "." + name + "\": \"" + clearName + "\",");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        // Add items to vanilla tabs if needed here. The empty if-statement was removed.
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    private static Item getItemByName(String name) {
        return ITEMS.getEntries().stream()
                .filter(e -> e.getId().getPath().equals(name))
                .findFirst()
                .map(holder -> (Item) holder.get())
                .orElse(Items.BARRIER);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Satisfactory.HUB_TERMINAL_MENU.get(), com.troller2705.satisfactory.gui.HubTerminalScreen::new);
    }
}