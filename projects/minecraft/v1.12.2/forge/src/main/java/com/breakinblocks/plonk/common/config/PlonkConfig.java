package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.PlonkConstants;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PlonkConfig {
    public static Configuration config;

    public static int maxStackSize = -1;

    public static Set<ResourceLocation> unplaceableItems = Collections.emptySet();

    /**
     * Gets the maximum stack size for placed items.
     */
    public static int getInventoryStackLimit() {
        return maxStackSize <= 0 ? ItemUtils.getMaxStackSize() : maxStackSize;
    }

    /**
     * Checks if the given stack can be placed down.
     */
    public static boolean canPlace(ItemStack stack) {
        return !PlonkConfig.unplaceableItems.contains(ItemUtils.getIdentifier(stack));
    }

    private static void sync() {
        maxStackSize = config.getInt(
                "maxStackSize", Configuration.CATEGORY_GENERAL, -1, -1, Integer.MAX_VALUE,
                "Max stack size per slot (-1 or 0 to use default). Going above 64 needs a mod like StackUp!."
        );

        unplaceableItems = Arrays.stream(config.getStringList(
                "unplaceableItems", Configuration.CATEGORY_GENERAL, new String[]{
                        PlonkConstants.CARRY_ON_MOD_ID + ":entity_item",
                        PlonkConstants.CARRY_ON_MOD_ID + ":tile_item"
                },
                "Items that cannot be placed down, in the format 'mod_id:item_id' e.g. minecraft:carrot"
        )).map(ResourceLocation::new).collect(Collectors.toCollection(LinkedHashSet::new));

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
            if (!event.getModID().equals(PlonkConstants.MOD_ID)) return;
            sync();
        }
    }
}
