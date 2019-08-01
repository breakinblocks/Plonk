package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterials;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

public class BlockPlacedItems extends Block {

    public BlockPlacedItems() {
        super(RegistryMaterials.placed_items);
        this.setHardness(0.5f);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        if (tile != null) {
            for (int slot = 0; slot < tile.getSizeInventory(); slot++) {
                ItemStack stack = tile.getStackInSlot(slot);
                // TODO: Update item nulls
                if (stack != null) {
                    ItemUtils.dropItemWithinBlock(world, x, y, z, stack);
                    tile.setInventorySlotContents(slot, null);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TilePlacedItems();
    }
}
