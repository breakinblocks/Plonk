package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryCodecs;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;

/**
 * @see ServerboundUseItemOnPacket
 */
public record PacketPlaceItem(BlockHitResult hit, int renderType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketPlaceItem> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Plonk.MOD_ID, "place_item"));

    public static final StreamCodec<FriendlyByteBuf, PacketPlaceItem> STREAM_CODEC = StreamCodec.composite(
            RegistryCodecs.BLOCK_HIT_RESULT,
            PacketPlaceItem::hit,
            ByteBufCodecs.VAR_INT,
            PacketPlaceItem::renderType,
            PacketPlaceItem::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, PacketPlaceItem::handle);
    }

    /**
     * @see ServerGamePacketListenerImpl#handleUseItemOn(ServerboundUseItemOnPacket)
     */
    private static void handle(PacketPlaceItem payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.player());
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        ItemStack held = player.getMainHandItem();
        RegistryItems.placed_items.setHeldStack(toPlace, held, payload.renderType);
        EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, toPlace);
        if (toPlace.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, payload.hit)).consumesAction()) {
            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, newHeld);
        } else {
            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, held);
        }
    }
}
