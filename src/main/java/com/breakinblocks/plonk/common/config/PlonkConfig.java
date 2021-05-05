package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class PlonkConfig {
    public static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static int getInventoryStackLimit() {
        int maxStackSize = SERVER.maxStackSize.get();
        return maxStackSize <= 0 ? ItemUtils.getMaxStackSize() : maxStackSize;
    }

    public static class Server {
        public final IntValue maxStackSize;
        public final ConfigValue<List<? extends String>> unplaceableItems;

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
                            ), o -> o instanceof String && ResourceLocation.isResouceNameValid((String) o));
        }
    }
}
