package com.troller2705.satisfactory.network;

import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.blockentities.HubTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetActiveMilestonePayload(BlockPos pos, long questId) implements CustomPacketPayload {

    public static final Type<SetActiveMilestonePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "set_active_milestone"));

    public static final StreamCodec<FriendlyByteBuf, SetActiveMilestonePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeLong(payload.questId());
            },
            buf -> new SetActiveMilestonePayload(buf.readBlockPos(), buf.readLong())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(pos()) instanceof HubTerminalBlockEntity hub) {
                hub.activeQuestId = questId();
                hub.setChanged();

                // Sync to all players looking at it
                hub.getLevel().sendBlockUpdated(pos(), hub.getBlockState(), hub.getBlockState(), 3);
            }
        });
    }
}