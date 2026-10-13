package com.troller2705.satisfactory.menu;

import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.blockentities.HubTerminalBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;

public class HubTerminalMenu extends AbstractContainerMenu {
    public final HubTerminalBlockEntity blockEntity;

    // The master switch that tells slots whether to render and accept clicks
    public boolean slotsActive = false;

    public HubTerminalMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf data) {
        this(containerId, playerInv, playerInv.player.level().getBlockEntity(data.readBlockPos()));
    }

    public HubTerminalMenu(int containerId, Inventory playerInv, BlockEntity entity) {
        super(Satisfactory.HUB_TERMINAL_MENU.get(), containerId);
        this.blockEntity = (HubTerminalBlockEntity) entity;

        // 1. HUB Input Slots
        // 1. HUB Input Slots
        for (int i = 0; i < 9; i++) {
            final int slotIndex = i;

            this.addSlot(new SlotItemHandler(this.blockEntity.inventory, i, 44 + (i * 18), 120) {

                @Override
                public boolean isActive() {
                    if (!slotsActive) return false;

                    Quest quest = getActiveQuest();
                    return quest != null && slotIndex < quest.getTasks().size();
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    Quest quest = getActiveQuest();
                    if (quest == null || slotIndex >= quest.getTasks().size()) return false;

                    // Convert the Collection to a List so we can safely use .get(slotIndex)
                    java.util.List<Task> tasks = new java.util.ArrayList<>(quest.getTasks());
                    Task task = tasks.get(slotIndex);

                    if (task instanceof ItemTask itemTask) {
                        return itemTask.test(stack);
                    }
                    return false;
                }
            });
        }

        // 2. Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 44 + col * 18, 145 + row * 18) {
                    @Override
                    public boolean isActive() {
                        return slotsActive;
                    }
                });
            }
        }

        // 3. Player Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 44 + col * 18, 203) {
                @Override
                public boolean isActive() {
                    return slotsActive;
                }
            });
        }
    }

    // The mandatory, messy boilerplate for Shift-Clicking items
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index < 9) { // If shift-clicking FROM the Hub TO the Player
                if (!this.moveItemStackTo(slotStack, 9, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(slotStack, 0, 9, false)) { // FROM Player TO Hub
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Ensures the player hasn't walked too far away from the block
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, Satisfactory.HUB_TERMINAL_BLOCK.get());
    }

    // Safely fetches the active quest depending on whether this is running on the Server or Client
    private Quest getActiveQuest() {
        long questId = this.blockEntity.activeQuestId;
        if (questId == 0L) return null;

        if (this.blockEntity.getLevel().isClientSide()) {
            // Safe Client Call
            return com.troller2705.satisfactory.integration.FTBQuestsBridge.getQuestById(questId);
        } else {
            // Safe Server Call
            dev.ftb.mods.ftbquests.quest.ServerQuestFile file = dev.ftb.mods.ftbquests.quest.ServerQuestFile.INSTANCE;
            return file != null ? file.getQuest(questId) : null;
        }
    }
}