package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ClientEvents {
    public static final KeyBinding KEY_PLACE = new KeyBinding("key.plonk.place.desc", Keyboard.KEY_P, "key.plonk.category");

    public ClientEvents() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        EntityPlayerSP player = mc.player;
        GuiIngame ingameGUI = mc.ingameGUI;
        if (FMLClientHandler.instance().getClient().inGameHasFocus) {
            if (KEY_PLACE.isPressed()) {
                ItemStack held = player.getHeldItemMainhand();
                if (ingameGUI != null && !held.isEmpty()) {
                    RayTraceResult hit = mc.objectMouseOver;
                    if (hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                        float hitX = (float) hit.hitVec.x;
                        float hitY = (float) hit.hitVec.y;
                        float hitZ = (float) hit.hitVec.z;
                        boolean isBlock = TESRPlacedItems.isGoingToRenderAsBlock(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        toPlace.setTagInfo(TilePlacedItems.TAG_IS_BLOCK, new NBTTagInt(isBlock ? 1 : 0));
                        RegistryItems.placed_items.setHeldStack(toPlace, held, isBlock);
                        player.setHeldItem(EnumHand.MAIN_HAND, toPlace);
                        if (toPlace.onItemUse(player, world, hit.getBlockPos(), EnumHand.MAIN_HAND, hit.sideHit, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(hit.getBlockPos(), hit.sideHit, hitX, hitY, hitZ, isBlock));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            player.setHeldItem(EnumHand.MAIN_HAND, newHeld);
                        } else {
                            player.setHeldItem(EnumHand.MAIN_HAND, held);
                        }
                    }
                }
            }
        }
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(ClientEvents.KEY_PLACE);
    }
}
