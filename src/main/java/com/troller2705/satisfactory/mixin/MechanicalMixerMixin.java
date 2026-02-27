package com.troller2705.satisfactory.mixin;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MechanicalMixerBlockEntity.class, remap = false)
public abstract class MechanicalMixerMixin extends BasinOperatingBlockEntity {

    public MechanicalMixerMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Redirects the Mth.clamp call specifically inside the Mixer's tick method.
     * This forces the max value to 10,000 instead of 512.
     */
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I")
    )
    private int satisfactory$increaseMixerCap(int value, int min, int max) {
        // If the original code is trying to clamp to 512, we increase it to 10,000
        if (max == 512) {
            return Mth.clamp(value, min, 16384);
        }
        // Otherwise, behave normally for other clamps (like runningTicks)
        return Mth.clamp(value, min, max);
    }
}