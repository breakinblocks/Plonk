package com.breakinblocks.plonk.common.compat.jade;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PlonkJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.blockOperations().pick(BuiltInRegistries.BLOCK.getResourceKey(RegistryBlocks.placed_items).orElseThrow());
    }
}
