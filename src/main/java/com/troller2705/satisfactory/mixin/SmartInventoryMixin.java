package com.troller2705.satisfactory.mixin;

import com.simibubi.create.foundation.item.SmartInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SmartInventory.class, remap = false)
public class SmartInventoryMixin {

    // We grab Create's internal stackSize variable
    @Shadow protected int stackSize;

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void bypassSmartInvLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        // If Create intended this to be a standard stack (64), we uncap it to MAX_VALUE.
        // This allows stacksizetweaks to take over while leaving 1-item limit machines alone.
        if (this.stackSize >= 64) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }
}