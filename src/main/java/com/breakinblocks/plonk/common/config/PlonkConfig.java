package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class PlonkConfig {
    public static Configuration config;

    public static int maxStackSize = -1;

    public static int getInventoryStackLimit() {
        return maxStackSize <= 0 ? ItemUtils.getMaxStackSize() : maxStackSize;
    }

    private static void sync() {
        maxStackSize = config.getInt(
                "maxStackSize", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE,
                "Max stack size per slot (-1 or 0 to use default). Going above 64 needs a mod like StackUp!."
        );

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void init(File suggestedConfigurationFile) {
        MinecraftForge.EVENT_BUS.register(new Listener());
        config = new Configuration(suggestedConfigurationFile);
        config.load();
        sync();
    }

    public static class Listener {
        @SubscribeEvent
        public void reloadConfig(OnConfigChangedEvent event) {
            if (!event.getModID().equals(Plonk.MOD_ID)) return;
            sync();
        }
    }
}
