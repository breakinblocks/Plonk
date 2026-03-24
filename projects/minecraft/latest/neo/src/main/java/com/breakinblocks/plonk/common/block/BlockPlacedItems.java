package com.breakinblocks.plonk.common.block;

import com.breakinblocks.plonk.common.registry.RegistryTileEntities;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.ItemUtils;
import com.breakinblocks.plonk.common.util.WorldUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @see ChestBlock
 */
public class BlockPlacedItems extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<BlockPlacedItems> CODEC = simpleCodec(BlockPlacedItems::new);
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    /**
     * This is such a hack. Find a better way to do this eventually please???
     * The issue with handling the {@link RenderHighlightEvent.Block} event is that
     * it would break with other mods that add a custom block highlight...
     */
    private final ThreadLocal<Boolean> picking = ThreadLocal.withInitial(() -> false);

    public BlockPlacedItems(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide) {
            return createTickerHelper(pBlockEntityType, RegistryTileEntities.placed_items, TilePlacedItems::clientTick);
        } else {
            return createTickerHelper(pBlockEntityType, RegistryTileEntities.placed_items, TilePlacedItems::serverTick);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        //if (true) return RenderShape.MODEL;
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /**
     * @see ChestBlock#updateShape(BlockState, Direction, BlockState, LevelAccessor, BlockPos, BlockPos)
     */
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        // Used for block selection (and also rendering of the selection)
        return WorldUtils.withTile(worldIn, pos, TilePlacedItems.class, tile -> {
            if (picking.get())
                return tile.getContentsBoxes().getSelectionShape();
            int slot = -1;
            Entity entity = context instanceof EntityCollisionContext ? ((EntityCollisionContext) context).getEntity() : null;
            if (entity instanceof Player) {
                slot = getPickedSlot(tile, pos, (Player) entity);
            }
            return slot >= 0 ? tile.getContentsBoxes().getSelectionShapeById(slot + 1) : tile.getContentsBoxes().getSelectionShape();
        }, Shapes::empty);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        // Used for collision with entities
        return WorldUtils.withTile(worldIn, pos, TilePlacedItems.class,
                tile -> tile.getContentsBoxes().getCollisionShape(),
                Shapes::empty);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        // Used for colliding with the camera (third person)
        return Shapes.empty();
    }

    /**
     * @see ChestBlock#getStateForPlacement(BlockPlaceContext)
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, direction).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    /**
     * @see ChestBlock#getFluidState(BlockState)
     */
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    /**
     * @see TransparentBlock#getShadeBrightness(BlockState, BlockGetter, BlockPos)
     */
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F;
    }

    /**
     * @see ChestBlock#onRemove(BlockState, Level, BlockPos, BlockState, boolean)
     */
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof Container) {
                // Need this to prevent dupes: See MinecraftForge#7609
                if (!worldIn.restoringBlockSnapshots) {
                    Containers.dropContents(worldIn, pos, (Container) tileentity);
                }
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    /**
     * This can be called server side so needs to be ray-traced again
     *
     * @return -1 if no hit otherwise the closest slot
     * @see Entity#pick(double, float, boolean)
     * @see Attributes#BLOCK_INTERACTION_RANGE
     */
    protected int getPickedSlot(TilePlacedItems tile, BlockPos pos, Player player) {
        if (picking.get()) return -1;
        double blockReachDistance = Objects.requireNonNull(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).getValue();
        float partialTicks = 0.0f;

        // Might have issues if player is moving fast or turning their vision fast
        //  since client-side uses interpolated ray traces
        HitResult traceResult;
        try {
            picking.set(true);
            traceResult = player.pick(blockReachDistance, partialTicks, false);
        } finally {
            picking.set(false);
        }
        if (traceResult.getType() == HitResult.Type.BLOCK) {
            Vec3 hitVec = traceResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            int index = tile.getContentsBoxes().getSelectionIndexFromHitVec(hitVec);
            return index <= 0 ? 0 : index - 1;
        }
        return -1;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (worldIn.isClientSide) return InteractionResult.SUCCESS;

        TilePlacedItems tile = (TilePlacedItems) worldIn.getBlockEntity(pos);
        if (tile == null) return InteractionResult.SUCCESS;

        int slot = getPickedSlot(tile, pos, player);
        if (slot >= 0) {
            ItemStack stack = tile.getItem(slot);
            if (!stack.isEmpty()) {
                //ItemUtils.dropItemWithinBlock(worldId, x, y, z, stack);
                if (player.isShiftKeyDown()) {
                    tile.rotateSlot(slot);
                } else {
                    ItemUtils.dropItemOnEntity(player, stack);
                    tile.setItem(slot, ItemStack.EMPTY);
                }
                tile.setChanged();
                tile.clean();
            }
            return InteractionResult.CONSUME;
        }

        return super.useWithoutItem(state, worldIn, pos, player, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (placer == null) return;
        TilePlacedItems tile = (TilePlacedItems) worldIn.getBlockEntity(pos);
        Objects.requireNonNull(tile);
        // Set the rotation of the tile based on the player's yaw and facing
        Direction facing = worldIn.getBlockState(pos).getValue(FACING);
        float yaw = placer.getYRot() % 360f;
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RegistryTileEntities.placed_items.create(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return WorldUtils.withTile(level, pos, TilePlacedItems.class, tile -> {
            int slot = getPickedSlot(tile, pos, player);
            return slot >= 0 ? tile.getItem(slot) : ItemStack.EMPTY;
        }, () -> ItemStack.EMPTY);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        return true;
    }

    public static void initializeClientStatic(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState state, Level Level, HitResult target, ParticleEngine manager) {
                return true;
            }

            @Override
            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
                return true;
            }
        });
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state, world, pos, entity);
    }
}
