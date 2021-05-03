package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterials;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockPlacedItems extends Block {

    public BlockPlacedItems() {
        super(RegistryMaterials.placed_items);
        this.setHardness(0.5f);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB collider, List collisions, Entity entity) {
        WorldUtils.withTile(world, x, y, z, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().addCollisionBoxesToList(this, super::addCollisionBoxesToList, world, x, y, z, collider, collisions, entity));
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return WorldUtils.withTile(world, x, y, z, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().getSelectedBoundingBoxFromPool(),
                () -> super.getSelectedBoundingBoxFromPool(world, x, y, z));
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        if (tile != null) {
            for (int slot = 0; slot < tile.getSizeInventory(); slot++) {
                ItemStack stack = tile.getStackInSlot(slot);
                // TODO: Update item nulls
                if (stack != null) {
                    ItemUtils.dropItemWithinBlock(world, x, y, z, stack);
                    tile.setInventorySlotContents(slot, null);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    @Nullable
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return null; // TODO: Update item null
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 from, Vec3 to) {
        return WorldUtils.withTile(world, x, y, z, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().collisionRayTrace(this, super::collisionRayTrace, world, x, y, z, from, to),
                () -> null);
    }

    /**
     * This is called server side so needs to be ray-traced again
     *
     * @see EntityLivingBase#rayTrace(double, float)
     * @see PlayerControllerMP#getBlockReachDistance()
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int meta, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        // Should be EntityPlayerMP at this point
        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        double reachDistance = playerMP.theItemInWorldManager.getBlockReachDistance();
        float renderPartialTicks = 0.0f;

        // Might have issues if player is moving fast or turning their vision fast
        //  since client-side uses interpolated ray traces
        //Vec3 from = player.getPosition(renderPartialTicks);
        Vec3 from = EntityUtils.getEyePosition(player, renderPartialTicks);
        Vec3 look = player.getLook(renderPartialTicks);
        //Vec3 to = Vec3.createVectorHelper(x + hitX, y + hitY, z + hitZ);
        Vec3 to = from.addVector(look.xCoord * reachDistance, look.yCoord * reachDistance, look.zCoord * reachDistance);

        if (world.func_147447_a(from, to, false, false, true) != null) {
            TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
            int index = tile.getContentsBoxes().getSelectedId();
            int slot = index <= 0 ? 0 : index - 1;
            ItemStack stack = tile.getStackInSlot(slot);
            // TODO: Update item nulls
            if (stack != null) {
                //ItemUtils.dropItemWithinBlock(world, x, y, z, stack);
                if (player.isSneaking()) {
                    tile.rotateSlot(slot);
                } else {
                    ItemUtils.dropItemOnEntity(player, stack);
                    tile.setInventorySlotContents(slot, null);
                }
                tile.markDirty();
                tile.clean();
            }
            return true;
        }
        return super.onBlockActivated(world, x, y, z, player, meta, hitX, hitY, hitZ);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int itemMeta) {
        if (side >= 0 && side < 6) {
            return Facing.oppositeSide[side];
        }
        return super.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, itemMeta);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess iba, int x, int y, int z) {
        WorldUtils.withTile(iba, x, y, z, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().setBlockBoundsBasedOnState(this, iba, x, y, z));
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        // Set the rotation of the tile based on the player's yaw and facing
        int meta = world.getBlockMetadata(x, y, z);
        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        float yaw = entity.rotationYaw % 360f;
        if (yaw < 0) yaw += 360f;
        int rotation = Math.round(yaw / 90f) % 4;
        if (meta == 0) { // Down
            rotation = (rotation + 2) % 4;
        } else if (meta == 1) { // Up
            rotation = 4 - rotation;
        } else {
            rotation = 0;
        }
        tile.setTileRotation(rotation);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TilePlacedItems();
    }

    @Override
    @Nullable // TODO: Remove Nullable
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return WorldUtils.withTile(world, x, y, z, TilePlacedItems.class, tile -> {
            int index = tile.getContentsBoxes().getSelectedId();
            int slot = index <= 0 ? 0 : index - 1;
            return tile.getStackInSlot(slot);
        }, () -> null);
    }
}
