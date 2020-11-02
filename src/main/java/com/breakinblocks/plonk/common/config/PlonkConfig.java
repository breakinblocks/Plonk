package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.util.ItemUtils;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

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
        FMLCommonHandler.instance().bus().register(new Listener());
        config = new Configuration(suggestedConfigurationFile);
        config.load();
        sync();
    }

    public static class Listener {
        @SubscribeEvent
        public void reloadConfig(OnConfigChangedEvent event) {
            if (!event.modID.equals(Plonk.MOD_ID)) return;
            sync();
        }
    }
}
