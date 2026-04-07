package com.breakinblocks.plonk.client.render.tile.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlacedItemsRenderState extends BlockEntityRenderState {
    public Direction direction = Direction.NORTH;
    public int rotation = 0;
    public final BlockModelRenderState blockModel = new BlockModelRenderState();
    public final ItemStackRenderState item0 = new ItemStackRenderState();
    public final ItemStackRenderState item1 = new ItemStackRenderState();
    public final ItemStackRenderState item2 = new ItemStackRenderState();
    public final ItemStackRenderState item3 = new ItemStackRenderState();
}
