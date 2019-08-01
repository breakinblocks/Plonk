package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.registry.RegistryItems;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.World;

public class PacketPlaceItem extends PacketBase<PacketPlaceItem> {
    private int x;
    private int y;
    private int z;
    private int side;
    private float hitX;
    private float hitY;
    private float hitZ;

    public PacketPlaceItem() {
    }

    public PacketPlaceItem(int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        side = buf.readInt();
        hitX = buf.readFloat();
        hitY = buf.readFloat();
        hitZ = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(side);
        buf.writeFloat(hitX);
        buf.writeFloat(hitY);
        buf.writeFloat(hitZ);
    }

    @Override
    protected void handle(MessageContext ctx) {
        if (ctx.side == Side.CLIENT) throw new RuntimeException("PacketPlaceItem should be server-bound only.");
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.getEntityWorld();
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        if (!toPlace.tryPlaceItemIntoWorld(player, world, x, y, z, side, hitX, hitY, hitZ)) {
            // TODO: Find out if this is the proper way to do this...
            ctx.getServerHandler().netManager.scheduleOutboundPacket(new S23PacketBlockChange(x, y, z, world));
        }
    }
}
