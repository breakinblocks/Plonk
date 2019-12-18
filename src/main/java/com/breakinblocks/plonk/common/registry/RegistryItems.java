package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.item.ItemBlockPlacedItems;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryItems {
    public static final ItemBlockPlacedItems placed_items = new ItemBlockPlacedItems();

    public static void init(RegistryEvent.Register<Item> event) {
        for (Field f : RegistryItems.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Item.class.isAssignableFrom(f.getType())) {
                        String name = f.getName();
                        Plonk.LOG.info("Registering Item: " + name);
                        Item item = (Item) f.get(null);
                        item.setTranslationKey(name);
                        item.setRegistryName(Plonk.MOD_ID, name);
                        event.getRegistry().register(item);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
