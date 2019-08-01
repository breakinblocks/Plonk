package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterial;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockPlacedItems extends Block {
    public BlockPlacedItems() {
        super(RegistryMaterial.placed_items);
        this.setCreativeTab(CreativeTabs.tabMisc); // TODO: Hide or remove from creative menu
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
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
        public TileEntity createTileEntity(World world, int metadata) {
        return new TilePlacedItems();
    }
}
