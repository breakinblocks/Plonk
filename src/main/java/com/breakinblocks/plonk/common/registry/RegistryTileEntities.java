package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryTileEntities {
    public static final Class<TilePlacedItems> placed_items = TilePlacedItems.class;

    public static void init() {
        for (Field f : RegistryTileEntities.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == Class.class) {
                        Class<?> clazz = (Class<?>) f.get(null);
                        if (TileEntity.class.isAssignableFrom(clazz)) {
                            Class<TileEntity> tileClass = (Class<TileEntity>) clazz;
                            ResourceLocation r = new ResourceLocation(Plonk.MOD_ID, f.getName());
                            Plonk.LOG.info("Registering TileEntity: " + r);
                            GameRegistry.registerTileEntity(tileClass, r);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
