package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class ClientEvents {
    public static final KeyBinding KEY_PLACE = new KeyBinding("key.plonk.place.desc", Keyboard.KEY_P, "key.plonk.category");

    public ClientEvents() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        EntityClientPlayerMP player = mc.thePlayer;
        GuiIngame ingameGUI = mc.ingameGUI;
        if (FMLClientHandler.instance().getClient().inGameHasFocus) {
            if (KEY_PLACE.isPressed()) {
                if (ingameGUI != null) {
                    MovingObjectPosition hit = mc.objectMouseOver;
                    if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        EnumFacing hitSide = EnumFacing.getFront(hit.sideHit);
                        int x = hit.blockX + hitSide.getFrontOffsetX();
                        int y = hit.blockY + hitSide.getFrontOffsetY();
                        int z = hit.blockZ + hitSide.getFrontOffsetZ();
                        int side = Facing.oppositeSide[hit.sideHit];
                        float hitX = (float) hit.hitVec.xCoord;
                        float hitY = (float) hit.hitVec.yCoord;
                        float hitZ = (float) hit.hitVec.zCoord;
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        if (toPlace.tryPlaceItemIntoWorld(player, world, x, y, z, side, hitX, hitY, hitZ)) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(x, y, z, side, hitX, hitY, hitZ));
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
