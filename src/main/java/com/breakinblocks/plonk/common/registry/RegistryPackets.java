package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.packet.PacketRotateTile;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RegistryPackets {
    private static final SimpleNetworkWrapper CHANNEL = Plonk.CHANNEL;

    public static void init() {
        int id = 0;
        // Client -> Server
        CHANNEL.registerMessage(PacketPlaceItem.class, PacketPlaceItem.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketRotateTile.class, PacketRotateTile.class, id++, Side.SERVER);
    }
}
