package com.breakinblocks.plonk.common.registry;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.stream.Stream;

public class RegistryRecipes {
    public static void init() {
        Stream.of(0, 1).map(damage -> new ItemStack(Items.coal, 1, damage)).forEach(coal ->
                GameRegistry.addRecipe(new ShapedOreRecipe(RegistryItems.plonk_wand,
                        "  P",
                        " I ",
                        "C  ",
                        'P', "dyePink",
                        'I', "ingotIron",
                        'C', coal
                )));
    }
}
