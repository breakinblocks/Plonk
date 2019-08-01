package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.common.CommonProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    public final ClientEvents CLIENT_EVENTS = new ClientEvents();

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        CLIENT_EVENTS.registerKeyBindings();
    }
}
