package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent evt) {
        if (evt.includeServer()) {
            evt.getGenerator().addProvider(new BlockStates(evt.getGenerator(), Plonk.MOD_ID, evt.getExistingFileHelper()));
        }
    }
}
