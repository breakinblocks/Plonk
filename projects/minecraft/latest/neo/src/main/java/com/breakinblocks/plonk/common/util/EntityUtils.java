package com.breakinblocks.plonk.common.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EntityUtils {
    /**
     * Silent version of {@link LivingEntity#setItemInHand(InteractionHand, ItemStack)} for players only
     *
     * @param player target
     * @param hand   hand to set
     * @param stack  stack to set
     * @see LivingEntity#setItemInHand(InteractionHand, ItemStack)
     */
    public static void setHeldItemSilent(Player player, InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            setItemStackToSlotSilent(player, EquipmentSlot.MAINHAND, stack);
        } else {
            if (hand != InteractionHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + hand);
            }
            setItemStackToSlotSilent(player, EquipmentSlot.OFFHAND, stack);
        }
    }

    /**
     * Silent version of {@link Player#setItemSlot(EquipmentSlot, ItemStack)}
     *
     * @param player target
     * @param slotIn equipment slot to set
     * @param stack  stack to set
     * @see Player#setItemSlot(EquipmentSlot, ItemStack)
     */
    public static void setItemStackToSlotSilent(Player player, EquipmentSlot slotIn, ItemStack stack) {
        player.verifyEquippedItem(stack);
        if (slotIn == EquipmentSlot.MAINHAND) {
            player.getInventory().items.set(player.getInventory().selected, stack);
        } else if (slotIn == EquipmentSlot.OFFHAND) {
            player.getInventory().offhand.set(0, stack);
        } else if (slotIn.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            player.getInventory().armor.set(slotIn.getIndex(), stack);
        }
    }
}
