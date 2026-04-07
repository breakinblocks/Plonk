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
        // TODO: Port this.
//        final BlockModelBuilder placed_items_model = models().withExistingParent("placed_items", minecraft("block/block"))
//                .renderType("cutout")
//                .texture("all", plonk("block/placed_items"))
//                .texture("particle", "#all")
//                .element()
//                .from(0, -0.01f, 0)
//                .to(16, 0.01f, 16)
//                .face(Direction.UP).texture("#all").end()
//                .face(Direction.DOWN).uvs(0, 16, 16, 0).texture("#all").cullface(Direction.DOWN).end()
//                .end();
//        getVariantBuilder(RegistryBlocks.placed_items)
//                .forAllStates(state -> {
//                    Direction dir = state.getValue(BlockStateProperties.FACING);
//                    return ConfiguredModel.builder()
//                            .modelFile(placed_items_model)
//                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 270 : 0)
//                            .rotationY(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? ((int) dir.toYRot()) % 360 : 0)
//                            .build();
//                });
//        itemModels().getBuilder("placed_items")
//                .parent(placed_items_model);
    }

    @Nonnull
    @Override
    public String getName() {
        return Plonk.NAME + " Block States";
    }
}
