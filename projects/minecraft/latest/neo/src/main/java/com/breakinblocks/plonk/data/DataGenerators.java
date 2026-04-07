package com.breakinblocks.plonk.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(Models::new);
        event.createProvider(BlockTags::new);
        event.createProvider(ItemTags::new);
    }
}
