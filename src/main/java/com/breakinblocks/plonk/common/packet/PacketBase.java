package com.breakinblocks.plonk.common.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class PacketBase<PKT extends PacketBase> implements IMessage, IMessageHandler<PKT, IMessage> {

    @Override
    public abstract void fromBytes(ByteBuf buf);

    @Override
    public abstract void toBytes(ByteBuf buf);

    @Override
    public IMessage onMessage(PKT message, MessageContext ctx) {
        if (ctx.side != this.getSideBound())
            throw new RuntimeException(this.getClass().getName() + " should only be received on side " + this.getSideBound().toString());

        if (isAsync()) {
            message.handle(ctx);
        } else {
            if (ctx.side == Side.SERVER) {
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> message.handle(ctx));
            } else {
                Minecraft.getMinecraft().addScheduledTask(() -> message.handle(ctx));
            }
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

    /**
     * Side the packet should be received on.
     *
     * @return receiving side
     */
    public abstract Side getSideBound();

    protected abstract void handle(MessageContext ctx);
}
