package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {
    /**
     * Gets the position of the entity's eyes
     *
     * @param entity             target
     * @param renderPartialTicks fractional tick
     * @return position of eye
     * @see Entity#getPositionEyes(float)
     * @deprecated
     */
    public static Vec3d getEyePosition(EntityLivingBase entity, float renderPartialTicks) {
        return entity.getPositionEyes(renderPartialTicks);
    }

    /**
     * Silent version of EntityLivingBase#setHeldItem for players only
     *
     * @param player target
     * @param hand   hand to set
     * @param stack  stack to set
     * @see EntityLivingBase#setHeldItem(net.minecraft.util.EnumHand, net.minecraft.item.ItemStack)
     */
    public static void setHeldItemSilent(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (hand == EnumHand.MAIN_HAND) {
            setItemStackToSlotSilent(player, EntityEquipmentSlot.MAINHAND, stack);
        } else {
            if (hand != EnumHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + hand);
            }
            setItemStackToSlotSilent(player, EntityEquipmentSlot.OFFHAND, stack);
        }
    }

    /**
     * Silent version of EntityPlayer#setItemStackToSlot
     *
     * @param player target
     * @param slotIn equipment slot to set
     * @param stack  stack to set
     * @see EntityPlayer#setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot, net.minecraft.item.ItemStack)
     */
    public static void setItemStackToSlotSilent(EntityPlayer player, EntityEquipmentSlot slotIn, ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);
        } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
            player.inventory.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            player.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }
}
