package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryMaterials;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.bound.BoxCollection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BlockPlacedItems extends Block {

    public static final PropertyDirection FACING = BlockDirectional.FACING;

    public BlockPlacedItems() {
        super(RegistryMaterials.placed_items);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        //this.setHardness(0.5f);
        this.setSoundType(SoundType.STONE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        // TODO: Maybe implement rotation?
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        //return EnumBlockRenderType.MODEL;
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TilePlacedItems tile = (TilePlacedItems) source.getTileEntity(pos);
        return tile != null ? tile.getContentsBoxes().getBoundingBox(pos) : FULL_BLOCK_AABB;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TilePlacedItems tile = (TilePlacedItems) Objects.requireNonNull(worldIn.getTileEntity(pos));
        tile.getContentsBoxes().addCollidingBoxes(pos, entityBox, collidingBoxes);
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TilePlacedItems tile = (TilePlacedItems) worldIn.getTileEntity(pos);
        return tile.getContentsBoxes().getSelectedBoundingBoxFromPool();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /**
     * @see BlockChest#breakBlock(net.minecraft.world.World, net.minecraft.util.math.BlockPos, net.minecraft.block.state.IBlockState)
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        TilePlacedItems tile = (TilePlacedItems) worldIn.getTileEntity(pos);
        return tile.getContentsBoxes().collisionRayTrace(this, super::collisionRayTrace, blockState, worldIn, pos, start, end);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    /**
     * This is called server side so needs to be ray-traced again
     *
     * @see EntityLivingBase#rayTrace(double, float)
     * @see PlayerControllerMP#getBlockReachDistance()
     */
    @Override
    public boolean onBlockActivated(World worldId, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldId.isRemote) return true;
        // Should be EntityPlayerMP at this point
        EntityPlayerMP player = (EntityPlayerMP) playerIn;

        double blockReachDistance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        float partialTicks = 0.0f;

        // Might have issues if player is moving fast or turning their vision fast
        //  since client-side uses interpolated ray traces
        Vec3d from = player.getPositionEyes(partialTicks);
        Vec3d look = player.getLook(partialTicks);
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);
        //return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);

        if (worldId.rayTraceBlocks(from, to, false, false, true) != null) {
            TilePlacedItems tile = (TilePlacedItems) worldId.getTileEntity(pos);
            int index = tile.getContentsBoxes().selectionLastEntry.id;
            int slot = index <= 0 ? 0 : index - 1;
            ItemStack stack = tile.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                //ItemUtils.dropItemWithinBlock(worldId, x, y, z, stack);
                if (player.isSneaking()) {
                    tile.rotateSlot(slot);
                } else {
                    ItemUtils.dropItemOnEntity(player, stack);
                    tile.setInventorySlotContents(slot, ItemStack.EMPTY);
                }
                tile.markDirty();
                tile.clean();
            }
            return true;
        }
        return super.onBlockActivated(worldId, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        // Set the rotation of the tile based on the player's yaw and facing
        EnumFacing facing = worldIn.getBlockState(pos).getValue(FACING);
        TilePlacedItems tile = (TilePlacedItems) worldIn.getTileEntity(pos);
        float yaw = placer.rotationYaw % 360f;
        if (yaw < 0) yaw += 360f;
        int rotation = Math.round(yaw / 90f) % 4;
        if (facing == EnumFacing.UP) { // Down
            rotation = (rotation + 2) % 4;
        } else if (facing == EnumFacing.DOWN) { // Up
            rotation = 4 - rotation;
        } else {
            rotation = 0;
        }
        tile.setTileRotation(rotation);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePlacedItems();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TilePlacedItems tile = (TilePlacedItems) world.getTileEntity(pos);
        if (tile == null) return ItemStack.EMPTY;
        BoxCollection.Entry last = tile.getContentsBoxes().selectionLastEntry;
        if (last == null) return ItemStack.EMPTY;
        int index = last.id;
        int slot = index <= 0 ? 0 : index - 1;
        return tile.getStackInSlot(slot);
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return state.withProperty(FACING, facing);
    }
}
