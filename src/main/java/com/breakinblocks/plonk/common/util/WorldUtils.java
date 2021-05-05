package com.breakinblocks.plonk.common.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

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
     * @see IBlockReader#getTileEntity(BlockPos)
     */
    @Nullable
    public static <TILE extends TileEntity> TILE getTileEntity(IBlockReader world, BlockPos pos, Class<TILE> clazz) {
        TileEntity tile = world.getTileEntity(pos);
        if (clazz.isInstance(tile)) {
            return clazz.cast(tile);
        }
        return null;
    }

    /**
     * Run if the tile is present and matching.
     *
     * @param consumer Immediately accepts the tile.
     * @see WorldUtils#getTileEntity(IBlockReader, BlockPos, Class)
     */
    public static <TILE extends TileEntity> void withTile(IBlockReader world, BlockPos pos, Class<TILE> clazz, Consumer<TILE> consumer) {
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
     * @see WorldUtils#getTileEntity(IBlockReader, BlockPos, Class)
     */
    public static <TILE extends TileEntity, RETURN> RETURN withTile(IBlockReader world, BlockPos pos, Class<TILE> clazz, Function<TILE, RETURN> func, Supplier<RETURN> def) {
        TILE tile = getTileEntity(world, pos, clazz);
        if (tile != null) {
            return func.apply(tile);
        }
        return def.get();
    }
}
