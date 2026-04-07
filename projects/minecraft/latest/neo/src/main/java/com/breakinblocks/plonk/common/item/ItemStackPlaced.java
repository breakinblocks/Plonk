package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;

/**
 * @see ItemStackWithSlot
 */
public record ItemStackPlaced(int slot, int renderType, int itemRotation, ItemStack stack) {
    public static final Codec<ItemStackPlaced> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            ExtraCodecs.UNSIGNED_BYTE.fieldOf(TilePlacedItems.TAG_SLOT).orElse(0).forGetter(ItemStackPlaced::slot),
                            Codec.INT.fieldOf(TilePlacedItems.TAG_RENDER_TYPE).orElse(0).forGetter(ItemStackPlaced::renderType),
                            Codec.INT.fieldOf(TilePlacedItems.TAG_ITEM_ROTATION).orElse(0).forGetter(ItemStackPlaced::itemRotation),
                            ItemStack.MAP_CODEC.forGetter(ItemStackPlaced::stack)
                    )
                    .apply(i, ItemStackPlaced::new)
    );

    public boolean isValidInContainer(int containerSize) {
        return this.slot >= 0 && this.slot < containerSize;
    }
}
