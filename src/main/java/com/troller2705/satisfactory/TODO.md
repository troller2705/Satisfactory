# Satisfactory x Create (NeoForge 1.21.1) - Project Roadmap

## Phase 1: Core Engine Modifications (✅ Completed)
- [x] Configure `mods.toml` and `satisfactory.mixins.json`
- [x] Write `MechanicalMixerMixin.java` using `@Redirect` on `Mth.clamp` to bypass the 512-tick limit (increased to 16,384)
- [x] Establish the Golden Math Formula at 32 RPM: `Ticks = 0.6 * processing_time`
- [x] Define logistics baseline (Dual Mechanical Arms at 256 RPM for input, Multi-Brass Funnels for batched output)

## Phase 2: Recipe Datagen (🏗️ In Progress)
- [x] Create `SatisfactoryMixingBuilder` for fluent JSON generation
- [x] Implement math formula directly into Datagen `rate()` and `cycleSeconds()` methods
- [x] Add auto-unrolling logic for batch ingredients (bypassing Create's lack of input `count`)
- [x] Hook `SatisfactoryRecipeProvider` into NeoForge's `GatherDataEvent`
- [ ] Upgrade `SatisfactoryMixingBuilder` to support `FluidStack` inputs and outputs
- [ ] Upgrade `SatisfactoryMixingBuilder` to support Create Heat parameters (`heated`, `superheated`)

## Phase 3: Item & Fluid Registries (⏳ To Do)
- [ ] **Standard Items (`DeferredRegister.Items`)**
  - [ ] Tier 1-4 (Iron Plates, Rotors, Modular Frames, Steel Pipes)
  - [ ] Tier 5-6 (Computers, Heavy Modular Frames, Circuit Boards)
  - [ ] Tier 7-8 (Supercomputers, Cooling Systems, Fused Modular Frames)
  - [ ] Space Elevator Project Parts (Smart Plating to Assembly Director Systems)
- [ ] **Tools & Equipment**
  - [ ] Xeno-Zapper / Xeno-Basher (Melee weapons)
  - [ ] Chainsaw (Foliage clearing, consumes fuel)
  - [ ] Object Scanner
- [ ] **Fluids (`DeferredRegister<FluidType>` & `DeferredRegister<Fluid>`)**
  - [ ] Crude Oil & Heavy Oil Residue
  - [ ] Fuel, Turbofuel, & Liquid Biofuel
  - [ ] Alumina Solution, Sulfuric Acid, & Nitric Acid
  - [ ] Corresponding Buckets & `LiquidBlock` definitions

## Phase 4: Blocks & Machines (⏳ To Do)
- [ ] **AWESOME Sink**
  - [ ] Block & BlockEntity creation
  - [ ] Capability attachments (`IItemHandler`, `IFluidHandler` that instantly void inputs)
  - [ ] Point calculation logic & NBT data storage
- [ ] **AWESOME Shop**
  - [ ] Block & BlockEntity creation
  - [ ] Custom GUI / Menu Screen (`RegisterMenuScreensEvent`)
  - [ ] Network packets to sync coupon balances
- [x] **Resource Nodes** (Done in separate mod)
  - [ ] World-gen deposit blocks (Iron, Copper, Limestone, Caterium, etc.)
  - [x] Purity variants (Impure, Normal, Pure) for drill speed scaling

## Phase 5: Advanced Logistics & Manufacturing (⏳ To Do)
- [ ] **Refinery Mechanics**
  - [ ] Basin setup for Dual-Phase Outputs (Fluid + Item simultaneously)
  - [ ] Byproduct stall mechanics (Machine halts if fluid/item output is backed up)
  - [ ] Overflow logic mapping (Smart Fluid Pipes & Threshold Switches to prioritize processing over sinking)
- [ ] **Machine Equivalents Setup**
  - [ ] Smelter/Foundry -> Bulk Blasting or Heated Basin
  - [ ] Constructor -> Mechanical Press / Deployer
  - [ ] Assembler -> 2-Input Basin or Mechanical Crafters

## Phase 6: Integration & Polish (⏳ To Do)
- [ ] **JEI / EMI / REI Compatibility**
  - [ ] Write a custom plugin to display the true (unclamped) recipe durations in seconds, overriding Create's default cycle display
- [ ] **Mixer Audio Fix**
  - [ ] Write a Mixin for `MechanicalMixerBlockEntity.tickAudio()` to keep the mixing sound looping for multi-minute recipes
- [ ] **Visuals**
  - [ ] Fluid rendering and transparency (`FMLClientSetupEvent`)
  - [ ] Mod Logo implementation in mod menu