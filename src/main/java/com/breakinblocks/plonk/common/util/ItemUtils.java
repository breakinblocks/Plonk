package com.breakinblocks.plonk.common.util;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

public class ItemUtils {
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
    public static EntityItem dropItemWithinBlock(World world, int x, int y, int z, ItemStack stack) {
        // TODO: Update item nulls
        if (stack == null || stack.stackSize <= 0) return null;

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
        world.spawnEntityInWorld(entityItem);
        return entityItem;
    }
}
