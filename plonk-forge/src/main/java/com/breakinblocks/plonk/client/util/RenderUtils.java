package com.breakinblocks.plonk.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;

public class RenderUtils {
    public static Matrix4f getModelTransformMatrix(BakedModel model, ItemTransforms.TransformType type) {
        PoseStack stack = new PoseStack();
        model.handlePerspective(type, stack);
        return stack.last().pose();
    }
}
