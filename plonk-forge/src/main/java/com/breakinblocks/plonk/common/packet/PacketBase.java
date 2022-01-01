package com.breakinblocks.plonk.common.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class PacketBase {

    public abstract PacketBase read(FriendlyByteBuf buf);

    public abstract void write(FriendlyByteBuf buf);

    public void onMessage(Supplier<NetworkEvent.Context> ctx) {
        if (isAsync()) {
            this.handle(ctx);
        } else {
            ctx.get().enqueueWork(() -> this.handle(ctx));
        }
    }

    /**
     * Check if the handler can be called asynchronously
     * Defaults to false
     * For mixed async and synchronous: return true and use {@link NetworkEvent.Context#enqueueWork(Runnable)}
     *
     * @return true if the handler can be called asynchronously
     */
    public boolean isAsync() {
        return false;
    }

    public abstract Optional<NetworkDirection> getNetworkDirection();

    protected abstract void handle(Supplier<NetworkEvent.Context> ctx);
}
