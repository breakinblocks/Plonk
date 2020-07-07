package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.packet.PacketBase;
import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class RegistryPackets {
    private static final SimpleChannel CHANNEL = Plonk.CHANNEL;

    public static void register(int id, PacketBase packet) {
        CHANNEL.registerMessage(
                id, packet.getClass().asSubclass(PacketBase.class),
                PacketBase::write,
                packet::read,
                PacketBase::onMessage,
                packet.getNetworkDirection()
        );
    }

    @SuppressWarnings("UnusedAssignment")
    public static void init() {
        int id = 0;
        // Client -> Server
        register(id++, new PacketPlaceItem());
    }
}
