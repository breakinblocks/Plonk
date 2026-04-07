package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.item.ItemBlockPlacedItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryItems {
    public static final ItemBlockPlacedItems placed_items = new ItemBlockPlacedItems(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Plonk.MOD_ID, "placed_items")))
            .useBlockDescriptionPrefix());
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegisterEvent.RegisterHelper<Item> helper) {
        for (Field f : RegistryItems.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Item.class.isAssignableFrom(f.getType())) {
                        Identifier id = Identifier.fromNamespaceAndPath(Plonk.MOD_ID, f.getName());
                        LOG.info("Registering Item: {}", id);
                        Item item = (Item) f.get(null);
                        helper.register(id, item);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
