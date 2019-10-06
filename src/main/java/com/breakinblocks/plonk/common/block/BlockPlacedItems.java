package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterials;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.EntityUtils;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.PlayerControllerMP;
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

import java.util.Random;

public class BlockPlacedItems extends Block {

    public static final float HEIGHT_PLATE = 1.0f / 32f;
    public static final float HEIGHT_ITEM = 1.0f / 16f * 1.5f;
    public static final float HEIGHT_BLOCK = 1.0f / 2f;
    public static final float BLOCK_PADDING_PERCENTAGE = 0.0f; // 0.125f for a one pixel padding
    public static final float BLOCK_PADDING_AMOUNT = HEIGHT_BLOCK * BLOCK_PADDING_PERCENTAGE;

    // [number of items 0 to 4][collision index][minX, minZ, maxX, maxZ]
    private static final float[][][] COLLISION_XZ;

    static {
        float[][][] temp = new float[5][5][4];

        // Base plate for 0, 1, 2, 3, 4 items
        for (int i = 0; i < 5; i++) {
            temp[i][0][0] = 0.0f;
            temp[i][0][1] = 0.0f;
            // Don't collide with base plate if there are items inside the block
            if (i > 0) {
                temp[i][0][2] = 0.0f;
                temp[i][0][3] = 0.0f;
            } else {
                temp[i][0][2] = 1.0f;
                temp[i][0][3] = 1.0f;
            }
        }

        // 1 item
        temp[1][1][0] = 0.25f;
        temp[1][1][1] = 0.25f;
        temp[1][1][2] = 0.75f;
        temp[1][1][3] = 0.75f;

        // 2 item
        // left
        temp[2][1][0] = 0.0f;
        temp[2][1][1] = 0.25f;
        temp[2][1][2] = 0.5f;
        temp[2][1][3] = 0.75f;

        // right
        temp[2][2][0] = 0.5f;
        temp[2][2][1] = 0.25f;
        temp[2][2][2] = 1.0f;
        temp[2][2][3] = 0.75f;

        // 3 and 4 items
        for (int i = 3; i <= 4; i++) {
            // top-left
            temp[i][1][0] = 0.0f;
            temp[i][1][1] = 0.0f;
            temp[i][1][2] = 0.5f;
            temp[i][1][3] = 0.5f;

            // top right
            temp[i][2][0] = 0.5f;
            temp[i][2][1] = 0.0f;
            temp[i][2][2] = 1.0f;
            temp[i][2][3] = 0.5f;

            // bottom left
            temp[i][3][0] = 0.0f;
            temp[i][3][1] = 0.5f;
            temp[i][3][2] = 0.5f;
            temp[i][3][3] = 1.0f;

            // bottom right
            temp[i][4][0] = 0.5f;
            temp[i][4][1] = 0.5f;
            temp[i][4][2] = 1.0f;
            temp[i][4][3] = 1.0f;
        }
        COLLISION_XZ = temp;
    }

    private boolean collisionChk = false;
    private int collisionIndex = 0;
    private int collisionLastIndex = 0;
    private AxisAlignedBB collisionLastBB = null;

    public BlockPlacedItems() {
        super(RegistryMaterials.placed_items);
        this.setHardness(0.5f);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        if (collisionLastBB != null) {
            return collisionLastBB;
        }
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
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
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return null;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 from, Vec3 to) {
        collisionChk = true;
        // +1 because base is one of the MOPs
        int num = ((TilePlacedItems) world.getTileEntity(x, y, z)).getContentsDisplay().length + 1;
        MovingObjectPosition[] mops = new MovingObjectPosition[num];

        for (int i = 0; i < num; i++) {
            collisionIndex = i;
            mops[i] = super.collisionRayTrace(world, x, y, z, from, to);
        }

        // Return closest
        int nearestMopIndex = -1;
        double minDistSq = Double.POSITIVE_INFINITY;

        for (int i = 0; i < num; i++) {
            MovingObjectPosition mop = mops[i];
            if (mop == null) continue;
            double distSq = mop.hitVec.squareDistanceTo(from);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearestMopIndex = i;
            }
        }

