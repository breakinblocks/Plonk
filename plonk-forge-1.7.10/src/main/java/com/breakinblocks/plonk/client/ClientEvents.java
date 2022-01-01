package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.packet.PacketRotateTile;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
        if (FMLClientHandler.instance().getClient().inGameHasFocus && mc.ingameGUI != null) {
            if (KEY_PLACE.isPressed()) {
                MovingObjectPosition hit = mc.objectMouseOver;
                if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    int x = hit.blockX;
                    int y = hit.blockY;
                    int z = hit.blockZ;
                    float hitX = (float) hit.hitVec.xCoord;
                    float hitY = (float) hit.hitVec.yCoord;
                    float hitZ = (float) hit.hitVec.zCoord;
                    ItemStack held = player.getHeldItem();
                    // TODO: Update null
                    if (held != null && held.stackSize > 0) {
                        int renderType = TESRPlacedItems.getRenderTypeFromStack(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
                        EntityUtils.setHeldItemSilent(player, toPlace);
                        if (toPlace.tryPlaceItemIntoWorld(player, world, x, y, z, hit.sideHit, hitX, hitY, hitZ)) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(x, y, z, hit.sideHit, hitX, hitY, hitZ, renderType));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            EntityUtils.setHeldItemSilent(player, newHeld);
                        } else {
                            EntityUtils.setHeldItemSilent(player, held);
                        }
                    } else if (player.isSneaking()) {
                        if (!rotatePlacedItemsTile(world, x, y, z)) {
                            rotatePlacedItemsTile(world,
                                    x + Facing.offsetsXForSide[hit.sideHit],
                                    y + Facing.offsetsYForSide[hit.sideHit],
                                    z + Facing.offsetsZForSide[hit.sideHit]
                            );
                        }
                    }
                }
            }
        }
    }

    private boolean rotatePlacedItemsTile(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TilePlacedItems) {
            ((TilePlacedItems) te).rotateTile();
            Plonk.CHANNEL.sendToServer(new PacketRotateTile(x, y, z));
            return true;
        }
        return false;
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(ClientEvents.KEY_PLACE);
    }
}
