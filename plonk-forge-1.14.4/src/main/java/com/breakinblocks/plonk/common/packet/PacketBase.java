package com.breakinblocks.plonk.common.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class PacketBase {

    public abstract PacketBase read(PacketBuffer buf);

    public abstract void write(PacketBuffer buf);

    public void onMessage(Supplier<Context> ctx) {
        if (isAsync()) {
            this.handle(ctx);
        } else {
            ctx.get().enqueueWork(() -> this.handle(ctx));
        }
    }

    /**
     * Check if the handler can be called asynchronously
     * Defaults to false
     * For mixed async and synchronous: return true and use {@link Context#enqueueWork(Runnable)}
     *
     * @return true if the handler can be called asynchronously
     */
    public boolean isAsync() {
        return false;
    }

    public abstract Optional<NetworkDirection> getNetworkDirection();

    protected abstract void handle(Supplier<Context> ctx);
}
