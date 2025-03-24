package com.breakinblocks.plonk.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static ItemEntity dropItemOnEntity(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty())
            return null;
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        ItemEntity entityItem = new ItemEntity(entity.level(), x, y, z, stack.copy());

        return entity.level().addFreshEntity(entityItem) ? entityItem : null;
    }

    /**
     * Attempts to insert stack into inventory, returning the remaining items.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return Remaining items if partially inserted, null if fully inserted, original stack if no insertion.
     */
    public static ItemStack insertStack(Container inv, ItemStack stack) {
        return insertStackAdv(inv, stack).remainder;
    }

    /**
     * Attempts to insert stack into inventory, returning information object about what happened.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return The remaining items and slots inserted into
     * @see HopperBlockEntity
     */
    public static InsertStackResult insertStackAdv(Container inv, ItemStack stack) {
        if (stack.isEmpty())
            return new InsertStackResult(stack, new int[0]);
        int stackSizeLimit = Math.min(stack.getMaxStackSize(), inv.getMaxStackSize());
        int size = inv.getContainerSize();

        ArrayList<Integer> slots = new ArrayList<>();

        ItemStack remainder = stack;

        for (int slot = 0; slot < size && !remainder.isEmpty(); slot++) {
            if (!inv.canPlaceItem(slot, stack)) {
                continue;
            }
            ItemStack current = inv.getItem(slot);
            if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, remainder)) continue;
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
            inv.setItem(slot, current);
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

    /**
     * Gets the identifier for the given stack.
     *
     * @param stack target stack
     * @return Item's Identifier
     */
    @Nullable
    public static ResourceLocation getIdentifier(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
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
