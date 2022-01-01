package com.breakinblocks.plonk.common.util.bound;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Class for manipulating a box within the bounds of a block
 */
public class Box {

    public static final Box BLOCK_BOX = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

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
        return new AxisAlignedBB(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ
        );
    }

    public VoxelShape toShape() {
        return VoxelShapes.box(
                this.minX,
                this.minY,
                this.minZ,
                this.maxX,
                this.maxY,
                this.maxZ
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
     * Rotate 90 degrees anti-clockwise about the positive X axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 180 degrees about the positive X axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 270 degrees anti-clockwise about the positive X axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 90 degrees anti-clockwise about the positive Y axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 180 degrees about the positive Y axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 270 degrees anti-clockwise about the positive Y axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 90 degrees anti-clockwise about the positive Z axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 180 degrees about the positive Z axis (from origin 0.5, 0.5, 0.5)
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
     * Rotate 270 degrees anti-clockwise about the positive X axis (from origin 0.5, 0.5, 0.5)
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

    /**
     * Translates the whole bounding box by the given coordinates
     *
     * @param dx change in x
     * @param dy change in y
     * @param dz change in z
     * @return translated box
     */
    public Box translate(double dx, double dy, double dz) {
        return new Box(
                this.minX + dx,
                this.minY + dy,
                this.minZ + dz,
                this.maxX + dx,
                this.maxY + dy,
                this.maxZ + dz
        );
    }

    /**
     * Gets the box that encloses both the boxes
     *
     * @param o other box
     * @return enclosing box
     */
    public Box enclosing(Box o) {
        return new Box(
                Math.min(this.minX, o.minX),
                Math.min(this.minY, o.minY),
                Math.min(this.minZ, o.minZ),
                Math.max(this.maxX, o.maxX),
                Math.max(this.maxY, o.maxY),
                Math.max(this.maxZ, o.maxZ)
        );
    }

    /**
     * @see Box#contains(double, double, double)
     */
    public boolean contains(Vector3d vec) {
        return contains(vec.x, vec.y, vec.z);
    }

    /**
     * Checks if the given vector is contained within the box
     *
     * @return if the given vector is contained
     */
    public boolean contains(double x, double y, double z) {
        return this.minX <= x && x <= this.maxX && this.minY <= y && y <= this.maxY && this.minZ <= z && z <= this.minZ;
    }

    /**
     * @see Box#distanceSq(double, double, double)
     */
    public double distanceSq(Vector3d vec) {
        return distanceSq(vec.x, vec.y, vec.z);
    }

    /**
     * Calculates the squared distance of the given vector to the box
     *
     * @return 0 if it is within the box, otherwise the distance to the box
     */
    public double distanceSq(double x, double y, double z) {
        double dx = Math.max(0, Math.max(this.minX - x, x - this.maxX));
        double dy = Math.max(0, Math.max(this.minY - y, y - this.maxY));
        double dz = Math.max(0, Math.max(this.minZ - z, z - this.maxZ));
        return dx * dx + dy * dy + dz * dz;
    }
}
