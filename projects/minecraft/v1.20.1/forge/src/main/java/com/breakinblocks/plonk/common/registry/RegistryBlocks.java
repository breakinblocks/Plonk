package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class RegistryBlocks {
    /**
     * @see Blocks#GLASS
     */
    public static final BlockPlacedItems placed_items = new BlockPlacedItems(BlockBehaviour.Properties
            .of()
            .strength(0.3F)
            .sound(SoundType.STONE)
            .noOcclusion()
            // Don't think the following are needed but... eh
            .isValidSpawn((state, world, pos, type) -> false)
            .isRedstoneConductor((state, world, pos) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false)
    );
    private static final Logger LOG = LogManager.getLogger();

    public static void init(RegisterEvent.RegisterHelper<Block> helper) {
        for (Field f : RegistryBlocks.class.getDeclaredFields()) {
            try {
                if (Modifier.isStatic(f.getModifiers())) {
                    if (Block.class.isAssignableFrom(f.getType())) {
                        ResourceLocation rl = new ResourceLocation(Plonk.MOD_ID, f.getName());
                        LOG.info(REGISTRIES, "Registering Block: " + rl);
                        Block block = (Block) f.get(null);
                        helper.register(rl, block);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
