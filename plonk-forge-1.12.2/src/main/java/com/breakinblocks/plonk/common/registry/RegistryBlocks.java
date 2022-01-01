package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryBlocks {
    public static final BlockPlacedItems placed_items = new BlockPlacedItems();

    public static void init(RegistryEvent.Register<Block> event) {
        for (Field f : RegistryBlocks.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Block.class.isAssignableFrom(f.getType())) {
                        String name = f.getName();
                        Plonk.LOG.info("Registering Block: " + name);
                        Block block = (Block) f.get(null);
                        block.setTranslationKey(name);
                        block.setRegistryName(Plonk.MOD_ID, name);
                        event.getRegistry().register(block);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
