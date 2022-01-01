package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

import javax.annotation.Nullable;

public class BlockTags extends BlockTagsProvider {
    @SuppressWarnings("unused")
    public BlockTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn);
    }

    @Override
    protected void addTags() {
    }

    @Override
    public String getName() {
        return Plonk.NAME + " Block Tags";
    }
}
