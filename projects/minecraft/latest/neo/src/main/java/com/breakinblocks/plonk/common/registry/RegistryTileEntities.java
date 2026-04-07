package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryTileEntities {
    public static final BlockEntityType<TilePlacedItems> placed_items = new BlockEntityType<>(TilePlacedItems::new, RegistryBlocks.placed_items);
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        for (Field f : RegistryTileEntities.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == BlockEntityType.class) {
                        Identifier id = Identifier.fromNamespaceAndPath(Plonk.MOD_ID, f.getName());
                        LOG.info("Registering BlockEntity: {}", id);
                        BlockEntityType<?> type = (BlockEntityType<?>) f.get(null);
                        helper.register(id, type);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
