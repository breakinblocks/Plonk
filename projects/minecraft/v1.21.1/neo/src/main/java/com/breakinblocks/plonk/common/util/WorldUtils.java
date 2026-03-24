package com.breakinblocks.plonk.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class WorldUtils {
    /**
     * Get tile entity of the given class at the location or return null if not present or mis-matched.
     *
     * @param clazz Tile entity should be instance of this class.
     * @return The tile entity or null.
     * @see BlockGetter#getBlockEntity(BlockPos)
     */
    @Nullable
    public static <TILE extends BlockEntity> TILE getTileEntity(BlockGetter world, BlockPos pos, Class<TILE> clazz) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (clazz.isInstance(tile)) {
            return clazz.cast(tile);
        }
        return null;
    }

    /**
     * Run if the tile is present and matching.
     *
     * @param consumer Immediately accepts the tile.
     * @see WorldUtils#getTileEntity(BlockGetter, BlockPos, Class)
     */
    public static <TILE extends BlockEntity> void withTile(BlockGetter world, BlockPos pos, Class<TILE> clazz, Consumer<TILE> consumer) {
        TILE tile = getTileEntity(world, pos, clazz);
        if (tile != null) {
            consumer.accept(tile);
        }
    }

    /**
     * Apply if the tile is present and matching.
     *
     * @param func Immediately applied to the tile.
     * @param def  Default value supplier.
     * @return Result of applying the function to the tile or default if not present or mis-matched.
     * @see WorldUtils#getTileEntity(BlockGetter, BlockPos, Class)
     */
    public static <TILE extends BlockEntity, RETURN> RETURN withTile(BlockGetter world, BlockPos pos, Class<TILE> clazz, Function<TILE, RETURN> func, Supplier<RETURN> def) {
        TILE tile = getTileEntity(world, pos, clazz);
        if (tile != null) {
            return func.apply(tile);
        }
        return def.get();
    }
}
