package com.breakinblocks.plonk.common.tag;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class PlonkTags {
    public static void init() {
        Items.init();
    }

    public static class Items {

        public static final Tag<Item> UNPLACEABLE = tag("unplaceable");

        public static void init() {
        }

        @SuppressWarnings("SameParameterValue")
        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(new ResourceLocation(Plonk.MOD_ID, name));
        }
    }
}
