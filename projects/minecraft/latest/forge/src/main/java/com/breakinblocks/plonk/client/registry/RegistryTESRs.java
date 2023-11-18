package com.breakinblocks.plonk.client.registry;

import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class RegistryTESRs {

    public static void init() {
        BlockEntityRenderers.register(RegistryTileEntities.placed_items, TESRPlacedItems::new);
    }
}
