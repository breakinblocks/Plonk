package com.breakinblocks.plonk.common.config;

import com.breakinblocks.plonk.common.tag.PlonkTags;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

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
        if (stack.is(PlonkTags.Items.UNPLACEABLE))
            return false;
        return !PlonkConfig.SERVER.unplaceableItemsSet.contains(ItemUtils.getIdentifier(stack));
    }

    public static void refresh(ModConfigEvent event) {
        ModConfig modConfig = event.getConfig();
        IConfigSpec<?> spec = modConfig.getSpec();
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
                    .comment("Items that cannot be placed down, in the format \"mod_id:item_id\" e.g. [\"minecraft:carrot\"]",
                            "You can also use the " + PlonkTags.Items.UNPLACEABLE.location() + " item tag as well.")
                    .defineList("unplaceableItems", Collections.emptyList(),
                            o -> o instanceof String && ResourceLocation.tryParse((String) o) != null);
        }

        public void refresh() {
            unplaceableItemsSet = unplaceableItems.get().stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet());
        }
    }
}
