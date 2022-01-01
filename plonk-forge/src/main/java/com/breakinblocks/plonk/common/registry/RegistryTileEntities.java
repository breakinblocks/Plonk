package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class RegistryTileEntities {
    public static final BlockEntityType<TilePlacedItems> placed_items = BlockEntityType.Builder.of(TilePlacedItems::new, RegistryBlocks.placed_items).build(null);
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegistryEvent.Register<BlockEntityType<?>> event) {
        IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
        for (Field f : RegistryTileEntities.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == BlockEntityType.class) {
                        ResourceLocation rl = new ResourceLocation(Plonk.MOD_ID, f.getName());
                        LOG.info(REGISTRIES, "Registering BlockEntity: " + rl);
                        BlockEntityType<?> type = (BlockEntityType<?>) f.get(null);
                        type.setRegistryName(rl);
                        registry.register(type);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
