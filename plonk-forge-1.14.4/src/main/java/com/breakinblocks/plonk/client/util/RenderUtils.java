package com.breakinblocks.plonk.client.util;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;

import javax.vecmath.Matrix4f;


public class RenderUtils {
    public static Matrix4f getModelTransformMatrix(IBakedModel model, ItemCameraTransforms.TransformType type) {
        Matrix4f mat = model.handlePerspective(type).getRight();
        if (mat == null) {
            mat = new javax.vecmath.Matrix4f();
            mat.setIdentity();
        }
        return mat;
    }
}
