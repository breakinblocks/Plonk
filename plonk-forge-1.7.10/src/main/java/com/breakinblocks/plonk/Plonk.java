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

@Mod(
        modid = PlonkConstants.MOD_ID,
        name = PlonkConstants.MOD_NAME,
        version = PlonkConstants.MOD_VERSION,
        dependencies = PlonkConstants.MOD_DEPENDENCIES,
        acceptedMinecraftVersions = PlonkConstants.MOD_ACCEPTED_MINECRAFT_VERSIONS,
        guiFactory = PlonkConstants.MOD_GUI_FACTORY
)
public class Plonk {
    public static final Logger LOG = LogManager.getLogger(PlonkConstants.MOD_ID);
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(PlonkConstants.MOD_ID);
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
