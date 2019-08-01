package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryBlocks {
    public static final BlockPlacedItems placed_items = new BlockPlacedItems();

    public static void init() {
        for (Field f : RegistryBlocks.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Block.class.isAssignableFrom(f.getType())) {
                        String name = f.getName();
                        Plonk.log.info("Registering Block: " + name);
                        Block block = (Block) f.get(null);
                        block.setBlockName(name);
                        block.setBlockTextureName(Plonk.MOD_ID + ":" + name);
                        GameRegistry.registerBlock(block, f.getName());
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
