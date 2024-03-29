package com.breakinblocks.plonk;

import com.breakinblocks.plonk.client.ClientEvents;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.registry.RegistryPackets;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Plonk.MOD_ID)
public class Plonk {
    public static final String MOD_ID = "plonk";
    public static final String NAME = "Plonk";
    public static final String CARRY_ON_MOD_ID = "carryon";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Plonk() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onRegister);
        modEventBus.addListener(this::setup);
        modEventBus.addListener(PlonkConfig::refresh);

        if (FMLEnvironment.dist.isClient()) {
            ClientEvents.init(modEventBus);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PlonkConfig.serverSpec);
        PlonkTags.init();
    }

    public void onRegister(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, RegistryBlocks::init);
        event.register(ForgeRegistries.Keys.ITEMS, RegistryItems::init);
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, RegistryTileEntities::init);
    }

    public void setup(FMLCommonSetupEvent event) {
        RegistryPackets.init();
    }
}
