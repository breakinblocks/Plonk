package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static net.minecraft.entity.player.EntityPlayer.REACH_DISTANCE;

public class PacketRotateTile extends PacketBase<PacketRotateTile> {
    private BlockPos pos;

    public PacketRotateTile() {
    }

    public PacketRotateTile(BlockPos pos) {
        this.pos = pos;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    @Override
    public Side getSideBound() {
        return Side.SERVER;
    }

    @Override
    protected void handle(MessageContext ctx) {
        if (ctx.side == Side.CLIENT) throw new RuntimeException("PacketRotateTile should be server-bound only.");
        EntityPlayerMP player = ctx.getServerHandler().player;
        WorldServer world = player.getServerWorld();
        double reach = player.getEntityAttribute(REACH_DISTANCE).getAttributeValue() + 2;
        if (player.getDistanceSq(pos) < reach * reach) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TilePlacedItems) {
                ((TilePlacedItems) te).rotateTile();
            }
        }
    }
}
