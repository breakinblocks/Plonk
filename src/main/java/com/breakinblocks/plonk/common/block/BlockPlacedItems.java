package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterials;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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

    private static final float HEIGHT_PLATE = 1.0f / 32f;
    private static final float HEIGHT_ITEM = 1.0f / 16f;
    private static final float HEIGHT_BLOCK = 1.0f / 2f;

    // [number of items 0 to 4][collision index][minX, minZ, maxX, maxZ]
    private static final float[][][] COLLISION_XZ;

    static {
        float[][][] temp = new float[5][5][4];

        // Base plate for 0, 1, 2, 3, 4 items
        for(int i = 0; i < 5; i++) {
            temp[i][0][0] = 0.0f;
            temp[i][0][1] = 0.0f;
            temp[i][0][2] = 1.0f;
            temp[i][0][3] = 1.0f;
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
        for (int i = 3; i <=4; i++) {
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
    private AxisAlignedBB collisionBB = null;

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
        if(collisionBB != null) {
            return collisionBB;
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


        for(int i = 0; i < num; i++) {
            collisionIndex = i;
            mops[i] = super.collisionRayTrace(world, x, y, z, from, to);
        }

        // Return closest
        int nearestMopIndex = -1;
        double minDistSq = Double.POSITIVE_INFINITY;

        for(int i = 0; i < num; i++) {
            MovingObjectPosition mop = mops[i];
            if (mop == null) continue;
            double distSq = mop.hitVec.squareDistanceTo(from);
            if(distSq < minDistSq) {
                minDistSq = distSq;
                nearestMopIndex = i;
            }
        }

        if (nearestMopIndex >= 0) {
            collisionIndex = nearestMopIndex;
            this.setBlockBoundsBasedOnState(world, x, y, z);
            collisionBB = AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
        } else {
            collisionBB = null;
        }

        collisionIndex = 0;
        collisionChk = false;
        return nearestMopIndex < 0 ? null : mops[nearestMopIndex];
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
            int num = contents.length;

            if(num > 0) {
                minX = COLLISION_XZ[num][collisionIndex][0];
                minZ = COLLISION_XZ[num][collisionIndex][1];
                maxX = COLLISION_XZ[num][collisionIndex][2];
                maxZ = COLLISION_XZ[num][collisionIndex][3];
                if (collisionIndex > 0) {
                    minY = HEIGHT_PLATE;
                    // TODO: Better Item Block Check?
                    //  Somehow make use of clientside isGoingToRenderAsBlock on the server side?
                    //  Possibly send in the place item packet?
                    // TODO: Single item edge case
                    maxY = contents[collisionIndex - 1].getItem() instanceof ItemBlock ? HEIGHT_BLOCK : HEIGHT_ITEM;
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
                        minX,
                        1.0f - maxY,
                        1.0f - maxZ,
                        maxX,
                        1.0f - minY,
                        1.0f - minZ
                );
                break;
            case 2: // NORTH
                this.setBlockBounds(
                        minX,
                        minZ,
                        minY,
                        maxX,
                        maxZ,
                        maxY
                );
                break;
            case 3: // SOUTH
                this.setBlockBounds(
                        1.0f - maxX,
                        minZ,
                        1.0f - maxY,
                        1.0f - minX,
                        maxZ,
                        1.0f - minY
                );
                break;
            case 4: // WEST
                this.setBlockBounds(
                        minY,
                        minZ,
                        minX,
                        maxY,
                        maxZ,
                        maxX
                );
                break;
            case 5: // EAST
                this.setBlockBounds(
                        1.0f - maxY,
                        minZ,
                        1.0f - maxX,
                        1.0f - minY,
                        maxZ,
                        1.0f - minX
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
}
