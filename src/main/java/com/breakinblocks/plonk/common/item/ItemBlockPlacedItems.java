package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends ItemBlock {

    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem();
        //TODO: Update null stacks
        if (heldItem == null || heldItem.stackSize <= 0) return false;
        Block block = world.getBlock(x, y, z);
        if (block == RegistryBlocks.placed_items) {
            TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
            // TODO: If insert item success then return, else pass
            ItemStack remainder = ItemUtils.insertStack(tile, heldItem);
            if (remainder != heldItem) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, remainder);
                return true;
            }
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

        // Freshly placed
        ItemStack remainder = ItemUtils.insertStack(tile, heldItem);
        if (remainder != heldItem) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, remainder);
        }

        return true;
    }
}
