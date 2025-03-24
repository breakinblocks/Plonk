package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @see ServerboundUseItemOnPacket
 */
public class PacketPlaceItem extends PacketBase {
    private BlockHitResult hit;
    private int renderType;

    public PacketPlaceItem() {
    }

    public PacketPlaceItem(BlockHitResult hit, int renderType) {
        this.hit = hit;
        this.renderType = renderType;
    }

    @Override
    public PacketBase read(FriendlyByteBuf buf) {
        hit = buf.readBlockHitResult();
        renderType = buf.readInt();
        return new PacketPlaceItem(hit, renderType);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockHitResult(hit);
        buf.writeInt(renderType);
    }

    @Override
    public Optional<NetworkDirection> getNetworkDirection() {
        return Optional.of(NetworkDirection.PLAY_TO_SERVER);
    }

    /**
     * @see ServerGamePacketListenerImpl#handleUseItemOn(ServerboundUseItemOnPacket)
     */
    @Override
    protected void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        ItemStack held = player.getMainHandItem();
        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
        EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, toPlace);
        if (toPlace.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()) {
            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, newHeld);
        } else {
            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, held);
        }
    }
}
