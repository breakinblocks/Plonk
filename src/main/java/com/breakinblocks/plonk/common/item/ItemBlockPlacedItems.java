package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends ItemBlock {
    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    /**
     * Try to insert held item into the tile
     *
     * @param stack  ItemBlockPlacedItems reference stack, which should contain IsBlock information
     * @param tile   TilePlacedItems to insert into
     * @param player That is currently holding the item to be inserted
     */
    protected boolean tryInsertStack(ItemStack stack, TilePlacedItems tile, EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem();
        boolean isBlock = stack.getTagCompound().getBoolean(TilePlacedItems.TAG_IS_BLOCK);
        ItemStack remainder = tile.insertStack(heldItem, isBlock);
        // If inserted some items then return true after updating the held item
        if (remainder != heldItem) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, remainder);
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem();
        //TODO: Update null stacks
        if (heldItem == null || heldItem.stackSize <= 0) return false;

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

        if (tile != null) {
            // TODO: Remove duplicated code
            return tryInsertStack(stack, tile, player);
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        ItemStack heldItem = player.getHeldItem();
        //TODO: Update null stacks
        if (heldItem == null || heldItem.stackSize <= 0) return false;
        if (!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata)) return false;

        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);

        // Insert into freshly placed tile
        return tryInsertStack(stack, tile, player);
    }
}
