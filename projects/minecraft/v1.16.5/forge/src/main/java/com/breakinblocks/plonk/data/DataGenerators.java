package com.breakinblocks.plonk.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator dataGenerator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        if (event.includeServer()) {
            dataGenerator.addProvider(new BlockStates(dataGenerator, existingFileHelper));
            BlockTags blockTagsProvider = new BlockTags(dataGenerator, existingFileHelper);
            dataGenerator.addProvider(blockTagsProvider);
            dataGenerator.addProvider(new ItemTags(dataGenerator, blockTagsProvider, existingFileHelper));
        }
    }
}
