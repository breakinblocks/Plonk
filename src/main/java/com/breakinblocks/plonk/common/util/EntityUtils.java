package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

public class EntityUtils {
    /**
     * Gets the position of the entity's eyes
     *
     * @param entity             target
     * @param renderPartialTicks fractional tick
     * @return position of eye
     * @see EntityLivingBase#getPosition(float)
     * @see EntityPlayer#getPosition(float)
     */
    public static Vec3 getEyePosition(EntityLivingBase entity, float renderPartialTicks) {
        if (renderPartialTicks == 1.0f) {
            renderPartialTicks = 0.0f;
        }

        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) renderPartialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) renderPartialTicks + entity.getEyeHeight();
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) renderPartialTicks;
        return Vec3.createVectorHelper(d0, d1, d2);
    }

    /**
     * Silent version of EntityLivingBase#setHeldItem for players only
     *
     * @param player target
     * @param stack  stack to set
     */
    public static void setHeldItemSilent(EntityPlayer player, ItemStack stack) {
        player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
    }
}
