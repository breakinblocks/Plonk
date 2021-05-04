package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityHopper;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ItemUtils {
    private static final ItemStack REFERENCE = new ItemStack(Items.AIR);

    /**
     * Drop item on an entity
     *
     * @param entity Target entity
     * @param stack  Stack to drop
     * @return Resulting item entity if it was spawned
     */
    @Nullable
    public static EntityItem dropItemOnEntity(EntityLivingBase entity, ItemStack stack) {
        if (stack.isEmpty())
            return null;
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        EntityItem entityItem = new EntityItem(entity.world, x, y, z, stack.copy());

        return entity.world.spawnEntity(entityItem) ? entityItem : null;
    }

    /**
     * Compares two item stacks for equality, ignoring the stack size.
     *
     * @param a First item
     * @param b Second item
     * @return True if the item, damage and nbt are the same
     */
    public static boolean areStacksEqualIgnoringSize(ItemStack a, ItemStack b) {
        if (a.isEmpty()) {
            return b.isEmpty();
        } else {
            if (b.isEmpty()) return false;
            if (!a.isItemEqual(b)) return false; // checks item and damage but not nbt
            return ItemStack.areItemStackTagsEqual(a, b);
        }
    }

    /**
     * Attempts to insert stack into inventory, returning the remaining items.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return Remaining items if partially inserted, null if fully inserted, original stack if no insertion.
     */
    public static ItemStack insertStack(IInventory inv, ItemStack stack) {
        return insertStackAdv(inv, stack).remainder;
    }

    /**
     * Attempts to insert stack into inventory, returning information object about what happened.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return The remaining items and slots inserted into
     * @see TileEntityHopper
     */
    public static InsertStackResult insertStackAdv(IInventory inv, ItemStack stack) {
        if (stack.isEmpty())
            return new InsertStackResult(stack, new int[0]);
        int stackSizeLimit = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        int size = inv.getSizeInventory();

        ArrayList<Integer> slots = new ArrayList<>();

        ItemStack remainder = stack;

        for (int slot = 0; slot < size && !remainder.isEmpty(); slot++) {
            if (!inv.isItemValidForSlot(slot, stack)) {
                continue;
            }
            ItemStack current = inv.getStackInSlot(slot);
            if (!current.isEmpty() && !areStacksEqualIgnoringSize(current, remainder)) continue;
            int toTransfer = Math.min(current.getCount() + remainder.getCount(), stackSizeLimit) - current.getCount();
            if (toTransfer <= 0) continue;
            if (current.isEmpty()) {
                current = remainder.copy();
                current.setCount(toTransfer);
            } else {
                current.setCount(current.getCount() + toTransfer);
            }
            // Don't modify input stack
            if (remainder == stack) remainder = stack.copy();
            remainder.setCount(remainder.getCount() - toTransfer);
            inv.setInventorySlotContents(slot, current);
            slots.add(slot);
        }
        int[] slotsArray = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            slotsArray[i] = slots.get(i);
        }
        return new InsertStackResult(remainder, slotsArray);
    }

    /**
     * Get the maximum stack size of REFERENCE.
     * This will usually be 64 but mods like StackUp! may change this.
     *
     * @return maximum stack size of REFERENCE
     */
    public static int getMaxStackSize() {
        return REFERENCE.getMaxStackSize();
    }

    public static class InsertStackResult {
        /**
         * Remaining items if partially inserted, null if fully inserted, original stack if no insertion.
         */
        public final ItemStack remainder;
        /**
         * Slots in the target inventory that had items inserted into them
         */
        public final int[] slots;

        public InsertStackResult(ItemStack remainder, int[] slots) {
            this.remainder = remainder;
            this.slots = slots;
        }
    }
}
