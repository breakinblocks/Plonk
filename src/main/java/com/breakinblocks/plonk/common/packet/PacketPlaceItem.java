package com.breakinblocks.plonk.common.packet;

import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @see CPlayerTryUseItemOnBlockPacket
 */
public class PacketPlaceItem extends PacketBase {
    private BlockRayTraceResult hit;
    private int renderType;

    public PacketPlaceItem() {
    }

    public PacketPlaceItem(BlockRayTraceResult hit, int renderType) {
        this.hit = hit;
        this.renderType = renderType;
    }

    @Override
    public PacketBase read(PacketBuffer buf) {
        hit = buf.readBlockHitResult();
        renderType = buf.readInt();
        return new PacketPlaceItem(hit, renderType);
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockHitResult(hit);
        buf.writeInt(renderType);
    }

    @Override
    public Optional<NetworkDirection> getNetworkDirection() {
        return Optional.of(NetworkDirection.PLAY_TO_SERVER);
    }

    /**
     * @see ServerPlayNetHandler#processTryUseItemOnBlock(CPlayerTryUseItemOnBlockPacket)
     */
    @Override
    protected void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity player = Objects.requireNonNull(ctx.get().getSender());
        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
        ItemStack held = player.getMainHandItem();
        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
        EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, toPlace);
        if (toPlace.useOn(new ItemUseContext(player, Hand.MAIN_HAND, hit)) == ActionResultType.SUCCESS) {
            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
            EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, newHeld);
        } else {
            EntityUtils.setHeldItemSilent(player, Hand.MAIN_HAND, held);
        }
    }
}
