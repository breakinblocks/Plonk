package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import static net.minecraft.client.util.InputMappings.Type.KEYSYM;
import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

public class ClientEvents {
    public static final KeyBinding KEY_PLACE = new KeyBinding("key.plonk.place", IN_GAME, KEYSYM, GLFW_KEY_P, "key.categories.plonk");

    static {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    public static void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(ClientEvents.KEY_PLACE);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (mc.loadingGui == null && (mc.currentScreen == null || mc.currentScreen.passEvents)) {
            if (KEY_PLACE.isPressed() && player != null) {
                ItemStack held = player.getHeldItemMainhand();
                if (!held.isEmpty()) {
                    RayTraceResult hitRaw = mc.objectMouseOver;
                    if (hitRaw != null && hitRaw.getType() == RayTraceResult.Type.BLOCK) {
                        BlockRayTraceResult hit = (BlockRayTraceResult) hitRaw;
                        int renderType = TESRPlacedItems.getRenderTypeFromStack(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
                        EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, toPlace);
                        if (toPlace.onItemUse(new ItemUseContext(player, Hand.MAIN_HAND, hit)).isSuccessOrConsume()) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(hit, renderType));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, newHeld);
                        } else {
                            EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, held);
                        }
                    }
                }
            }
        }
    }
}
