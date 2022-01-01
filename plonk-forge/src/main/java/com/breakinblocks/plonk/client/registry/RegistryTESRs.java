package com.breakinblocks.plonk.client.registry;

import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RegistryTESRs {

    public static void init() {
        ClientRegistry.bindTileEntityRenderer(RegistryTileEntities.placed_items, TESRPlacedItems::new);
    }
}
