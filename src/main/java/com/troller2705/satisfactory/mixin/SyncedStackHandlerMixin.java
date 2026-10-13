package com.troller2705.satisfactory.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Using 'targets' is required for nested/inner classes in Mixins
@Mixin(targets = "com.simibubi.create.foundation.item.SmartInventory$SyncedStackHandler", remap = false)
public class SyncedStackHandlerMixin {

    @Shadow private int stackSize;

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void bypassSyncedSlotLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        if (this.stackSize >= 64) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }
}