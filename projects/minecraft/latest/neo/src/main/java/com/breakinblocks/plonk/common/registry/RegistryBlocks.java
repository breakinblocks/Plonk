package com.breakinblocks.plonk.common.registry;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RegistryBlocks {
    /**
     * @see Blocks#GLASS
     */
    public static final BlockPlacedItems placed_items = new BlockPlacedItems(BlockBehaviour.Properties
            .of()
            .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Plonk.MOD_ID, "placed_items")))
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
                        Identifier id = Identifier.fromNamespaceAndPath(Plonk.MOD_ID, f.getName());
                        LOG.info("Registering Block: {}", id);
                        Block block = (Block) f.get(null);
                        helper.register(id, block);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
