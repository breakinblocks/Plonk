package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.resources.Identifier;

public class DataGenUtils {
    public static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    public static Identifier plonk(String path) {
        return Identifier.fromNamespaceAndPath(Plonk.MOD_ID, path);
    }

    public static Identifier carryOn(String path) {
        return Identifier.fromNamespaceAndPath(Plonk.CARRY_ON_MOD_ID, path);
    }
}
