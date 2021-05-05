package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

import javax.annotation.Nullable;

import static com.breakinblocks.plonk.data.DataGenUtils.carryOn;

public class ItemTags extends ItemTagsProvider {
    @SuppressWarnings("unused")
    public ItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator);
    }

    @Override
    protected void registerTags() {
        TagCollection<Item> collection = net.minecraft.tags.ItemTags.getCollection();
        getBuilder(PlonkTags.Items.UNPLACEABLE)
                .add(RegistryItems.placed_items)
                .addOptional(collection, carryOn("entity_item"))
                .addOptional(collection, carryOn("tile_item"))
        ;
    }

    @Override
    public String getName() {
        return Plonk.NAME + " Item Tags";
    }
}
