package com.breakinblocks.plonk.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.util.math.vector.Matrix4f;


public class RenderUtils {
    public static Matrix4f getModelTransformMatrix(IBakedModel model, ItemCameraTransforms.TransformType type) {
        MatrixStack stack = new MatrixStack();
        model.handlePerspective(type, stack);
        return stack.getLast().getMatrix();
    }
}
