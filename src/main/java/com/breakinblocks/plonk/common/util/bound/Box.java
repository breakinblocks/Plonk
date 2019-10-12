package com.breakinblocks.plonk.common.util.bound;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;

/**
 * Class for manipulating a box within the bounds of a block
 */
public class Box {

    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    /**
     * Create box using the given bounds
     *
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     */
    public Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Create box from AABB
     *
     * @param aabb Minecraft AABB
     */
    public Box(AxisAlignedBB aabb) {
        this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    /**
     * Convert to AABB
     *
     * @return Minecraft AABB
     */
    public AxisAlignedBB toAABB() {
        return AxisAlignedBB.getBoundingBox(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ
        );
    }

    /**
     * Set block bounds to match this
     *
     * @param block to update
     */
    public void setBlockBounds(Block block) {
        block.setBlockBounds(
                (float) this.minX,
                (float) this.minY,
                (float) this.minZ,
                (float) this.maxX,
                (float) this.maxY,
                (float) this.maxZ
        );
    }

    /**
     * Makes a copy
     *
     * @return copied bounding box
     */
    public Box copy() {
        return new Box(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ
        );
    }

    /**
     * Rotate 90 degrees anti-clockwise about the positive X axis
     *
     * @return rotated bounding box
     */
    public Box rotateX90() {
        return new Box(
                this.minX,
                1.0 - this.maxZ,
                this.minY,
                this.maxX,
                1.0 - this.minZ,
                this.maxY
        );
    }

    /**
     * Rotate 180 degrees about the positive X axis
     *
     * @return rotated bounding box
     */
    public Box rotateX180() {
        return new Box(
                this.minX,
                1.0 - this.maxY,
                1.0 - this.maxZ,
                this.maxX,
                1.0 - this.minY,
                1.0 - this.minZ
        );
    }

    /**
     * Rotate 270 degrees anti-clockwise about the positive X axis
     *
     * @return rotated bounding box
     */
    public Box rotateX270() {
        return new Box(
                this.minX,
                this.minZ,
                1.0 - this.maxY,
                this.maxX,
                this.maxZ,
                1.0 - this.minY
        );
    }

    /**
     * Rotate 90 degrees anti-clockwise about the positive Y axis
     *
     * @return rotated bounding box
     */
    public Box rotateY90() {
        return new Box(
                this.minZ,
                this.minY,
                1.0 - this.maxX,
                this.maxZ,
                this.maxY,
                1.0 - this.minX
        );
    }

    /**
     * Rotate 180 degrees about the positive Y axis
     *
     * @return rotated bounding box
     */
    public Box rotateY180() {
        return new Box(
                1.0 - this.maxX,
                this.minY,
                1.0 - this.maxZ,
                1.0 - this.minX,
                this.maxY,
                1.0 - this.minZ
        );
    }

    /**
     * Rotate 270 degrees anti-clockwise about the positive Y axis
     *
     * @return rotated bounding box
     */
    public Box rotateY270() {
        return new Box(
                1.0 - this.maxZ,
                this.minY,
                this.minX,
                1.0 - this.minZ,
                this.maxY,
                this.maxX
        );
    }

    /**
     * Rotate 90 degrees anti-clockwise about the positive Z axis
     *
     * @return rotated bounding box
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public Box rotateZ90() {
        return new Box(
                1.0 - this.maxY,
                this.minX,
                this.minZ,
                1.0 - this.minY,
                this.maxX,
                this.maxZ
        );
    }

    /**
     * Rotate 180 degrees about the positive Z axis
     *
     * @return rotated bounding box
     */
    public Box rotateZ180() {
        return new Box(
                1.0 - this.maxX,
                1.0 - this.maxY,
                this.minZ,
                1.0 - this.minX,
                1.0 - this.minY,
                this.maxZ
        );
    }

    /**
     * Rotate 270 degrees anti-clockwise about the positive X axis
     *
     * @return rotated bounding box
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public Box rotateZ270() {
        return new Box(
                this.minY,
                1.0 - this.maxX,
                this.minZ,
                this.maxY,
                1.0 - this.minX,
                this.maxZ
        );
    }
}
