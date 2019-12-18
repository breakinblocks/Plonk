package com.breakinblocks.plonk.common;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.registry.RegistryPackets;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Plonk.MOD_ID)
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        RegistryTileEntities.init();
        RegistryPackets.init();
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryBlocks.init(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryItems.init(event);
    }
}
