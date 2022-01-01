package com.breakinblocks.plonk.common.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;

public abstract class PacketBase<PKT extends PacketBase<PKT>> implements IMessage, IMessageHandler<PKT, IMessage> {

    @Override
    public abstract void fromBytes(ByteBuf buf);

    @Override
    public abstract void toBytes(ByteBuf buf);

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    @Override
    @Nullable
    public IMessage onMessage(PKT message, MessageContext ctx) {
        if (isAsync()) {
            message.handle(ctx);
        } else {
            // TODO: in 1.8+ make sure the packet runs on the main thread
            message.handle(ctx);
        }
        return null;
    }

    /**
     * Check if the handler can be called asynchronously
     * Defaults to false
     *
     * @return true if the handler can be called asynchronously
     */
    public boolean isAsync() {
        return false;
    }

    protected abstract void handle(MessageContext ctx);
}
