package com.breakinblocks.plonk.common;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        RegistryBlocks.init();
        RegistryTileEntities.init();
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
