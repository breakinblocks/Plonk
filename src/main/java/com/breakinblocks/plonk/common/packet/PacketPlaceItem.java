package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

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
    protected void handle(MessageContext ctx) {
        if (ctx.side == Side.CLIENT) throw new RuntimeException("PacketPlaceItem should be server-bound only.");
        EntityPlayerMP player = ctx.getServerHandler().player;
        World world = player.getEntityWorld();
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        ItemStack held = player.getHeldItemMainhand();
        RegistryItems.placed_items.setHeldStack(toPlace, held);
        toPlace.setTagInfo(TilePlacedItems.TAG_IS_BLOCK, new NBTTagInt(isBlock ? 1 : 0));
        player.setHeldItem(EnumHand.MAIN_HAND, toPlace);
        if (toPlace.onItemUse(player, world, pos, EnumHand.MAIN_HAND, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS) {

        }
        player.setHeldItem(EnumHand.MAIN_HAND, RegistryItems.placed_items.getHeldStack(toPlace));
    }
}
