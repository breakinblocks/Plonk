package com.breakinblocks.plonk;

import com.breakinblocks.plonk.client.ClientEvents;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryDataComponents;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.registry.RegistryPackets;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import com.breakinblocks.plonk.data.DataGenerators;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Plonk.MOD_ID)
public class Plonk {
    public static final String MOD_ID = "plonk";
    public static final String NAME = "Plonk";
    public static final String CARRY_ON_MOD_ID = "carryon";

    private static final String PROTOCOL_VERSION = "1";

    public Plonk(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::onRegisterPayloadHandlers);
        modEventBus.addListener(this::onRegisterClientExtensions);
        modEventBus.addListener(PlonkConfig::refresh);
        modEventBus.addListener(DataGenerators::gatherData);

        if (FMLEnvironment.dist.isClient()) {
            ClientEvents.init(modEventBus);
        }

        modContainer.registerConfig(ModConfig.Type.SERVER, PlonkConfig.serverSpec);
        PlonkTags.init();
    }

    public void onRegister(RegisterEvent event) {
        event.register(Registries.BLOCK, RegistryBlocks::init);
        event.register(Registries.ITEM, RegistryItems::init);
        event.register(Registries.BLOCK_ENTITY_TYPE, RegistryTileEntities::init);
        event.register(Registries.DATA_COMPONENT_TYPE, RegistryDataComponents::init);
    }

    public void onRegisterPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        RegistryPackets.init(event.registrar(PROTOCOL_VERSION));
    }

    public void onRegisterClientExtensions(final RegisterClientExtensionsEvent event) {
        BlockPlacedItems.initializeClientStatic(extension -> event.registerBlock(extension, RegistryBlocks.placed_items));
    }
}
