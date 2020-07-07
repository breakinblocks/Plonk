package com.breakinblocks.plonk.common.registry;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class RegistryMaterials {
    public static final Material placed_items = (new Material.Builder(MaterialColor.AIR)).notSolid().notOpaque().doesNotBlockMovement().build();
}
