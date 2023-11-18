package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends ItemBlock {
    private static final String TAG_HELD = "Held";
    private static final String TAG_RENDER_TYPE = TilePlacedItems.TAG_RENDER_TYPE;

    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    public void setHeldStack(ItemStack stack, ItemStack held, int renderType) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        NBTTagCompound tagCompoundHeld = tagCompound.getCompoundTag(TAG_HELD);
        held.writeToNBT(tagCompoundHeld);
        tagCompound.setTag(TAG_HELD, tagCompoundHeld);
        tagCompound.setInteger(TAG_RENDER_TYPE, renderType);
        stack.setTagCompound(tagCompound);
    }

    public ItemStack getHeldStack(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null)
            return ItemStack.EMPTY;
        return new ItemStack(tagCompound.getCompoundTag(TAG_HELD));
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
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack placerStack = player.getHeldItem(hand);
        if (placerStack.isEmpty())
            return EnumActionResult.FAIL;
        ItemStack heldStack = getHeldStack(placerStack);
        if (heldStack.isEmpty() || !PlonkConfig.canPlace(heldStack))
            return EnumActionResult.FAIL;

        TilePlacedItems tile = null;
        if (world.getBlockState(pos).getBlock() == RegistryBlocks.placed_items) {
            tile = (TilePlacedItems) world.getTileEntity(pos);
        } else {
            BlockPos pos2 = pos.offset(facing);
            if (world.getBlockState(pos2).getBlock() == RegistryBlocks.placed_items) {
                tile = (TilePlacedItems) world.getTileEntity(pos2);
            }
        }

        if (tile != null && tryInsertStack(placerStack, tile)) {
            IBlockState state = world.getBlockState(pos);
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            return EnumActionResult.SUCCESS;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    @Override
    public boolean placeBlockAt(ItemStack placerStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        ItemStack heldStack = getHeldStack(placerStack);
        if (heldStack.isEmpty())
            return false;
        if (!super.placeBlockAt(placerStack, player, world, pos, side, hitX, hitY, hitZ, newState))
            return false;

        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(pos);
        if (tile == null)
            return false;

        // Insert into freshly placed tile
        return tryInsertStack(placerStack, tile);
    }
}
