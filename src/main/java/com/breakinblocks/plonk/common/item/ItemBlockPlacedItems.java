package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends ItemBlock {

    public ItemBlockPlacedItems() {
        super(RegistryBlocks.placed_items);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
        ItemStack heldItem = player.getHeldItem();
        //TODO: Update null stacks
        if (heldItem == null || heldItem.stackSize <= 0) return false;
        if (!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata)) return false;

        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);

        tile.setInventorySlotContents(0, heldItem);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

        return true;
    }
}
