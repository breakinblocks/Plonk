package com.breakinblocks.plonk.common.tile;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.bound.Box;
import com.breakinblocks.plonk.common.util.bound.BoxCollection;
import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.registry.GameData;
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

import javax.annotation.Nullable;
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

    public static final int NBT_VERSION = 1;
    public static final String TAG_VERSION = "Version";
    public static final String TAG_TILE_ROTATION = "TileRotation";
    public static final int TILE_ROTATION_COUNT = 4;
    public static final String TAG_ITEMS = "Items";
    public static final String TAG_SLOT = "Slot";
    public static final String TAG_RENDER_TYPE = "RenderType";
    public static final int RENDER_TYPE_BLOCK = 1;
    public static final int RENDER_TYPE_ITEM = 0;
    public static final String TAG_ITEM_ROTATION = "ItemRotation";
    public static final int ITEM_ROTATION_COUNT = 16;

    private int tileRotation = 0;
    private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
    private ItemMeta[] contentsMeta = new ItemMeta[this.getSizeInventory()];
    private ItemStack[] contentsDisplay = new ItemStack[0];
    private BoxCollection contentsBoxes = new BoxCollection.Builder()
            .addBox(0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
            .build();
    private boolean needsCleaning = true;

    public TilePlacedItems() {
        Arrays.fill(this.contentsMeta, ItemMeta.DEFAULT);
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
        for (int i = 0; i < contents.length - 1; i++) {
            if (contents[i] == null) {
                // If the slot is empty, try move any non-empty stacks in front of it to the slot
                for (int j = i + 1; j < contents.length; j++) {
                    if (contents[j] != null) {
                        contents[i] = contents[j];
                        // Also update the meta
                        contentsMeta[i] = contentsMeta[j];
                        contentsMeta[j] = ItemMeta.DEFAULT;
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
     */
    private void updateContentsDisplay() {
        int count = 0;
        for (int i = 0; i < contents.length; i++) {
            // TODO: Update null check
            if (contents[i] != null) {
                count = i + 1;
            }
        }
        contentsDisplay = Arrays.copyOf(contents, count);

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
        int meta = this.hasWorldObj() ? this.getBlockMetadata() : 0;

        switch (meta) {
            case 0: // DOWN
                break;
            case 1: // UP
                builder.apply(Box::rotateZ180);
                break;
            case 2: // NORTH
                builder.apply(Box::rotateX90);
                break;
            case 3: // SOUTH
                builder.apply(Box::rotateX90).apply(Box::rotateY180);
                break;
            case 4: // WEST
                builder.apply(Box::rotateX90).apply(Box::rotateY90);
                break;
            case 5: // EAST
                builder.apply(Box::rotateX90).apply(Box::rotateY270);
                break;
        }

        contentsBoxes = builder.build();
    }

    /**
     * @see net.minecraft.tileentity.TileEntityChest
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTUpgrader.upgrade(tag);
        this.tileRotation = tag.getInteger(TAG_TILE_ROTATION);
        NBTTagList tagItems = tag.getTagList(TAG_ITEMS, 10);
        this.contents = new ItemStack[this.getSizeInventory()];
        this.contentsMeta = new ItemMeta[this.getSizeInventory()];
        Arrays.fill(this.contentsMeta, ItemMeta.DEFAULT);

        for (int i = 0; i < tagItems.tagCount(); i++) {
            NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
            int slot = tagItem.getByte(TAG_SLOT) & 255;
            int renderType = tagItem.getInteger(TAG_RENDER_TYPE);
            int itemRotation = tagItem.getInteger(TAG_ITEM_ROTATION);

            if (slot >= 0 && slot < this.contents.length) {
                this.contents[slot] = ItemStack.loadItemStackFromNBT(tagItem);
                this.contentsMeta[slot] = new ItemMeta(renderType, itemRotation);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger(TAG_VERSION, NBT_VERSION);
        tag.setInteger(TAG_TILE_ROTATION, tileRotation);
        NBTTagList tagItems = new NBTTagList();

        for (int slot = 0; slot < this.contents.length; slot++) {
            if (this.contents[slot] != null) {
                NBTTagCompound tagItem = new NBTTagCompound();
                tagItem.setByte(TAG_SLOT, (byte) slot);
                tagItem.setInteger(TAG_RENDER_TYPE, this.contentsMeta[slot].renderType);
                tagItem.setInteger(TAG_ITEM_ROTATION, this.contentsMeta[slot].rotation);
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
    @Nullable // TODO: Remove nullable
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    @Nullable // TODO: Remove nullable
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
    @Nullable
    public ItemStack getStackInSlotOnClosing(int slot) {
        return contents[slot];
    }

    @Override
    //TODO: Update null stacks
    public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
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
        return PlonkConfig.getInventoryStackLimit();
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
        return !PlonkConfig.unplaceableItems.contains(GameData.getItemRegistry().getNameForObject(stack.getItem()));
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
     * Attempt to insert the given stack, also recording the isBlock status if needed
     *
     * @param stack      to be inserted
     * @param renderType if the stack to be inserted should be treated as a block for display
     * @return the remaining items if successful, or the original stack if not
     */
    @Nullable //TODO: Update null stacks
    public ItemStack insertStack(@Nullable ItemStack stack, int renderType) {
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

    private static class NBTUpgrader {
        public static final ImmutableMap<Integer, Upgrade> UPGRADES = ImmutableMap.<Integer, Upgrade>builder()
                .put(0, NBTUpgrader::upgradeFrom0To1)
                .build();

        public static void upgrade(NBTTagCompound tag) {
            int tileVersion = tag.getInteger(TAG_VERSION); // Defaults to 0 if it doesn't exist
            while (tileVersion < NBT_VERSION && UPGRADES.containsKey(tileVersion)) {
                UPGRADES.get(tileVersion).apply(tag);
                tileVersion = tag.getInteger(TAG_VERSION);
            }
            if (tileVersion < NBT_VERSION) {
                // Couldn't be upgraded which shouldn't happen
                throw new RuntimeException("Failed to upgrade an existing tile!");
            } else if (tileVersion > NBT_VERSION) {
                // Mod being downgraded
                Plonk.LOG.warn("Placed Items tile version " + tileVersion + " > " + NBT_VERSION + " (current). Potential loss of data.");
            }
        }

        public static void upgradeFrom0To1(NBTTagCompound tag) {
            final String TAG_IS_BLOCK = "IsBlock";
            final String TAG_VERSION = "Version";
            final String TAG_TILE_ROTATION = "TileRotation";
            final String TAG_ITEMS = "Items";
            final String TAG_RENDER_TYPE = "RenderType";
            final String TAG_ITEM_ROTATION = "ItemRotation";
            tag.setInteger(TAG_VERSION, 1);
            tag.setInteger(TAG_TILE_ROTATION, 0);
            NBTTagList tagItems = tag.getTagList(TAG_ITEMS, 10);
            for (int i = 0; i < tagItems.tagCount(); i++) {
                NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
                if (tagItem.hasKey(TAG_IS_BLOCK)) {
                    boolean isBlock = tagItem.getBoolean(TAG_IS_BLOCK);
                    tagItem.setInteger(TAG_RENDER_TYPE, isBlock ? 1 : 0);
                }
                tagItem.setInteger(TAG_ITEM_ROTATION, 0);
            }
        }

        private interface Upgrade {
            void apply(NBTTagCompound tag);
        }
    }
}
