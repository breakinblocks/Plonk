package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketRotateTile extends PacketBase<PacketRotateTile> {
    private int x;
    private int y;
    private int z;

    @SuppressWarnings("unused")
    public PacketRotateTile() {
    }

    public PacketRotateTile(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    protected void handle(MessageContext ctx) {
        if (ctx.side == Side.CLIENT) throw new RuntimeException("PacketRotateTile should be server-bound only.");
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.getEntityWorld();
        double reach = player.theItemInWorldManager.getBlockReachDistance() + 2;
        if (player.getDistanceSq(x, y, z) < reach * reach) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TilePlacedItems) {
                ((TilePlacedItems) te).rotateTile();
            }
        }
    }
}
