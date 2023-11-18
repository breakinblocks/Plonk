package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.client.command.CommandClientPlonk;
import com.breakinblocks.plonk.client.registry.RegistryTESRs;
import com.breakinblocks.plonk.common.CommonProxy;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    public final ClientEvents CLIENT_EVENTS = new ClientEvents();

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        RegistryTESRs.init();
        CLIENT_EVENTS.registerKeyBindings();
        ClientCommandHandler.instance.registerCommand(new CommandClientPlonk());
    }
}
