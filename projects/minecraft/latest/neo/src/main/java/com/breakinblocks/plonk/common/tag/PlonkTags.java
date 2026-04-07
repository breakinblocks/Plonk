package com.breakinblocks.plonk.common.tag;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PlonkTags {
    public static void init() {
        Items.init();
    }

    public static class Items {

        public static final TagKey<Item> UNPLACEABLE = tag("unplaceable");

        public static void init() {
        }

        @SuppressWarnings("SameParameterValue")
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(Plonk.MOD_ID, name));
        }
    }
}
