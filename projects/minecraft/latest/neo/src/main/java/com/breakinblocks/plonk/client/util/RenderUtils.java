package com.breakinblocks.plonk.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

public class RenderUtils {
    public static Matrix4f getModelTransformMatrix(ItemStack stack, ItemDisplayContext transformType, @Nullable Level level, @Nullable ItemOwner itemOwner, int seed) {
        TrackingItemStackRenderState itemStackRenderState = new TrackingItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemStackRenderState, stack, transformType, level, itemOwner, seed + transformType.ordinal());
        ItemStackRenderState.LayerRenderState layerRenderState = itemStackRenderState.firstLayer();
        PoseStack.Pose pose = new PoseStack.Pose();
        layerRenderState.applyTransform(pose);
        return pose.pose();
    }
}
