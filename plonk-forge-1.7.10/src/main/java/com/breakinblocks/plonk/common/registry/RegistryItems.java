package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.PlonkConstants;
import com.breakinblocks.plonk.common.item.ItemBlockPlacedItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryItems {
    public static final ItemBlockPlacedItems placed_items = new ItemBlockPlacedItems();

    public static void init() {
        for (Field f : RegistryItems.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Item.class.isAssignableFrom(f.getType())) {
                        String name = f.getName();
                        Plonk.LOG.info("Registering Item: " + name);
                        Item item = (Item) f.get(null);
                        item.setUnlocalizedName(name);
                        item.setTextureName(PlonkConstants.MOD_ID + ":" + name);
                        GameRegistry.registerItem(item, name);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
