package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketPlaceItem extends PacketBase<PacketPlaceItem> {
    private BlockPos pos;
    private EnumFacing facing;
    private float hitX;
    private float hitY;
    private float hitZ;
    private boolean isBlock;

    public PacketPlaceItem() {
    }

    public PacketPlaceItem(BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, boolean isBlock) {
        this.pos = pos;
        this.facing = facing;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.isBlock = isBlock;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        facing = EnumFacing.values()[buf.readInt()];
        hitX = buf.readFloat();
        hitY = buf.readFloat();
        hitZ = buf.readFloat();
        isBlock = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(facing.ordinal());
        buf.writeFloat(hitX);
        buf.writeFloat(hitY);
        buf.writeFloat(hitZ);
        buf.writeBoolean(isBlock);
    }

    @Override
    public Side getSideBound() {
        return Side.SERVER;
    }

    @Override
    protected void handle(MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        WorldServer world = player.getServerWorld();
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        ItemStack held = player.getHeldItemMainhand();
        RegistryItems.placed_items.setHeldStack(toPlace, held, isBlock);
        EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, toPlace);
        if (toPlace.onItemUse(player, world, pos, EnumHand.MAIN_HAND, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {
            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
            EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, newHeld);
        } else {
            EntityUtils.setHeldItemSilent(player, EnumHand.MAIN_HAND, held);
        }
    }
}