        if (nearestMopIndex >= 0) {
            collisionIndex = collisionLastIndex = nearestMopIndex;
            this.setBlockBoundsBasedOnState(world, x, y, z);
            collisionLastBB = AxisAlignedBB.getBoundingBox((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ, (double) x + this.maxX, (double) y + this.maxY, (double) z + this.maxZ);
        } else {
            collisionLastIndex = 0;
            collisionLastBB = null;
        }

        collisionIndex = 0;
        collisionChk = false;
        return nearestMopIndex < 0 ? null : mops[nearestMopIndex];
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
            if (this.collisionLastIndex > 0) {
                TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
                if (tile != null) {
                    int slot = this.collisionLastIndex - 1;
                    ItemStack stack = tile.getStackInSlot(slot);
                    // TODO: Update item nulls
                    if (stack != null) {
                        //ItemUtils.dropItemWithinBlock(world, x, y, z, stack);
                        ItemUtils.dropItemOnEntity(player, stack);
                        tile.setInventorySlotContents(slot, null);
                        tile.markDirty();
                        tile.clean();
                    }
                }
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
        int meta = iba.getBlockMetadata(x, y, z);

        float minX = 0.0f;
        float minY = 0.0f;
        float minZ = 0.0f;
        float maxX = 1.0f;
        float maxY = HEIGHT_PLATE;
        float maxZ = 1.0f;

        if (collisionChk) {
            TilePlacedItems tile = (TilePlacedItems) iba.getTileEntity(x, y, z);

            ItemStack[] contents = tile.getContentsDisplay();
            boolean[] contentsIsBlock = tile.getContentsIsBlock();
            int num = contents.length;

            if (num > 0) {
                minX = COLLISION_XZ[num][collisionIndex][0];
                minZ = COLLISION_XZ[num][collisionIndex][1];
                maxX = COLLISION_XZ[num][collisionIndex][2];
                maxZ = COLLISION_XZ[num][collisionIndex][3];
                if (collisionIndex > 0) {
                    minY = 0.0f;
                    boolean isBlock = contentsIsBlock[collisionIndex - 1];
                    // TODO: Single item edge case (big item)
                    if (!isBlock && num == 1) {
                        minX = minZ = 0.0f;
                        maxX = maxZ = 1.0f;
                    }

                    if (isBlock) {
                        maxY = HEIGHT_BLOCK - 2f * BLOCK_PADDING_AMOUNT;
                        // Padding sides of block
                        minX += BLOCK_PADDING_AMOUNT;
                        minZ += BLOCK_PADDING_AMOUNT;
                        maxX -= BLOCK_PADDING_AMOUNT;
                        maxZ -= BLOCK_PADDING_AMOUNT;
                    } else {
                        maxY = HEIGHT_ITEM;
                    }
                }
            }
        }

        // Rotate to match side
        switch (meta) {
            case 0: // DOWN
                this.setBlockBounds(
                        minX,
                        minY,
                        minZ,
                        maxX,
                        maxY,
                        maxZ
                );
                break;
            case 1: // UP
                this.setBlockBounds(
                        1.0f - maxX,
                        1.0f - maxY,
                        minZ,
                        1.0f - minX,
                        1.0f - minY,
                        maxZ
                );
                break;
            case 2: // NORTH
                this.setBlockBounds(
                        minX,
                        1.0f - maxZ,
                        minY,
                        maxX,
                        1.0f - minZ,
                        maxY
                );
                break;
            case 3: // SOUTH
                this.setBlockBounds(
                        1.0f - maxX,
                        1.0f - maxZ,
                        1.0f - maxY,
                        1.0f - minX,
                        1.0f - minZ,
                        1.0f - minY
                );
                break;
            case 4: // WEST
                this.setBlockBounds(
                        minY,
                        1.0f - maxZ,
                        1.0f - maxX,
                        maxY,
                        1.0f - minZ,
                        1.0f - minX
                );
                break;
            case 5: // EAST
                this.setBlockBounds(
                        1.0f - maxY,
                        1.0f - maxZ,
                        minX,
                        1.0f - minY,
                        1.0f - minZ,
                        maxX
                );
                break;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
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
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        int slot = collisionLastIndex <= 0 ? 0 : collisionLastIndex - 1;
        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(x, y, z);
        return tile.getStackInSlot(slot);
    }
}
