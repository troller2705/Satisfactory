package com.troller2705.satisfactory.blockentities;

import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.menu.HubTerminalMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class HubTerminalBlockEntity extends BlockEntity implements MenuProvider
{

    // 1. The currently locked-in FTB Quest ID (0 means none selected)
    public long activeQuestId = 0L;

    // 2. The physical inventory of the HUB
    public final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            // Tell the server to sync the new items to the client UI
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public HubTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(Satisfactory.HUB_TERMINAL_BE.get(), pos, state);
    }

    // --- SAVE TO HARD DRIVE ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("ActiveQuest", this.activeQuestId);
        tag.put("Inventory", this.inventory.serializeNBT(registries));
    }

    // --- LOAD FROM HARD DRIVE ---
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.activeQuestId = tag.getLong("ActiveQuest");
        if (tag.contains("Inventory")) {
            this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    // --- CLIENT SYNCING ---
    // These methods ensure that when the player opens the UI, it knows what is inside
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        if (pkt.getTag() != null) {
            loadAdditional(pkt.getTag(), registries);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.satisfactory.hub_terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new HubTerminalMenu(containerId, playerInventory, this);
    }
}