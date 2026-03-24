package com.breakinblocks.plonk.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public class RenderUtils {
    public static Matrix4f getModelTransformMatrix(BakedModel model, ItemDisplayContext transformType) {
        PoseStack stack = new PoseStack();
        model.applyTransform(transformType, stack, false);
        return stack.last().pose();
    }
}
