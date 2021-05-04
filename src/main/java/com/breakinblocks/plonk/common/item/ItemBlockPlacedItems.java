package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemBlockPlacedItems extends ItemBlock {
    private static final String TAG_HELD = "Held";
    private static final String TAG_RENDER_TYPE = TilePlacedItems.TAG_RENDER_TYPE;

    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    // TODO: Update item nulls
    public void setHeldStack(ItemStack stack, @Nullable ItemStack held, int renderType) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        // TODO: Remove null case
        if (held == null) {
            tagCompound.removeTag(TAG_HELD);
        } else {
            NBTTagCompound tagCompoundHeld = tagCompound.getCompoundTag(TAG_HELD);
            held.writeToNBT(tagCompoundHeld);
            tagCompound.setTag(TAG_HELD, tagCompoundHeld);
        }
        tagCompound.setInteger(TAG_RENDER_TYPE, renderType);
        stack.setTagCompound(tagCompound);
    }

    @Nullable
    public ItemStack getHeldStack(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();

        // TODO: Update null case and stack
        if (tagCompound == null || !tagCompound.hasKey(TAG_HELD))
            return null;
        return ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag(TAG_HELD));
    }

    public int getHeldRenderType(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null)
            return 0;
        return stack.getTagCompound().getInteger(TAG_RENDER_TYPE);
    }

    /**
     * Try to insert held item into the tile
     *
     * @param placerStack ItemBlockPlacedItems reference stack, which should contain renderType information
     * @param tile        TilePlacedItems to insert into
     * @return true if stack was at least partially successfully inserted
     */
    protected boolean tryInsertStack(ItemStack placerStack, TilePlacedItems tile) {
        ItemStack heldItem = getHeldStack(placerStack);
        int renderType = getHeldRenderType(placerStack);
        ItemStack remainder = tile.insertStack(heldItem, renderType);
        tile.markDirty();
        tile.clean();
        if (remainder != heldItem) {
            setHeldStack(placerStack, remainder, renderType);
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemUse(@Nullable ItemStack placerStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        //TODO: Update null stacks
        if (placerStack == null)
            return false;
        ItemStack heldStack = getHeldStack(placerStack);
        if (heldStack == null || heldStack.stackSize <= 0 || PlonkConfig.unplaceableItems.contains(ItemUtils.getIdentifier(heldStack)))
            return false;

        TilePlacedItems tile = null;
        if (world.getBlock(x, y, z) == RegistryBlocks.placed_items) {
            tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        } else {
            int x2 = x + Facing.offsetsXForSide[side];
            int y2 = y + Facing.offsetsYForSide[side];
            int z2 = z + Facing.offsetsZForSide[side];
            if (world.getBlock(x2, y2, z2) == RegistryBlocks.placed_items) {
                tile = (TilePlacedItems) world.getTileEntity(x2, y2, z2);
            }
        }

        if (tile != null && tryInsertStack(placerStack, tile)) {
            world.playSoundEffect(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
            return true;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.onItemUse(placerStack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack placerStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        //TODO: Update null stacks
        ItemStack heldStack = getHeldStack(placerStack);
        if (heldStack == null || heldStack.stackSize <= 0)
            return false;
        if (!super.placeBlockAt(placerStack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
            return false;

        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        if (tile == null)
            return false;

        // Insert into freshly placed tile
        if (tryInsertStack(placerStack, tile)) {
            world.playSoundEffect((float) tile.xCoord + 0.5F, (float) tile.yCoord + 0.5F, (float) tile.zCoord + 0.5F, this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
            return true;
        }

        return false;
    }
}
