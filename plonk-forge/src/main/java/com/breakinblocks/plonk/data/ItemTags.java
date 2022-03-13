package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import static com.breakinblocks.plonk.data.DataGenUtils.carryOn;

public class ItemTags extends ItemTagsProvider {
    public ItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, Plonk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        m_206424_(PlonkTags.Items.UNPLACEABLE)
                .add(RegistryItems.placed_items)
                .addOptional(carryOn("entity_item"))
                .addOptional(carryOn("tile_item"))
        ;
    }

    @Override
    public String getName() {
        return Plonk.NAME + " Item Tags";
    }
}
