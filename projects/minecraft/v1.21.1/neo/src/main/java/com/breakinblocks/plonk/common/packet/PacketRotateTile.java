package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;

public record PacketRotateTile(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketRotateTile> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Plonk.MOD_ID, "rotate_tile"));

    public static final StreamCodec<ByteBuf, PacketRotateTile> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PacketRotateTile::pos,
            PacketRotateTile::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, PacketRotateTile::handle);
    }

    private static void handle(PacketRotateTile payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.player());
        ServerLevel world = player.serverLevel();
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 2;
        if (player.distanceToSqr(payload.pos.getX() + 0.5, payload.pos.getY() + 0.5, payload.pos.getZ() + 0.5) < reach * reach) {
            BlockEntity te = world.getBlockEntity(payload.pos);
            if (te instanceof TilePlacedItems) {
                ((TilePlacedItems) te).rotateTile();
            }
        }
    }
}
