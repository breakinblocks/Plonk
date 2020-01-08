package com.breakinblocks.plonk.common.tile;

import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.bound.Box;
import com.breakinblocks.plonk.common.util.bound.BoxCollection;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TilePlacedItems extends TileEntity implements ISidedInventory, ITickable {

    public static final PropertyDirection FACING = BlockPlacedItems.FACING;
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

    public static final String TAG_ITEMS = "Items";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_RENDER_TYPE = "RenderType";
    public static final int RENDER_TYPE_BLOCK = 1;
    public static final int RENDER_TYPE_ITEM = 0;
    boolean needsCleaning = true;
    private NonNullList<ItemStack> contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
    private int[] contentsRenderType = new int[this.getSizeInventory()];
    private ItemStack[] contentsDisplay = new ItemStack[0];
    private BoxCollection contentsBoxes = new BoxCollection.Builder()
            .addBox(0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            .build();

    public TilePlacedItems() {
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
            this.world.setBlockToAir(this.pos);
            return true;
        }

        // If the contents to display are up to date
        if (contentsDisplay.length == count) {
            // If there is no empty space
            if (first_empty == -1) return false;
            // If there is empty space only in front of a non-empty space then nothing can be moved
            if (first_empty > last_not_empty) return false;
        }

        updateContents();

        updateContentsDisplay();

        return true;
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
                        contentsRenderType[i] = contentsRenderType[j];
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
     *
     * @return size of the array
     */
    private int updateContentsDisplay() {
        int count = 0;
        for (int i = 0; i < contents.size(); i++) {
            if (!contents.get(i).isEmpty()) {
                count = i + 1;
            }
        }
        contentsDisplay = contents.stream().limit(count).toArray(ItemStack[]::new);

        updateContentsBoxes(count);

        return count;
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
        BoxCollection.Builder builder = new BoxCollection.Builder(false, true);

        switch (count) {
            case 1:
                builder.addBox(1, getBox(count, contentsRenderType[0]));
                break;
            case 2:
                builder.addBox(1, getBox(count, contentsRenderType[0]).translate(-0.25, 0, 0));
                builder.addBox(2, getBox(count, contentsRenderType[1]).translate(0.25, 0, 0));
                break;
            case 4:
                builder.addBox(4, getBox(count, contentsRenderType[3]).translate(0.25, 0, 0.25));
            case 3:
                builder.addBox(1, getBox(count, contentsRenderType[0]).translate(-0.25, 0, -0.25));
                builder.addBox(2, getBox(count, contentsRenderType[1]).translate(0.25, 0, -0.25));
                builder.addBox(3, getBox(count, contentsRenderType[2]).translate(-0.25, 0, 0.25));
                break;
            default:
                builder.addBox(0, 0.0, 0.0, 0.0, 1.0, HEIGHT_PLATE, 1.0);
        }

        EnumFacing facing = this.getWorld().getBlockState(this.getPos()).getValue(FACING);

        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                builder.apply(box -> box.rotateZ180());
                break;
            case SOUTH: // NORTH
                builder.apply(box -> box.rotateX90());
                break;
            case NORTH: // SOUTH
                builder.apply(box -> box.rotateX90().rotateY180());
                break;
            case EAST: // WEST
                builder.apply(box -> box.rotateX90().rotateY90());
                break;
            case WEST: // EAST
                builder.apply(box -> box.rotateX90().rotateY270());
                break;
        }

        contentsBoxes = builder.build();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList tagItems = tag.getTagList(TAG_ITEMS, 10);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        this.contentsRenderType = new int[this.getSizeInventory()];

        for (int i = 0; i < tagItems.tagCount(); i++) {
            NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
            int slot = tagItem.getByte(TAG_SLOT) & 255;
            int renderType = tagItem.getInteger(TAG_RENDER_TYPE);
            // TODO: Remove Migration Code at some point?
            final String TAG_IS_BLOCK = "IsBlock";
            if (tagItem.hasKey(TAG_IS_BLOCK)) {
                boolean isBlock = tagItem.getBoolean(TAG_IS_BLOCK);
                renderType = isBlock ? 1 : 0;
            }

            if (slot >= 0 && slot < this.contents.size()) {
                this.contents.set(slot, new ItemStack(tagItem));
                this.contentsRenderType[slot] = renderType;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList tagItems = new NBTTagList();

        for (int slot = 0; slot < this.contents.size(); slot++) {
            if (!this.contents.get(slot).isEmpty()) {
                NBTTagCompound tagItem = new NBTTagCompound();
                tagItem.setByte(TAG_SLOT, (byte) slot);
                tagItem.setInteger(TAG_RENDER_TYPE, this.contentsRenderType[slot]);
                this.contents.get(slot).writeToNBT(tagItem);
                tagItems.appendTag(tagItem);
            }
        }

        tag.setTag(TAG_ITEMS, tagItems);

        return tag;
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (needsCleaning) {
            if (clean()) {
                this.markDirty();
            }
            needsCleaning = false;
        }
    }

    @Override
    public void markDirty() {
        // this.world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        world.scheduleBlockUpdate(pos, this.getBlockType(), 0, 0);
        super.markDirty();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(super.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.readFromNBT(tag);
        updateContentsDisplay();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        // TODO: This doesn't work properly for custom item renders... since they can go outside the normal bounds
        // TODO: Maybe find out a way to get the render bounding boxes for each of the items??? Bit worse fps for now...
        // return this.contentsBoxes.getRenderBoundingBox(this);
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public int getSizeInventory() {
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
    public ItemStack getStackInSlot(int slot) {
        return contents.get(slot);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.contents, index, count);

        if (!itemstack.isEmpty()) {
            needsCleaning = true;
            this.markDirty();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.contents, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.contents.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        needsCleaning = true;
        this.markDirty();
    }

    @Override
    public String getName() {
        return "container.placed_items";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    public ItemStack[] getContentsDisplay() {
        return contentsDisplay;
    }

    public int[] getContentsRenderType() {
        return contentsRenderType;
    }

    public BoxCollection getContentsBoxes() {
        return contentsBoxes;
    }

    /**
     * Attempt to insert the given stack, also recording the renderType status if needed
     *
     * @param stack   to be inserted
     * @param renderType if the stack to be inserted should be treated as a block for display
     * @return the remaining items if successful, or the original stack if not
     */
    public ItemStack insertStack(ItemStack stack, int renderType) {
        ItemUtils.InsertStackResult result = ItemUtils.insertStackAdv(this, stack);
        if (result.remainder != stack) {
            for (int slot : result.slots) {
                contentsRenderType[slot] = renderType;
            }
        }
        return result.remainder;
    }
}
