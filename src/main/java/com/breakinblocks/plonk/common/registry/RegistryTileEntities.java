package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.tileentity.TileEntity;

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
                            String name = f.getName();
                            Plonk.LOG.info("Registering TileEntity: " + name);
                            GameRegistry.registerTileEntity(tileClass, name);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
