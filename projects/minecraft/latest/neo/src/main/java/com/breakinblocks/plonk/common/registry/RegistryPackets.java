package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.common.packet.PacketPlaceItem;
import com.breakinblocks.plonk.common.packet.PacketRotateTile;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class RegistryPackets {
    public static void init(final PayloadRegistrar registrar) {
        PacketPlaceItem.register(registrar);
        PacketRotateTile.register(registrar);
    }
}
