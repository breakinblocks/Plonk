package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.resources.ResourceLocation;

public class DataGenUtils {
    public static ResourceLocation minecraft(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    public static ResourceLocation plonk(String path) {
        return ResourceLocation.fromNamespaceAndPath(Plonk.MOD_ID, path);
    }

    public static ResourceLocation carryOn(String path) {
        return ResourceLocation.fromNamespaceAndPath(Plonk.CARRY_ON_MOD_ID, path);
    }
}
