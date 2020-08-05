package com.breakinblocks.plonk.common.util.bound;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BoxCollection {
    private final ArrayList<Entry> boxes;
    private final ArrayList<Entry> collisionBoxes;
    private final ArrayList<Entry> selectionBoxes;
    private final Box renderBox;
    private final VoxelShape shape;
    private final VoxelShape collisionShape;
    private final VoxelShape selectionShape;
    public Entry selectionLastEntry = null;
    public AxisAlignedBB selectionLastAABB = null;
    private Entry boundsEntry = null;

    private BoxCollection(ArrayList<Entry> boxes) {
        this.boxes = new ArrayList<>(boxes);
        this.collisionBoxes = this.boxes.stream().filter(entry -> entry.collision).collect(Collectors.toCollection(ArrayList::new));
        this.selectionBoxes = this.boxes.stream().filter(entry -> entry.selection).collect(Collectors.toCollection(ArrayList::new));
        this.renderBox = this.boxes.stream().map(entry -> entry.box).reduce(Box::enclosing).orElse(Box.BLOCK_BOX);
        this.shape = this.boxes.stream().map(entry -> entry.box.toShape()).reduce(VoxelShapes::or).orElseGet(VoxelShapes::empty);
        this.collisionShape = this.collisionBoxes.stream().map(entry -> entry.box.toShape()).reduce(VoxelShapes::or).orElseGet(VoxelShapes::empty);
        this.selectionShape = this.selectionBoxes.stream().map(entry -> entry.box.toShape()).reduce(VoxelShapes::or).orElseGet(VoxelShapes::empty);
    }

    /**
     * The number of boxes in the collection
     *
     * @return number of boxes
     */
    public int size() {
        return boxes.size();
    }

    /**
     * Get boxes registered under the given index
     *
     * @param id of the boxes to retrieve
     * @return boxes that have the given index
     */
    public ArrayList<Box> get(int id) {
        return boxes.stream().filter((e) -> e.id == id).map(e -> e.box).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets the selection id of the first box that contains the given hit vector otherwise -1
     *
     * @param hitVec position of the ray trace hit relative to the box collection origin
     * @return selection id
     */
    public int getSelectionIndexFromHitVec(Vector3d hitVec) {
        return selectionBoxes.stream()
                .map((e) -> new SelectionEntry(e, e.box.distanceSq(hitVec)))
                .reduce((a, b) -> b.distance < a.distance ? b : a)
                .map((se) -> se.entry.id).orElse(-1);
    }

    public void addCollidingBoxes(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes) {
        for (Entry entry : collisionBoxes) {
            if (entry.collision) {
                AxisAlignedBB blockBox = entry.box.toAABB().offset(pos);
                if (blockBox.intersects(entityBox)) {
                    collidingBoxes.add(blockBox);
                }
            }
        }
    }

    // BLOCK METHODS

    /**
     * Return the last non-colliding + colliding bounding box
     */
    public AxisAlignedBB getSelectedBoundingBoxFromPool() {
        return selectionLastAABB;
    }

    public RayTraceResult collisionRayTrace(Block block, ICollisionRayTrace collisionRayTrace, BlockState blockState, World worldIn, BlockPos pos, Vector3d start, Vector3d end) {
        int num = this.boxes.size();
        RayTraceResult[] mops = new RayTraceResult[num];

        for (int i = 0; i < num; i++) {
            boundsEntry = boxes.get(i);
            mops[i] = collisionRayTrace.apply(blockState, worldIn, pos, start, end);
        }
        boundsEntry = null;

        // Return closest
        int nearestMopIndex = -1;
        double minDistSq = Double.POSITIVE_INFINITY;

        for (int i = 0; i < num; i++) {
            RayTraceResult mop = mops[i];
            if (mop == null) continue;
            double distSq = mop.getHitVec().squareDistanceTo(start);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearestMopIndex = i;
            }
        }

        if (nearestMopIndex >= 0) {
            selectionLastEntry = boxes.get(nearestMopIndex);
            selectionLastAABB = selectionLastEntry.box.toAABB().offset(pos);
        } else {
            selectionLastEntry = null;
            selectionLastAABB = null;
        }
        return nearestMopIndex < 0 ? null : mops[nearestMopIndex];
    }

    public AxisAlignedBB getBoundingBox(BlockPos pos) {
        if (boundsEntry != null)
            return boundsEntry.box.toAABB();
        return renderBox.toAABB();
    }

    public VoxelShape getShape() {
        return shape;
    }

    public VoxelShape getCollisionShape() {
        return collisionShape;
    }

    public VoxelShape getSelectionShape() {
        return selectionShape;
    }

    @FunctionalInterface
    public interface ICollisionRayTrace {
        RayTraceResult apply(BlockState blockState, World worldIn, BlockPos pos, Vector3d start, Vector3d end);
    }

    @FunctionalInterface
    public interface IAddCollisionBoxesToList {
        void apply(World world, int x, int y, int z, AxisAlignedBB collider, List collisions, Entity entity);
    }

    private static class SelectionEntry {
        public final Entry entry;
        public final double distance;

        public SelectionEntry(Entry entry, double distance) {
            this.entry = entry;
            this.distance = distance;
        }
    }

    public static class Entry {
        public final int id;
        public final boolean collision;
        public final boolean selection;
        public final Box box;

        public Entry(int id, boolean collision, boolean selection, Box box) {
            this.id = id;
            this.collision = collision;
            this.selection = selection;
            this.box = box;
        }
    }

    public static class Builder {
        private ArrayList<Entry> boxes = new ArrayList<>();
        private boolean collision;
        private boolean selection;

        public Builder(boolean collision, boolean selection) {
            this.collision = collision;
            this.selection = selection;
        }

        public Builder() {
            this(true, true);
        }

        public Builder setCollision(boolean collision) {
            this.collision = collision;
            return this;
        }

        public Builder setSelection(boolean selection) {
            this.selection = selection;
            return this;
        }

        public Builder addBox(int id, Box box) {
            boxes.add(new Entry(id, collision, selection, box));
            return this;
        }

        public Builder addBox(int id, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            return this.addBox(id, new Box(minX, minY, minZ, maxX, maxY, maxZ));
        }

        public Builder addBoxes(BoxCollection boxCollection) {
            boxes.addAll(boxCollection.boxes);
            return this;
        }

        public Builder apply(Function<Box, Box> transform) {
            this.boxes = boxes.stream()
                    .map((e) -> new Entry(e.id, e.collision, e.selection, transform.apply(e.box)))
                    .collect(Collectors.toCollection(ArrayList::new));
            return this;
        }

        public BoxCollection build() {
            BoxCollection boxCollection = new BoxCollection(boxes);
            return boxCollection;
        }
    }
}
