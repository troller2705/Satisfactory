package com.troller2705.satisfactory.network;

import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.blockentities.HubTerminalBlockEntity;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SubmitMilestonePayload(BlockPos pos, long questId) implements CustomPacketPayload {

    public static final Type<SubmitMilestonePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Satisfactory.MODID, "submit_milestone"));

    public static final StreamCodec<FriendlyByteBuf, SubmitMilestonePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeLong(payload.questId());
            },
            buf -> new SubmitMilestonePayload(buf.readBlockPos(), buf.readLong())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 1. Locate the physical Hub Terminal in the world
            if (!(player.level().getBlockEntity(pos()) instanceof HubTerminalBlockEntity hub)) return;

            // 2. Fetch the FTB Quests Data
            ServerQuestFile file = ServerQuestFile.INSTANCE;
            if (file == null) return;

            Quest quest = file.getQuest(questId());
            if (quest == null) return;

            TeamData data = file.getOrCreateTeamData(player);

            // 3. Consume items from the HUB inventory slots
            if (data.canStartTasks(quest)) {
                for (Task task : quest.getTasks()) {
                    if (!data.isCompleted(task)) {

                        // Scan the 9 internal HUB slots
                        for (int i = 0; i < hub.inventory.getSlots(); i++) {
                            ItemStack stack = hub.inventory.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                // FTB will evaluate the item, and physically shrink the ItemStack if it matches!
                                task.submitTask(data, player, stack);

                                // Re-set the slot so the Block Entity knows it changed and syncs to the client
                                hub.inventory.setStackInSlot(i, stack);
                            }
                        }
                    }
                }
            }
        });
    }
}