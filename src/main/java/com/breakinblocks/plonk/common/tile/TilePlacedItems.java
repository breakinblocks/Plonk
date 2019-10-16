package com.breakinblocks.plonk.common.tile;

import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.bound.Box;
import com.breakinblocks.plonk.common.util.bound.BoxCollection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.Arrays;

public class TilePlacedItems extends TileEntity implements ISidedInventory {

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
    public static final String TAG_IS_BLOCK = "IsBlock";

    private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
    private boolean[] contentsIsBlock = new boolean[this.getSizeInventory()];
    private ItemStack[] contentsDisplay = new ItemStack[0];
    private BoxCollection contentsBoxes = new BoxCollection.Builder()
            .addBox(0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            .build();
    boolean needsCleaning = true;

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
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
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
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
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
        for (int i = 0; i < contents.length - 1; i++) {
            if (contents[i] == null) {
                // If the slot is empty, try move any non-empty stacks in front of it to the slot
                for (int j = i + 1; j < contents.length; j++) {
                    if (contents[j] != null) {
                        contents[i] = contents[j];
                        // Also update the hitbox
                        contentsIsBlock[i] = contentsIsBlock[j];
                        // TODO: Update null stack
                        contents[j] = null;
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
     * @return size of the array
     */
    private int updateContentsDisplay() {
        int count = 0;
        for (int i = 0; i < contents.length; i++) {
            // TODO: Update null check
            if (contents[i] != null) {
                count = i + 1;
            }
        }
        contentsDisplay = Arrays.copyOf(contents, count);

        updateContentsBoxes(count);

        return count;
    }

    private void updateContentsBoxes(int count) {
        BoxCollection.Builder builder = new BoxCollection.Builder(true, true);

        switch (count) {
            case 1:
                builder.addBox(1, contentsIsBlock[0] ? BOX_BLOCK : BOX_ITEM_ONE);
                break;
            case 2:
                builder.addBox(1, (contentsIsBlock[0] ? BOX_BLOCK : BOX_ITEM_MANY).translate(-0.25, 0, 0));
                builder.addBox(2, (contentsIsBlock[1] ? BOX_BLOCK : BOX_ITEM_MANY).translate(0.25, 0, 0));
                break;
            case 4:
                builder.addBox(4, (contentsIsBlock[3] ? BOX_BLOCK : BOX_ITEM_MANY).translate(0.25, 0, 0.25));
            case 3:
                builder.addBox(1, (contentsIsBlock[0] ? BOX_BLOCK : BOX_ITEM_MANY).translate(-0.25, 0, -0.25));
                builder.addBox(2, (contentsIsBlock[1] ? BOX_BLOCK : BOX_ITEM_MANY).translate(0.25, 0, -0.25));
                builder.addBox(3, (contentsIsBlock[2] ? BOX_BLOCK : BOX_ITEM_MANY).translate(-0.25, 0, 0.25));
                break;
            default:
                builder.addBox(0, 0.0, 0.0, 0.0, 1.0, HEIGHT_PLATE, 1.0);
        }

        int meta = this.hasWorldObj() ? this.getBlockMetadata() : 0;

        switch (meta) {
            case 0: // DOWN
                break;
            case 1: // UP
                builder.apply(box -> box.rotateZ180());
                break;
            case 2: // NORTH
                builder.apply(box -> box.rotateX90());
                break;
            case 3: // SOUTH
                builder.apply(box -> box.rotateX90().rotateY180());
                break;
            case 4: // WEST
                builder.apply(box -> box.rotateX90().rotateY90());
                break;
            case 5: // EAST
                builder.apply(box -> box.rotateX90().rotateY270());
                break;
        }

        contentsBoxes = builder.build();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList tagItems = tag.getTagList(TAG_ITEMS, 10);
        this.contents = new ItemStack[this.getSizeInventory()];
        this.contentsIsBlock = new boolean[this.getSizeInventory()];

        for (int i = 0; i < tagItems.tagCount(); i++) {
            NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
            int slot = tagItem.getByte(TAG_SLOT) & 255;
            boolean isBlock = tagItem.getBoolean(TAG_IS_BLOCK);

            if (slot >= 0 && slot < this.contents.length) {
                this.contents[slot] = ItemStack.loadItemStackFromNBT(tagItem);
                this.contentsIsBlock[slot] = isBlock;
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList tagItems = new NBTTagList();

        for (int slot = 0; slot < this.contents.length; slot++) {
            if (this.contents[slot] != null) {
                NBTTagCompound tagItem = new NBTTagCompound();
                tagItem.setByte(TAG_SLOT, (byte) slot);
                tagItem.setBoolean(TAG_IS_BLOCK, this.contentsIsBlock[slot]);
                this.contents[slot].writeToNBT(tagItem);
                tagItems.appendTag(tagItem);
            }
        }

        tag.setTag(TAG_ITEMS, tagItems);
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;
        if (needsCleaning) {
            if (clean()) {
                this.markDirty();
            }
            needsCleaning = false;
        }
    }

    @Override
    public void markDirty() {
        // TODO: Find out what is required.. LOL
        //this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.getBlockMetadata(), 2);
        //this.worldObj.markAndNotifyBlock(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getChunkFromBlockCoords(this.xCoord, this.zCoord), this.getBlockType(), this.getBlockType(), 2);
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        super.markDirty();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
        updateContentsDisplay();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        // TODO: This doesn't work properly for custom item renders... since they can go outside the normal bounds
        // TODO: Maybe find out a way to get the render bounding boxes for each of the items??? Bit worse fps for now...
        //return this.contentsBoxes.getRenderBoundingBox(this);
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int maxAmount) {
        //TODO: Update null items
        ItemStack current = contents[slot];
        if (maxAmount > 0 && current != null && current.stackSize > 0) {
            int amount = Math.min(current.stackSize, maxAmount);

            ItemStack extractedStack = current.copy();
            extractedStack.stackSize = amount;

            current.stackSize -= amount;

            if (current.stackSize <= 0) {
                contents[slot] = null;
            }

            needsCleaning = true;

            return extractedStack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return contents[slot];
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        //TODO: Update null items
        if (stack == null || stack.stackSize <= 0) {
            contents[slot] = null;
        } else {
            if (stack.stackSize > this.getInventoryStackLimit()) {
                stack.stackSize = this.getInventoryStackLimit();
            }
            contents[slot] = stack;
        }
        needsCleaning = true;
    }

    @Override
    public String getInventoryName() {
        return "container.placed_items";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }

    public ItemStack[] getContentsDisplay() {
        return contentsDisplay;
    }

    public boolean[] getContentsIsBlock() {
        return contentsIsBlock;
    }

    public BoxCollection getContentsBoxes() {
        return contentsBoxes;
    }

    /**
     * Attempt to insert the given stack, also recording the isBlock status if needed
     *
     * @param stack   to be inserted
     * @param isBlock if the stack to be inserted should be treated as a block for display
     * @return the remaining items if successful, or the original stack if not
     */
    public ItemStack insertStack(ItemStack stack, boolean isBlock) {
        ItemUtils.InsertStackResult result = ItemUtils.insertStackAdv(this, stack);
        if (result.remainder != stack) {
            for (int slot : result.slots) {
                contentsIsBlock[slot] = isBlock;
            }
        }
        return result.remainder;
    }
}
