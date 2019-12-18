package com.breakinblocks.plonk.common.item;

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

import javax.annotation.Nonnull;

public class ItemBlockPlacedItems extends ItemBlock {
    private static final String TAG_HELD = "held";

    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    public void setHeldStack(ItemStack stack, ItemStack held) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if(tagCompound == null)
            tagCompound = new NBTTagCompound();
        held.writeToNBT(tagCompound.getCompoundTag(TAG_HELD));
        stack.setTagCompound(tagCompound);
    }

    public ItemStack getHeldStack(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if(tagCompound == null)
            return ItemStack.EMPTY;
        return new ItemStack(tagCompound.getCompoundTag(TAG_HELD));
    }

    /**
     * Try to insert held item into the tile
     *
     * @param stack  ItemBlockPlacedItems reference stack, which should contain IsBlock information
     * @param tile   TilePlacedItems to insert into
     * @param player That is currently holding the item to be inserted
     * @return true if stack was at least partially successfully inserted
     */
    protected boolean tryInsertStack(ItemStack stack, TilePlacedItems tile, EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        boolean isBlock = stack.getTagCompound().getBoolean(TilePlacedItems.TAG_IS_BLOCK);
        ItemStack remainder = tile.insertStack(heldItem, isBlock);
        tile.markDirty();
        tile.clean();
        if (remainder != heldItem) {
            if (!player.world.isRemote) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, remainder);
            }
            return true;
        }
        return false;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack placerStack = player.getHeldItem(EnumHand.MAIN_HAND);
        ItemStack heldItem = getHeldStack(placerStack);
        if (heldItem.isEmpty()) return EnumActionResult.FAIL;

        TilePlacedItems tile = null;
        if (world.getBlockState(pos).getBlock() == RegistryBlocks.placed_items) {
            tile = (TilePlacedItems) world.getTileEntity(pos);
        } else {
            BlockPos pos2 = pos.offset(facing);
            if (world.getBlockState(pos2).getBlock() == RegistryBlocks.placed_items) {
                tile = (TilePlacedItems) world.getTileEntity(pos2);
            }
        }

        if (tile != null && tryInsertStack(heldItem, tile, player)) {
            setHeldStack(placerStack, heldItem);
            // TODO: Sound
            // world.playSoundEffect(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
            return EnumActionResult.SUCCESS;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.PASS ? EnumActionResult.PASS : EnumActionResult.FAIL;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        ItemStack heldItem = getHeldStack(stack);
        if (heldItem.isEmpty()) return false;
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) return false;

        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(pos);
        if (tile == null)
            return false;

        // Insert into freshly placed tile
        if (tryInsertStack(stack, tile, player)) {
            setHeldStack(stack, heldItem);
            // TODO: Sound
            // world.playSoundEffect((float) tile.xCoord + 0.5F, (float) tile.yCoord + 0.5F, (float) tile.zCoord + 0.5F, this.field_150939_a.stepSound.func_150496_b(), (this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, this.field_150939_a.stepSound.getPitch() * 0.8F);
            return true;
        }

        return false;
    }
}
