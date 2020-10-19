package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public PacketBase read(PacketBuffer buf) {
        pos = buf.readBlockPos();
        return new PacketRotateTile(pos);
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Optional<NetworkDirection> getNetworkDirection() {
        return Optional.of(NetworkDirection.PLAY_TO_SERVER);
    }

    @Override
    protected void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = Objects.requireNonNull(ctx.get().getSender());
        ServerWorld world = player.getServerWorld();
        double reach = player.getAttributeValue(REACH_DISTANCE.get()) + 2;
        if (player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < reach * reach) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TilePlacedItems) {
                ((TilePlacedItems) te).rotateTile();
            }
        }
    }
}
