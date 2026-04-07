package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

import javax.annotation.Nonnull;

public class Models extends ModelProvider {
    public Models(PackOutput output) {
        super(output, Plonk.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(RegistryBlocks.placed_items);
    }

    @Nonnull
    @Override
    public String getName() {
        return Plonk.NAME + " Block States";
    }
}
