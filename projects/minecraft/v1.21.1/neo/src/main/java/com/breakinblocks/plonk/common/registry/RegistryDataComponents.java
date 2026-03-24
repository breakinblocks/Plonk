package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.item.ItemBlockPlacedItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

public class RegistryDataComponents {
    private static final Logger LOG = LogManager.getLogger();

    public static final DataComponentType<ItemBlockPlacedItems.Data> ITEM_BLOCK_PLACED_ITEMS_DATA = DataComponentType.<ItemBlockPlacedItems.Data>builder()
            .persistent(ItemBlockPlacedItems.Data.CODEC)
            .networkSynchronized(ItemBlockPlacedItems.Data.STREAM_CODEC)
            .build();

    public static void init(RegisterEvent.RegisterHelper<DataComponentType<?>> helper) {
        for (Field f : RegistryDataComponents.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (DataComponentType.class.isAssignableFrom(f.getType())) {
                        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(Plonk.MOD_ID, f.getName().toLowerCase(Locale.ROOT));
                        LOG.info("Registering Data Component Type: {}", rl);
                        DataComponentType<?> dataComponentType = (DataComponentType<?>) f.get(null);
                        helper.register(rl, dataComponentType);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
