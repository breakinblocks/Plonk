package com.breakinblocks.plonk.common.tile;

import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.bound.Box;
import com.breakinblocks.plonk.common.util.bound.BoxCollection;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class TilePlacedItems extends BlockEntity implements WorldlyContainer {

    public static final int Tag_VERSION = 1;

    public static final DirectionProperty FACING = BlockPlacedItems.FACING;
    public static final float HEIGHT_PLATE = 1.0f / 32f;
    public static final float HEIGHT_ITEM = 1.0f / 16f * 1.5f;
    public static final float HEIGHT_BLOCK = 1.0f / 2f;
    public static final float BLOCK_PADDING_PERCENTAGE = 0.0f; // 0.125f for a one pixel padding
    public static final float BLOCK_PADDING_AMOUNT = HEIGHT_BLOCK * BLOCK_PADDING_PERCENTAGE;
    // Box placed in the centre
    public static final Box BOX_BLOCK = new Box(
            0.5 - HEIGHT_BLOCK / 2 + BLOCK_PADDING_AMOUNT,
            0.0,
            0.5 - HEIGHT_BLOCK / 2 + BLOCK_PADDING_AMOUNT,
            0.5 + HEIGHT_BLOCK / 2 - BLOCK_PADDING_AMOUNT,
            HEIGHT_BLOCK - BLOCK_PADDING_AMOUNT * 2,
            0.5 + HEIGHT_BLOCK / 2 - BLOCK_PADDING_AMOUNT
    );

    public static final Box BOX_ITEM_ONE = new Box(
            0.0,
            0.0,
            0.0,
            1.0,
            HEIGHT_ITEM,
            1.0
    );

    public static final Box BOX_ITEM_MANY = new Box(
            0.25,
            0.0,
            0.25,
            0.75,
            HEIGHT_ITEM,
            0.75
    );
    public static final String TAG_VERSION = "Version";
    public static final String TAG_TILE_ROTATION = "TileRotation";
    public static final int TILE_ROTATION_COUNT = 4;
    public static final String TAG_ITEM_ROTATION = "ItemRotation";
    public static final String TAG_ITEMS = "Items";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_RENDER_TYPE = "RenderType";
    public static final int RENDER_TYPE_BLOCK = 1;
    public static final int RENDER_TYPE_ITEM = 0;
    public static final int ITEM_ROTATION_COUNT = 16;
    private static final Logger LOG = LogManager.getLogger();

    boolean needsCleaning = true;
    private int tileRotation = 0;
    private NonNullList<ItemStack> contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    private ItemMeta[] contentsMeta = new ItemMeta[this.getContainerSize()];
    private ItemStack[] contentsDisplay = new ItemStack[0];
    private BoxCollection contentsBoxes = new BoxCollection.Builder()
            .addBox(0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            .build();

    protected TilePlacedItems(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(this.contentsMeta, ItemMeta.DEFAULT);
    }

    public TilePlacedItems(BlockPos pos, BlockState state) {
        this(RegistryTileEntities.placed_items, pos, state);
    }

    public static void clientTick(Level level, BlockPos pPos, BlockState pState, TilePlacedItems pBlockEntity) {
        pBlockEntity.clientTick();
    }

    public static void serverTick(Level level, BlockPos pPos, BlockState pState, TilePlacedItems pBlockEntity) {
        pBlockEntity.serverTick();
    }

    /**
     * Clean the tile by compacting it and such, also updates contentsDisplay
     * Does not mark dirty itself
     *
     * @return true if there was a change
     */
    public boolean clean() {
        // Check if needs changes
        int first_empty = -1;
        int last_not_empty = -1;
        int count = 0;
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).isEmpty()) {
                if (first_empty == -1) {
                    first_empty = i;
                }
            } else {
                last_not_empty = i;
                count++;
            }
        }

        // If empty tile
        if (last_not_empty == -1) {
            Objects.requireNonNull(this.level);
            this.level.removeBlock(this.worldPosition, false);
            return true;
        }

        // If the contents to display are up to date
        if (contentsDisplay.length == count) {
            // If there is no empty space
            if (first_empty == -1) return false;
            // If there is empty space only in front of a non-empty space then nothing can be moved
            if (first_empty > last_not_empty) return false;
        }

        boolean shifted = updateContents();

        updateContentsDisplay();

        return shifted;
    }

    /**
     * Shift empty spaces to the end
     *
     * @return true if anything was shifted
     */
    private boolean updateContents() {
        boolean changed = false;
        for (int i = 0; i < contents.size() - 1; i++) {
            if (contents.get(i).isEmpty()) {
                // If the slot is empty, try move any non-empty stacks in front of it to the slot
                for (int j = i + 1; j < contents.size(); j++) {
                    if (!contents.get(j).isEmpty()) {
                        contents.set(i, contents.get(j));
                        // Also update the hitbox
                        contentsMeta[i] = contentsMeta[j];
                        contentsMeta[j] = ItemMeta.DEFAULT;
                        contents.set(j, ItemStack.EMPTY);
                        changed = true;
                        break;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Update the array used for display and rendering and the hit boxes
     */
    private void updateContentsDisplay() {
        int count = 0;
        for (int i = 0; i < contents.size(); i++) {
            if (!contents.get(i).isEmpty()) {
                count = i + 1;
            }
        }
        contentsDisplay = contents.stream().limit(count).toArray(ItemStack[]::new);

        updateContentsBoxes(count);
    }

    private Box getBox(int count, int renderType) {
        switch (renderType) {
            case RENDER_TYPE_BLOCK:
                return BOX_BLOCK;
            case RENDER_TYPE_ITEM:
            default:
                return count == 1 ? BOX_ITEM_ONE : BOX_ITEM_MANY;
        }
    }

    private void updateContentsBoxes(int count) {
        Objects.requireNonNull(this.level);
        BoxCollection.Builder builder = new BoxCollection.Builder(false, true);

        switch (count) {
            case 1:
                builder.addBox(1, getBox(count, contentsMeta[0].renderType));
                break;
            case 2:
                builder.addBox(1, getBox(count, contentsMeta[0].renderType).translate(-0.25, 0, 0));
                builder.addBox(2, getBox(count, contentsMeta[1].renderType).translate(0.25, 0, 0));
                break;
            case 4:
                builder.addBox(4, getBox(count, contentsMeta[3].renderType).translate(0.25, 0, 0.25));
            case 3:
                builder.addBox(1, getBox(count, contentsMeta[0].renderType).translate(-0.25, 0, -0.25));
                builder.addBox(2, getBox(count, contentsMeta[1].renderType).translate(0.25, 0, -0.25));
                builder.addBox(3, getBox(count, contentsMeta[2].renderType).translate(-0.25, 0, 0.25));
                break;
            default:
                builder.addBox(0, 0.0, 0.0, 0.0, 1.0, HEIGHT_PLATE, 1.0);
        }

        // Apply Tile Rotation clockwise
        switch (tileRotation % TILE_ROTATION_COUNT) {
            case 0: // None
                break;
            case 1: // 90
                builder.apply(Box::rotateY270);
                break;
            case 2: // 180
                builder.apply(Box::rotateY180);
                break;
            case 3: // 270
                builder.apply(Box::rotateY90);
                break;
        }

        // Apply Facing
        Direction facing = this.level.getBlockState(this.getBlockPos()).getValue(FACING);

        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                builder.apply(Box::rotateZ180);
                break;
            case SOUTH: // NORTH
                builder.apply(Box::rotateX90);
                break;
            case NORTH: // SOUTH
                builder.apply(Box::rotateX90).apply(Box::rotateY180);
                break;
            case EAST: // WEST
                builder.apply(Box::rotateX90).apply(Box::rotateY90);
                break;
            case WEST: // EAST
                builder.apply(Box::rotateX90).apply(Box::rotateY270);
                break;
        }

        contentsBoxes = builder.build();
    }

    /**
     * @see ChestBlockEntity
     */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        TagUpgrader.upgrade(tag);
        this.tileRotation = tag.getInt(TAG_TILE_ROTATION);
        ListTag tagItems = tag.getList(TAG_ITEMS, 10);
        this.contents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        this.contentsMeta = new ItemMeta[this.getContainerSize()];
        Arrays.fill(this.contentsMeta, ItemMeta.DEFAULT);

        for (int i = 0; i < tagItems.size(); i++) {
            CompoundTag tagItem = tagItems.getCompound(i);
            int slot = tagItem.getByte(TAG_SLOT) & 255;
            int renderType = tagItem.getInt(TAG_RENDER_TYPE);
            int itemRotation = tagItem.getInt(TAG_ITEM_ROTATION);

            if (slot < this.contents.size()) {
                this.contents.set(slot, ItemStack.of(tagItem));
                this.contentsMeta[slot] = new ItemMeta(renderType, itemRotation);
            }
        }

        this.needsCleaning = true;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_VERSION, Tag_VERSION);
        tag.putInt(TAG_TILE_ROTATION, tileRotation);
        ListTag tagItems = new ListTag();

        for (int slot = 0; slot < this.contents.size(); slot++) {
            if (!this.contents.get(slot).isEmpty()) {
                CompoundTag tagItem = new CompoundTag();
                tagItem.putByte(TAG_SLOT, (byte) slot);
                tagItem.putInt(TAG_RENDER_TYPE, this.contentsMeta[slot].renderType);
                tagItem.putInt(TAG_ITEM_ROTATION, this.contentsMeta[slot].rotation);
                this.contents.get(slot).save(tagItem);
                tagItems.add(tagItem);
            }
        }

        tag.put(TAG_ITEMS, tagItems);
    }

    public void clientTick() {
        Objects.requireNonNull(level);
        if (needsCleaning) {
            updateContentsDisplay();
            needsCleaning = false;
        }
    }

    public void serverTick() {
        Objects.requireNonNull(level);
        if (needsCleaning) {
            if (clean()) {
                this.setChanged();
            }
            needsCleaning = false;
        }
    }

    @Override
    public void setChanged() {
        Objects.requireNonNull(level);
        // this.world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        //world.markBlockRangeForRenderUpdate(pos, pos);
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        //world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
        super.setChanged();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        // TODO: This doesn't work properly for custom item renders... since they can go outside the normal bounds
        // TODO: Maybe find out a way to get the render bounding boxes for each of the items??? Bit worse fps for now...
        // return this.contentsBoxes.getRenderBoundingBox(this);
        return BlockEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.contents)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return contents.get(slot);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(this.contents, index, count);

        if (!itemstack.isEmpty()) {
            needsCleaning = true;
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.contents, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.contents.set(index, stack);

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        needsCleaning = true;
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return PlonkConfig.getInventoryStackLimit();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return PlonkConfig.canPlace(stack);
    }

    @Override
    public void clearContent() {
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public int getTileRotation() {
        return tileRotation;
    }

    public void setTileRotation(int tileRotation) {
        this.tileRotation = tileRotation % TILE_ROTATION_COUNT;
    }

    public double getTileRotationAngle() {
        return 360d * this.getTileRotation() / TILE_ROTATION_COUNT;
    }

    public void rotateTile() {
        this.tileRotation = (this.tileRotation + 1) % TILE_ROTATION_COUNT;
        updateContentsDisplay();
    }

    public void rotateSlot(int slot) {
        if (0 <= slot && slot < contentsMeta.length) {
            contentsMeta[slot] = contentsMeta[slot].rotate();
        }
    }

    public ItemStack[] getContentsDisplay() {
        return contentsDisplay;
    }

    public ItemMeta[] getContentsMeta() {
        return contentsMeta;
    }

    public BoxCollection getContentsBoxes() {
        return contentsBoxes;
    }

    /**
     * Attempt to insert the given stack, also recording the renderType status if needed
     *
     * @param stack      to be inserted
     * @param renderType if the stack to be inserted should be treated as a block for display
     * @return the remaining items if successful, or the original stack if not
     */
    public ItemStack insertStack(ItemStack stack, int renderType) {
        ItemUtils.InsertStackResult result = ItemUtils.insertStackAdv(this, stack);
        if (result.remainder != stack) {
            for (int slot : result.slots) {
                contentsMeta[slot] = contentsMeta[slot].withRenderType(renderType);
            }
        }
        return result.remainder;
    }

    public static final class ItemMeta {
        public static final ItemMeta DEFAULT = new ItemMeta(0, 0);
        public final int renderType;
        public final int rotation;

        private ItemMeta(int renderType, int rotation) {
            this.renderType = renderType;
            this.rotation = rotation;
        }

        public ItemMeta withRenderType(int renderType) {
            return new ItemMeta(renderType, rotation);
        }

        public ItemMeta withRotation(int rotation) {
            return new ItemMeta(renderType, rotation);
        }

        public ItemMeta rotate() {
            return withRotation((rotation + 1) % ITEM_ROTATION_COUNT);
        }

        public double getRotationAngle() {
            return 360d * rotation / TilePlacedItems.ITEM_ROTATION_COUNT;
        }
    }

    private static class TagUpgrader {
        public static final ImmutableMap<Integer, Upgrade> UPGRADES = ImmutableMap.<Integer, Upgrade>builder()
                .put(0, TagUpgrader::upgradeFrom0To1)
                .build();

        public static void upgrade(CompoundTag tag) {
            int tileVersion = tag.getInt(TAG_VERSION); // Defaults to 0 if it doesn't exist
            while (tileVersion < Tag_VERSION && UPGRADES.containsKey(tileVersion)) {
                UPGRADES.get(tileVersion).apply(tag);
                tileVersion = tag.getInt(TAG_VERSION);
            }
            if (tileVersion < Tag_VERSION) {
                // Couldn't be upgraded which shouldn't happen
                throw new RuntimeException("Failed to upgrade an existing tile!");
            } else if (tileVersion > Tag_VERSION) {
                // Mod being downgraded
                LOG.warn("Placed Items tile version " + tileVersion + " > " + Tag_VERSION + " (current). Potential loss of data.");
            }
        }

        public static void upgradeFrom0To1(CompoundTag tag) {
            final String TAG_IS_BLOCK = "IsBlock";
            final String TAG_VERSION = "Version";
            final String TAG_TILE_ROTATION = "TileRotation";
            final String TAG_ITEMS = "Items";
            final String TAG_RENDER_TYPE = "RenderType";
            final String TAG_ITEM_ROTATION = "ItemRotation";
            tag.putInt(TAG_VERSION, 1);
            tag.putInt(TAG_TILE_ROTATION, 0);
            ListTag tagItems = tag.getList(TAG_ITEMS, 10);
            for (int i = 0; i < tagItems.size(); i++) {
                CompoundTag tagItem = tagItems.getCompound(i);
                if (tagItem.contains(TAG_IS_BLOCK)) {
                    boolean isBlock = tagItem.getBoolean(TAG_IS_BLOCK);
                    tagItem.putInt(TAG_RENDER_TYPE, isBlock ? 1 : 0);
                }
                tagItem.putInt(TAG_ITEM_ROTATION, 0);
            }
        }

        private interface Upgrade {
            void apply(CompoundTag tag);
        }
    }
}
