package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.breakinblocks.plonk.common.tag.PlonkTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static com.breakinblocks.plonk.data.DataGenUtils.carryOn;

public class ItemTags extends ItemTagsProvider {
    public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, Plonk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(PlonkTags.Items.UNPLACEABLE)
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
