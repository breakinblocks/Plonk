package com.breakinblocks.plonk.common.tag;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class PlonkTags {
    public static void init() {
        Items.init();
    }

    public static class Items {

        public static final Tags.IOptionalNamedTag<Item> UNPLACEABLE = tag("unplaceable");

        public static void init() {
        }

        @SuppressWarnings("SameParameterValue")
        private static Tags.IOptionalNamedTag<Item> tag(String name) {
            return ItemTags.createOptional(new ResourceLocation(Plonk.MOD_ID, name));
        }
    }
}
