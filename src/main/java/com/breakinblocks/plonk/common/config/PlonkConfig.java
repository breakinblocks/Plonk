package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlonkConfig {
    public static final ForgeConfigSpec serverSpec;
    private static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    /**
     * Gets the maximum stack size for placed items.
     */
    public static int getInventoryStackLimit() {
        int maxStackSize = SERVER.maxStackSize.get();
        return maxStackSize <= 0 ? ItemUtils.getMaxStackSize() : maxStackSize;
    }

    /**
     * Checks if the given stack can be placed down.
     */
    public static boolean canPlace(ItemStack stack) {
        return !PlonkConfig.SERVER.unplaceableItemsSet.contains(ItemUtils.getIdentifier(stack));
    }

    public static void refresh(ModConfig.ModConfigEvent event) {
        ModConfig modConfig = event.getConfig();
        ForgeConfigSpec spec = modConfig.getSpec();
        if (spec == serverSpec) {
            SERVER.refresh();
        }
    }

    private static class Server {
        public final IntValue maxStackSize;
        private final ConfigValue<List<? extends String>> unplaceableItems;
        public Set<ResourceLocation> unplaceableItemsSet = Collections.emptySet();

        Server(ForgeConfigSpec.Builder builder) {
            maxStackSize = builder
                    .comment("Max stack size per slot (-1 or 0 to use default). Going above 64 needs a mod like StackUp!.")
                    .defineInRange("maxStackSize", -1, -1, Integer.MAX_VALUE);
            unplaceableItems = builder
                    .comment("Items that cannot be placed down, in the format 'modid:item_id' e.g. minecraft:carrot")
                    .defineList("unplaceableItems",
                            Arrays.asList(
                                    Plonk.CARRY_ON_MOD_ID + ":entity_item",
                                    Plonk.CARRY_ON_MOD_ID + ":tile_item"
                            ), o -> o instanceof String && ResourceLocation.tryCreate((String) o) != null);
        }

        public void refresh() {
            unplaceableItemsSet = unplaceableItems.get().stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet());
        }
    }
}
