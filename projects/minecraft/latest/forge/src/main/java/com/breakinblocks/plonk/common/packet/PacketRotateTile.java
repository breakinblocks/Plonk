package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraftforge.common.ForgeMod.REACH_DISTANCE;

public class PacketRotateTile extends PacketBase {
    private BlockPos pos;

    public PacketRotateTile() {
    }

    public PacketRotateTile(BlockPos pos) {
        this.pos = pos;
    }


    @Override
    public PacketBase read(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        return new PacketRotateTile(pos);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Optional<NetworkDirection> getNetworkDirection() {
        return Optional.of(NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    protected void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
        ServerLevel world = player.getLevel();
        double reach = player.getAttributeValue(REACH_DISTANCE.get()) + 2;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < reach * reach) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof TilePlacedItems) {
                ((TilePlacedItems) te).rotateTile();
            }
        }
    }
}
