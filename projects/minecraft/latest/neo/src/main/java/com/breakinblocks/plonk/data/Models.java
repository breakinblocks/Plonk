package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryItems;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import javax.annotation.Nonnull;

public class Models extends ModelProvider {
    public Models(PackOutput output) {
        super(output, Plonk.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Block placed_items = RegistryBlocks.placed_items;
        ExtendedModelTemplate template_placed_items = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(e -> e
                        .from(0, -0.01f, 0)
                        .to(16, 0.01f, 16)
                        .face(Direction.UP, f -> f.texture(TextureSlot.ALL))
                        .face(Direction.DOWN, f -> f
                                .uvs(0, 16, 16, 0)
                                .texture(TextureSlot.ALL)
                                .cullface(Direction.DOWN)))
                .build();
        Identifier placed_items_model = template_placed_items.create(placed_items,
                new TextureMapping()
                        .put(TextureSlot.ALL, TextureMapping.getBlockTexture(RegistryBlocks.placed_items))
                        .copySlot(TextureSlot.ALL, TextureSlot.PARTICLE),
                blockModels.modelOutput);
        Variant placed_items_variant = new Variant(placed_items_model);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(placed_items, BlockModelGenerators.variant(placed_items_variant))
                .with(getPlacedItemsPropertyDispatch()));
        itemModels.itemModelOutput.accept(RegistryItems.placed_items, ItemModelUtils.plainModel(placed_items_model));
    }

    private PropertyDispatch<VariantMutator> getPlacedItemsPropertyDispatch() {
        PropertyDispatch.C2<VariantMutator, Direction, Boolean> dispatch = PropertyDispatch.modify(BlockPlacedItems.FACING, BlockPlacedItems.WATERLOGGED);
        for (Direction dir : Direction.values()) {
            dispatch.select(dir, false, getPlacedItemsVariantMutator(dir));
            dispatch.select(dir, true, getPlacedItemsVariantMutator(dir));
        }
        return dispatch;
    }

    private VariantMutator getPlacedItemsVariantMutator(Direction dir) {
        return (dir == Direction.DOWN ? BlockModelGenerators.X_ROT_180 : dir.getAxis().isHorizontal() ? BlockModelGenerators.X_ROT_270 : BlockModelGenerators.NOP)
                .then(dir == Direction.DOWN ? BlockModelGenerators.Y_ROT_180 : dir.getAxis().isHorizontal() ? VariantMutator.Y_ROT.withValue(getHorizontalQuadrant(dir)) : BlockModelGenerators.NOP);
    }

    private Quadrant getHorizontalQuadrant(Direction dir) {
        switch (dir) {
            case DOWN, UP, SOUTH -> {
                return Quadrant.R0;
            }
            case NORTH -> {
                return Quadrant.R180;
            }
            case WEST -> {
                return Quadrant.R90;
            }
            case EAST -> {
                return Quadrant.R270;
            }
            default -> throw new IllegalStateException("Unexpected value: " + dir);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return Plonk.NAME + " Block States";
    }
}
