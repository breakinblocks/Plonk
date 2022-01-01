package com.breakinblocks.plonk;

import com.breakinblocks.plonk.client.ClientEvents;
import com.breakinblocks.plonk.client.registry.RegistryTESRs;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.registry.RegistryPackets;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::setupClient);
        modEventBus.register(this);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PlonkConfig.serverSpec);
        modEventBus.addListener(PlonkConfig::refresh);
        PlonkTags.init();
    }

    @SubscribeEvent
    public void registerBlocks(Register<Block> event) {
        RegistryBlocks.init(event);
    }

    @SubscribeEvent
    public void registerItems(Register<Item> event) {
        RegistryItems.init(event);
    }

    @SubscribeEvent
    public void registerTileEntities(Register<TileEntityType<?>> event) {
        RegistryTileEntities.init(event);
    }

    public void setup(FMLCommonSetupEvent event) {
        RegistryPackets.init();
    }

    public void setupClient(FMLClientSetupEvent event) {
        RegistryTESRs.init();
        RenderTypeLookup.setRenderLayer(RegistryBlocks.placed_items, RenderType.cutout());
        ClientEvents.registerKeyBindings();
    }
}
