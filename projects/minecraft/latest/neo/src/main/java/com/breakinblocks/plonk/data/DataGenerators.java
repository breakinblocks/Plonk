package com.breakinblocks.plonk.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataGenerators {
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(Models::new);
        event.createProvider(BlockTags::new);
        event.createProvider(ItemTags::new);
    }
}
