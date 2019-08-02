package com.breakinblocks.plonk.common.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TilePlacedItems extends TileEntity implements ISidedInventory {

    private ItemStack[] contents = new ItemStack[this.getSizeInventory()];

    public TilePlacedItems() {
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList tagItems = tag.getTagList("Items", 10);
        this.contents = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < tagItems.tagCount(); i++) {
            NBTTagCompound tagItem = tagItems.getCompoundTagAt(i);
            int slot = tagItem.getByte("Slot") & 255;

            if (slot >= 0 && slot < this.contents.length) {
                this.contents[slot] = ItemStack.loadItemStackFromNBT(tagItem);
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
                tagItem.setByte("Slot", (byte) slot);
                this.contents[slot].writeToNBT(tagItem);
                tagItems.appendTag(tagItem);
            }
        }

        tag.setTag("Items", tagItems);
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

            this.markDirty();
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
        this.markDirty();
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
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return true;
    }
}
