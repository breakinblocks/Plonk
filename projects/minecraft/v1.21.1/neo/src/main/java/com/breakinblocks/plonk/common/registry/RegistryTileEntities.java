package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryTileEntities {
    public static final BlockEntityType<TilePlacedItems> placed_items = BlockEntityType.Builder.of(TilePlacedItems::new, RegistryBlocks.placed_items).build(null);
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        for (Field f : RegistryTileEntities.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == BlockEntityType.class) {
                        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(Plonk.MOD_ID, f.getName());
                        LOG.info("Registering BlockEntity: {}", rl);
                        BlockEntityType<?> type = (BlockEntityType<?>) f.get(null);
                        helper.register(rl, type);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
