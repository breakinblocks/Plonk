package com.breakinblocks.plonk.client.registry;

import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import cpw.mods.fml.client.registry.ClientRegistry;

public class RegistryTESRs {

    public static void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TilePlacedItems.class, new TESRPlacedItems());
    }
}
