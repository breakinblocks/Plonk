package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class ItemUtils {
    /**
     * Drop item on an entity
     *
     * @param entity Target entity
     * @param stack  Stack to drop
     * @return Resulting item entity if it was spawned
     */
    public static EntityItem dropItemOnEntity(EntityLivingBase entity, ItemStack stack) {
        if (stack.isEmpty()) return null;
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        EntityItem entityItem = new EntityItem(entity.world, x, y, z, stack.copy());

        entity.world.spawnEntity(entityItem);

        return entityItem;
    }

    /**
     * Compares two item stacks for equality, ignoring the stack size.
     *
     * @param a First item
     * @param b Second item
     * @return True if the item, damage and nbt are the same
     */
    public static boolean areStacksEqualIgnoringSize(ItemStack a, ItemStack b) {
        if (a == null) {
            return b == null;
        } else {
            if (b == null) return false;
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

    /**
     * Attempts to insert stack into inventory, returning information object about what happened.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return
     */
    public static InsertStackResult insertStackAdv(IInventory inv, ItemStack stack) {
        //TODO: Update null stacks
        if (stack.isEmpty()) return null;
        int stackSizeLimit = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        int size = inv.getSizeInventory();

        ArrayList<Integer> slots = new ArrayList<>();

        for (int slot = 0; slot < size; slot++) {
            ItemStack current = inv.getStackInSlot(slot);
            if (current.isEmpty()) {
                current = stack.copy();
                current.setCount(0);
            }

            if (current.getCount() < stackSizeLimit && areStacksEqualIgnoringSize(current, stack)) {
                int total = current.getCount() + stack.getCount();

                current.setCount(Math.min(total, stackSizeLimit));
                inv.setInventorySlotContents(slot, current);
                slots.add(slot);

                int remaining = total - current.getCount();
                if (remaining <= 0) {
                    stack = ItemStack.EMPTY;
                    break;
                }

                stack = stack.copy();
                stack.setCount(remaining);
            }
        }
        int[] slotsArray = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            slotsArray[i] = slots.get(i);
        }
        return new InsertStackResult(stack, slotsArray);
    }
}
