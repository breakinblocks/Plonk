package com.breakinblocks.plonk.data;

import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Function;

import static com.breakinblocks.plonk.data.DataGenUtils.minecraft;
import static com.breakinblocks.plonk.data.DataGenUtils.plonk;

public class BlockStates extends net.minecraftforge.client.model.generators.BlockStateProvider {
    public BlockStates(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    /**
     * @see BlockStateProvider#directionalBlock(Block, Function, int)
     */
    @Override
    protected void registerStatesAndModels() {
        final BlockModelBuilder placed_items_model = models().withExistingParent("placed_items", minecraft("block/block"))
                .texture("all", plonk("block/placed_items"))
                .texture("particle", "#all")
                .element()
                .from(0, -0.01f, 0)
                .to(16, 0.01f, 16)
                .face(Direction.UP).texture("#all").end()
                .face(Direction.DOWN).uvs(0, 16, 16, 0).texture("#all").cullface(Direction.DOWN).end()
                .end();
        getVariantBuilder(RegistryBlocks.placed_items)
                .forAllStates(state -> {
                    Direction dir = state.get(BlockStateProperties.FACING);
                    return ConfiguredModel.builder()
                            .modelFile(placed_items_model)
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 270 : 0)
                            .rotationY(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? ((int) dir.getHorizontalAngle()) % 360 : 0)
                            .build();
                });
        itemModels().getBuilder("placed_items")
                .parent(placed_items_model);
    }
}