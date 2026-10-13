package com.troller2705.satisfactory.mixin;

import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 'remap = false' is required here because getSlotLimit is a NeoForge method, not a vanilla Mojang method.
@Mixin(value = ItemStackHandler.class, remap = false)
public class CreateBasinStackSizeMixin {

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void adjustCreateSlotLimits(int slot, CallbackInfoReturnable<Integer> cir) {

        // We only want to override the 64-limit for Create's inventories (Basins, Depots, Crafters).
        // If we do this for every mod, it might break UIs that rely on max 64.
        if (this.getClass().getName().startsWith("com.simibubi.create")) {

            // By returning MAX_VALUE, we tell the Basin the slot has no hard limit.
            // NeoForge will then safely clamp the stack size to whatever stacksizetweaks
            // set the ItemStack's getMaxStackSize() to!
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }
}