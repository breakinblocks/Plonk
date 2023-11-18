package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.packet.PacketRotateTile;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
        if (FMLClientHandler.instance().getClient().inGameHasFocus && mc.ingameGUI != null) {
            if (KEY_PLACE.isPressed()) {
                RayTraceResult hit = mc.objectMouseOver;
                if (hit.typeOfHit == RayTraceResult.Type.BLOCK) {
                    float hitX = (float) hit.hitVec.x;
                    float hitY = (float) hit.hitVec.y;
                    float hitZ = (float) hit.hitVec.z;
                    ItemStack held = player.getHeldItemMainhand();
                    if (!held.isEmpty()) {
                        int renderType = TESRPlacedItems.getRenderTypeFromStack(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
                        EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, toPlace);
                        if (toPlace.onItemUse(player, world, hit.getBlockPos(), EnumHand.MAIN_HAND, hit.sideHit, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(hit.getBlockPos(), hit.sideHit, hitX, hitY, hitZ, renderType));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, newHeld);
                        } else {
                            EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, held);
                        }
                    } else if (player.isSneaking()) {
                        if (!rotatePlacedItemsTile(world, hit.getBlockPos())) {
                            rotatePlacedItemsTile(world, hit.getBlockPos().offset(hit.sideHit));
                        }
                    }
                }
            }
        }
    }

    private boolean rotatePlacedItemsTile(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePlacedItems) {
            ((TilePlacedItems) te).rotateTile();
            Plonk.CHANNEL.sendToServer(new PacketRotateTile(pos));
            return true;
        }
        return false;
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(ClientEvents.KEY_PLACE);
    }
}
