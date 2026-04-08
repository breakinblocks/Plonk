package com.breakinblocks.plonk.client.render.tile.state;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @see ItemFrameRenderState
 * @see CampfireRenderState
 */
@OnlyIn(Dist.CLIENT)
public class PlacedItemsRenderState extends BlockEntityRenderState {
    public Direction direction = Direction.NORTH;
    public double tileRotationAngle = 0.0d;
    public ItemStackRenderState[] items = new ItemStackRenderState[0];
    public TilePlacedItems.ItemMeta[] itemMetas = new TilePlacedItems.ItemMeta[0];
    public int numItems = 0;
}
