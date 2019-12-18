package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {
    /**
     * Gets the position of the entity's eyes
     *
     * @param entity             target
     * @param renderPartialTicks fractional tick
     * @return position of eye
     * @deprecated
     * @see Entity#getPositionEyes(float)
     */
    public static Vec3d getEyePosition(EntityLivingBase entity, float renderPartialTicks) {
        return entity.getPositionEyes(renderPartialTicks);
    }

}
