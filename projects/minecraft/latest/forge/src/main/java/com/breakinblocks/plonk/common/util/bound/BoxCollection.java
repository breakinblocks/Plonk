package com.breakinblocks.plonk.common.util.bound;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BoxCollection {
    private final Entries boxes;
    private final Map<Integer, Entries> boxesById;

    private BoxCollection(ArrayList<Entry> boxes) {
        this.boxes = new Entries(boxes);
        this.boxesById = this.boxes.source.get().stream().collect(Collectors.groupingBy(
                e -> e.id,
                Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), Entries::new))
        );
    }

    /**
     * Get boxes registered under the given index
     *
     * @param id of the boxes to retrieve
     * @return boxes that have the given index
     */
    public ArrayList<Box> get(int id) {
        return this.boxesById.getOrDefault(id, Entries.EMPTY).all.get();
    }

    /**
     * Gets the selection id of the first box that contains the given hit vector otherwise -1
     *
     * @param hitVec position of the ray trace hit relative to the box collection origin
     * @return selection id
     */
    public int getSelectionIndexFromHitVec(Vec3 hitVec) {
        return boxes.source.get().stream()
                .filter(e -> e.selection)
                .map(e -> new SelectionEntry(e, e.box.distanceSq(hitVec)))
                .reduce((a, b) -> b.distance < a.distance ? b : a)
                .map(se -> se.entry.id).orElse(-1);
    }

    // BLOCK METHODS

    public VoxelShape getShape() {
        return this.boxes.shape.get();
    }

    public VoxelShape getCollisionShape() {
        return this.boxes.collisionShape.get();
    }

    public VoxelShape getSelectionShape() {
        return this.boxes.selectionShape.get();
    }

    public VoxelShape getSelectionShapeById(int id) {
        return this.boxesById.getOrDefault(id, Entries.EMPTY).selectionShape.get();
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

    public static class Entries {
        public static final Entries EMPTY = new Entries(new ArrayList<>());
        public final Lazy<ArrayList<Entry>> source;
        public final Lazy<ArrayList<Box>> all;
        public final Lazy<ArrayList<Box>> collision;
        public final Lazy<ArrayList<Box>> selection;
        public final Lazy<VoxelShape> shape;
        public final Lazy<VoxelShape> collisionShape;
        public final Lazy<VoxelShape> selectionShape;

        public Entries(ArrayList<Entry> entries) {
            this.source = Lazy.of(() -> new ArrayList<>(entries));
            this.all = Lazy.of(() -> this.source.get().stream().map(e -> e.box).collect(Collectors.toCollection(ArrayList::new)));
            this.collision = Lazy.of(() -> this.source.get().stream().filter(e -> e.collision).map(e -> e.box).collect(Collectors.toCollection(ArrayList::new)));
            this.selection = Lazy.of(() -> this.source.get().stream().filter(e -> e.selection).map(e -> e.box).collect(Collectors.toCollection(ArrayList::new)));
            this.shape = Lazy.of(() -> this.all.get().stream().map(Box::toShape).reduce(Shapes::or).orElseGet(Shapes::empty));
            this.collisionShape = Lazy.of(() -> this.collision.get().stream().map(Box::toShape).reduce(Shapes::or).orElseGet(Shapes::empty));
            this.selectionShape = Lazy.of(() -> this.selection.get().stream().map(Box::toShape).reduce(Shapes::or).orElseGet(Shapes::empty));
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
            boxes.addAll(boxCollection.boxes.source.get());
            return this;
        }

        public Builder apply(Function<Box, Box> transform) {
            this.boxes = boxes.stream()
                    .map((e) -> new Entry(e.id, e.collision, e.selection, transform.apply(e.box)))
                    .collect(Collectors.toCollection(ArrayList::new));
            return this;
        }

        public BoxCollection build() {
            return new BoxCollection(boxes);
        }
    }
}
