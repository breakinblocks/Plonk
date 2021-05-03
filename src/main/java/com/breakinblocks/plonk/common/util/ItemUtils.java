package com.breakinblocks.plonk.common.util;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class ItemUtils {
    // TODO: Update this to air
    private static final ItemStack REFERENCE = new ItemStack(Blocks.stone);

    /**
     * Drop item within a block of area with 0.1 padding
     *
     * @param world World Object
     * @param x     Block X coordinate
     * @param y     Block Y coordinate
     * @param z     Block Z coordinate
     * @param stack Stack to drop
     * @return The resulting item entity if it was spawned
     * @see BlockChest#breakBlock(net.minecraft.world.World, int, int, int, net.minecraft.block.Block, int)
     */
    @SuppressWarnings("RedundantCast")
    @Nullable
    public static EntityItem dropItemWithinBlock(World world, int x, int y, int z, @Nullable ItemStack stack) {
        // TODO: Update item nulls
        if (stack == null || stack.stackSize <= 0)
            return null;

        Random rand = world.rand;

        float offsetX = rand.nextFloat() * 0.8F + 0.1F;
        float offsetY = rand.nextFloat() * 0.8F + 0.1F;
        float offsetZ = rand.nextFloat() * 0.8F + 0.1F;

        EntityItem entityItem = new EntityItem(world, (double) (x + offsetX), (double) (y + offsetY), (double) (z + offsetZ), stack.copy());
        float maxVariation = 0.05F;
        entityItem.motionX = (double) ((float) rand.nextGaussian() * maxVariation);
        entityItem.motionY = (double) ((float) rand.nextGaussian() * maxVariation + 0.2F);
        entityItem.motionZ = (double) ((float) rand.nextGaussian() * maxVariation);

        if (stack.hasTagCompound()) {
            entityItem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
        }
        return world.spawnEntityInWorld(entityItem) ? entityItem : null;
    }

    /**
     * Drop item on an entity
     *
     * @param entity Target entity
     * @param stack  Stack to drop
     * @return Resulting item entity if it was spawned
     */
    // TODO: Update item nulls
    @Nullable
    public static EntityItem dropItemOnEntity(EntityLivingBase entity, @Nullable ItemStack stack) {
        // TODO: Update item nulls
        if (stack == null || stack.stackSize <= 0)
            return null;
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        EntityItem entityItem = new EntityItem(entity.worldObj, x, y, z, stack.copy());

        if (stack.hasTagCompound()) {
            entityItem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
        }

        entity.worldObj.spawnEntityInWorld(entityItem);

        return entityItem;
    }

    /**
     * Compares two item stacks for equality, ignoring the stack size.
     *
     * @param a First item
     * @param b Second item
     * @return True if the item, damage and nbt are the same
     */
    // TODO: Update item nulls
    public static boolean areStacksEqualIgnoringSize(@Nullable ItemStack a, @Nullable ItemStack b) {
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
    @Nullable //TODO: Update null stacks
    public static ItemStack insertStack(IInventory inv, ItemStack stack) {
        return insertStackAdv(inv, stack).remainder;
    }

    /**
     * Attempts to insert stack into inventory, returning information object about what happened.
     *
     * @param inv   Inventory to insert into
     * @param stack Stack to insert
     * @return The remaining items and slots inserted into
     * @see net.minecraft.tileentity.TileEntityHopper
     */
    public static InsertStackResult insertStackAdv(IInventory inv, @Nullable ItemStack stack) {
        //TODO: Update null stacks
        if (stack == null)
            return new InsertStackResult(null, new int[0]);
        int stackSizeLimit = Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit());
        int size = inv.getSizeInventory();

        ArrayList<Integer> slots = new ArrayList<>();

        for (int slot = 0; slot < size; slot++) {
            if (!inv.isItemValidForSlot(slot, stack)) {
                continue;
            }
            ItemStack current = inv.getStackInSlot(slot);
            if (current == null) {
                current = stack.copy();
                current.stackSize = 0;
            }

            if (current.stackSize < stackSizeLimit && areStacksEqualIgnoringSize(current, stack)) {
                int total = current.stackSize + stack.stackSize;

                current.stackSize = Math.min(total, stackSizeLimit);
                inv.setInventorySlotContents(slot, current);
                slots.add(slot);

                int remaining = total - current.stackSize;
                if (remaining <= 0) {
                    // TODO: Update null stack
                    stack = null;
                    break;
                }

                stack = stack.copy();
                stack.stackSize = remaining;
            }
        }
        int[] slotsArray = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            slotsArray[i] = slots.get(i);
        }
        return new InsertStackResult(stack, slotsArray);
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
        @Nullable //TODO: Update null stacks
        public final ItemStack remainder;
        /**
         * Slots in the target inventory that had items inserted into them
         */
        public final int[] slots;

        //TODO: Update null stacks
        public InsertStackResult(@Nullable ItemStack remainder, int[] slots) {
            this.remainder = remainder;
            this.slots = slots;
        }
    }
}
