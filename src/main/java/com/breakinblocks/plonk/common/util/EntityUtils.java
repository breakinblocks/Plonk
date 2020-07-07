package com.breakinblocks.plonk.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class EntityUtils {
    /**
     * Gets the position of the entity's eyes
     *
     * @param entity       target
     * @param partialTicks fractional tick
     * @return position of eye
     * @see Entity#getEyePosition(float)
     * @deprecated
     */
    public static Vector3d getEyePosition(Entity entity, float partialTicks) {
        return entity.getEyePosition(partialTicks);
    }

    /**
     * Silent version of {@link LivingEntity#setHeldItem(Hand, ItemStack)} for players only
     *
     * @param player target
     * @param hand   hand to set
     * @param stack  stack to set
     * @see LivingEntity#setHeldItem(Hand, ItemStack)
     */
    public static void setHeldItemSilent(PlayerEntity player, Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            setItemStackToSlotSilent(player, EquipmentSlotType.MAINHAND, stack);
        } else {
            if (hand != Hand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + hand);
            }
            setItemStackToSlotSilent(player, EquipmentSlotType.OFFHAND, stack);
        }
    }

    /**
     * Silent version of {@link PlayerEntity#setItemStackToSlot(EquipmentSlotType, ItemStack)}
     *
     * @param player target
     * @param slotIn equipment slot to set
     * @param stack  stack to set
     * @see PlayerEntity#setItemStackToSlot(EquipmentSlotType, ItemStack)
     */
    public static void setItemStackToSlotSilent(PlayerEntity player, EquipmentSlotType slotIn, ItemStack stack) {
        if (slotIn == EquipmentSlotType.MAINHAND) {
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            player.inventory.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            player.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }
}
