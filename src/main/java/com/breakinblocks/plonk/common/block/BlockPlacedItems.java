package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.WorldUtils;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

import javax.annotation.Nullable;
import java.util.Objects;

import static net.minecraft.entity.player.PlayerEntity.REACH_DISTANCE;

/**
 * @see ChestBlock
 */
public class BlockPlacedItems extends Block implements IWaterLoggable {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /**
     * This is such a hack. Find a better way to do this eventually please???
     * The issue with handling the {@link DrawBlockHighlightEvent.HighlightBlock} event is that
     * it would break with other mods that add a custom block highlight...
     */
    private final ThreadLocal<Boolean> picking = ThreadLocal.withInitial(() -> false);

    public BlockPlacedItems(Block.Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
    }

    /**
     * @see AbstractGlassBlock#propagatesSkylightDown(BlockState, IBlockReader, BlockPos)
     */
    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return getFluidState(state).isEmpty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        //if (true) return BlockRenderType.MODEL;
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    /**
     * @see ChestBlock#updatePostPlacement(BlockState, Direction, BlockState, IWorld, BlockPos, BlockPos)
     */
    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // Used for block selection (and also rendering of the selection)
        return WorldUtils.withTile(worldIn, pos, TilePlacedItems.class, tile -> {
            if (picking.get())
                return tile.getContentsBoxes().getSelectionShape();
            int slot = -1;
            Entity entity = context.getEntity();
            if (entity instanceof PlayerEntity) {
                slot = getPickedSlot(tile, pos, (PlayerEntity) entity);
            }
            return slot >= 0 ? tile.getContentsBoxes().getSelectionShapeById(slot + 1) : tile.getContentsBoxes().getSelectionShape();
        }, VoxelShapes::empty);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        // Used for collision with entities
        return WorldUtils.withTile(worldIn, pos, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().getCollisionShape(),
                VoxelShapes::empty);
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        // Used for colliding with the camera (third person)
        return VoxelShapes.empty();
    }

    /**
     * @see ChestBlock#getStateForPlacement(BlockItemUseContext)
     */
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getFace();
        IFluidState fluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState().with(FACING, direction).with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
    }

    /**
     * @see ChestBlock#getFluidState(BlockState)
     */
    @Override
    @SuppressWarnings("deprecation")
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    /**
     * @see AbstractGlassBlock#getAmbientOcclusionLightValue(BlockState, IBlockReader, BlockPos)
     */
    @Override
    @SuppressWarnings("deprecation")
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1.0F;
    }

    /**
     * @see ChestBlock#onReplaced(BlockState, World, BlockPos, BlockState, boolean)
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof IInventory) {
                // Need this to prevent dupes: See MinecraftForge#7609
                if (!worldIn.restoringBlockSnapshots) {
                    InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
                }
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    /**
     * Duplicated because it's clientside only on 1.14.4.
     *
     * @see Entity#pick(double, float, boolean)
     */
    public RayTraceResult pick(PlayerEntity player, double p_213324_1_, float p_213324_3_, boolean p_213324_4_) {
        Vec3d vec3d = player.getEyePosition(p_213324_3_);
        Vec3d vec3d1 = player.getLook(p_213324_3_);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * p_213324_1_, vec3d1.y * p_213324_1_, vec3d1.z * p_213324_1_);
        return player.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, p_213324_4_ ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, player));
    }

    /**
     * This can be called server side so needs to be ray-traced again
     *
     * @return -1 if no hit otherwise the closest slot
     * @see Entity#pick(double, float, boolean)
     * @see PlayerController#getBlockReachDistance()
     */
    protected int getPickedSlot(TilePlacedItems tile, BlockPos pos, PlayerEntity player) {
        if (picking.get()) return -1;
        double blockReachDistance = Objects.requireNonNull(player.getAttribute(REACH_DISTANCE)).getValue();
        float partialTicks = 0.0f;

        // Might have issues if player is moving fast or turning their vision fast
        //  since client-side uses interpolated ray traces
        RayTraceResult traceResult;
        try {
            picking.set(true);
            traceResult = pick(player, blockReachDistance, partialTicks, false);
        } finally {
            picking.set(false);
        }
        if (traceResult.getType() == RayTraceResult.Type.BLOCK) {
            Vec3d hitVec = traceResult.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
            int index = tile.getContentsBoxes().getSelectionIndexFromHitVec(hitVec);
            return index <= 0 ? 0 : index - 1;
        }
        return -1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) return true;

        TilePlacedItems tile = (TilePlacedItems) worldIn.getTileEntity(pos);
        if (tile == null) return true;

        int slot = getPickedSlot(tile, pos, player);
        if (slot >= 0) {
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
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (placer == null) return;
        TilePlacedItems tile = (TilePlacedItems) worldIn.getTileEntity(pos);
        Objects.requireNonNull(tile);
        // Set the rotation of the tile based on the player's yaw and facing
        Direction facing = worldIn.getBlockState(pos).get(FACING);
        float yaw = placer.rotationYaw % 360f;
        if (yaw < 0) yaw += 360f;
        int rotation = Math.round(yaw / 90f) % 4;
        if (facing == Direction.UP) { // Down
            rotation = (rotation + 2) % 4;
        } else if (facing == Direction.DOWN) { // Up
            rotation = 4 - rotation;
        } else {
            rotation = 0;
        }
        tile.setTileRotation(rotation);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return RegistryTileEntities.placed_items.create();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return WorldUtils.withTile(world, pos, TilePlacedItems.class, tile -> {
            int slot = getPickedSlot(tile, pos, player);
            return slot >= 0 ? tile.getStackInSlot(slot) : ItemStack.EMPTY;
        }, () -> ItemStack.EMPTY);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public boolean addHitEffects(BlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state, world, pos, entity);
    }
}
