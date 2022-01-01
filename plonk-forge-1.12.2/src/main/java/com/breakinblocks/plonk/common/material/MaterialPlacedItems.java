package com.breakinblocks.plonk.common.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialPlacedItems extends Material {
    public MaterialPlacedItems() {
        super(MapColor.AIR);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }
}
