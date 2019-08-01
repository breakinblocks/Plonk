package com.breakinblocks.plonk;

import com.breakinblocks.plonk.common.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Plonk.MOD_ID, name = Plonk.NAME, version = Plonk.VERSION)
public class Plonk {
    public static final String MOD_ID = "plonk";
    public static final String NAME = "Plonk";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOG = LogManager.getLogger(MOD_ID);
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Plonk.MOD_ID);
    @SidedProxy(clientSide = "com.breakinblocks.plonk.client.ClientProxy", serverSide = "com.breakinblocks.plonk.common.CommonProxy")
    public static CommonProxy PROXY;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);
    }
}
