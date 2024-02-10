package com.breakinblocks.plonk.client;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.command.CommandClientPlonk;
import com.breakinblocks.plonk.client.registry.RegistryTESRs;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.packet.PacketRotateTile;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static net.minecraftforge.client.settings.KeyConflictContext.IN_GAME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

public class ClientEvents {
    public static final KeyMapping KEY_PLACE = new KeyMapping("key.plonk.place", IN_GAME, KEYSYM, GLFW_KEY_P, "key.categories.plonk");

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientEvents::setupClient);
        modEventBus.addListener(ClientEvents::onRegisterKeyMappings);

        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onKeyInput);
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::serverStarting);
    }

    public static void setupClient(FMLClientSetupEvent event) {
        RegistryTESRs.init();
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.KEY_PLACE);
    }

    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.getOverlay() == null && mc.screen == null) {
            if (KEY_PLACE.consumeClick() && player != null) {
                HitResult hitRaw = mc.hitResult;
                if (hitRaw != null && hitRaw.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hit = (BlockHitResult) hitRaw;
                    ItemStack held = player.getMainHandItem();
                    if (!held.isEmpty()) {
                        int renderType = TESRPlacedItems.getRenderTypeFromStack(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
                        EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, toPlace);
                        if (toPlace.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()) {
                            Plonk.CHANNEL.sendToServer(new PacketPlaceItem(hit, renderType));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, newHeld);
                        } else {
                            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, held);
                        }
                    } else if (player.isShiftKeyDown()) {
                        if (!rotatePlacedItemsTile(player.level(), hit.getBlockPos())) {
                            rotatePlacedItemsTile(player.level(), hit.getBlockPos().relative(hit.getDirection()));
                        }
                    }
                }
            }
        }
    }

    private static boolean rotatePlacedItemsTile(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePlacedItems) {
            ((TilePlacedItems) te).rotateTile();
            Plonk.CHANNEL.sendToServer(new PacketRotateTile(pos));
            return true;
        }
        return false;
    }

    public static void serverStarting(ServerStartingEvent event) {
        new CommandClientPlonk().register(event.getServer().getCommands().getDispatcher());
    }
}
