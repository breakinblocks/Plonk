package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class RegistryTileEntities {
    public static final TileEntityType<TilePlacedItems> placed_items = TileEntityType.Builder.of(TilePlacedItems::new, RegistryBlocks.placed_items).build(null);
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
        for (Field f : RegistryTileEntities.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == TileEntityType.class) {
                        ResourceLocation rl = new ResourceLocation(Plonk.MOD_ID, f.getName());
                        LOG.info(REGISTRIES, "Registering TileEntity: " + rl);
                        TileEntityType<?> type = (TileEntityType<?>) f.get(null);
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
