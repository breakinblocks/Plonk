package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

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

        Server(ForgeConfigSpec.Builder builder) {
            maxStackSize = builder
                    .comment("Max stack size per slot (-1 or 0 to use default). Going above 64 needs a mod like StackUp!.")
                    .defineInRange("maxStackSize", -1, -1, Integer.MAX_VALUE);
        }
    }
}
