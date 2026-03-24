package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.registry.RegistryDataComponents;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockPlacedItems extends BlockItem {
    public record Data(ItemStack held, int renderType) {
        public static final Data DEFAULT = new Data(ItemStack.EMPTY, 0);
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC,
                Data::held,
                ByteBufCodecs.VAR_INT,
                Data::renderType,
                Data::new
        );
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.OPTIONAL_CODEC.fieldOf("Held").forGetter(Data::held),
                Codec.INT.optionalFieldOf(TilePlacedItems.TAG_RENDER_TYPE, 0).forGetter(Data::renderType)
        ).apply(instance, Data::new));

        public Data withHeld(ItemStack held) {
            return new Data(held, renderType);
        }

        public Data withRenderType(int renderType) {
            return new Data(held, renderType);
        }
    }

    public ItemBlockPlacedItems(Item.Properties builder) {
        super(RegistryBlocks.placed_items, builder);
    }

    /**
     * Do not modify the {@link ItemStack} that is passed in.
     */
    public void setHeldStack(ItemStack stack, ItemStack held, int renderType) {
        Data data = stack.getComponents().getOrDefault(RegistryDataComponents.ITEM_BLOCK_PLACED_ITEMS_DATA, Data.DEFAULT);
        data = data.withHeld(held);
        data = data.withRenderType(renderType);
        stack.set(RegistryDataComponents.ITEM_BLOCK_PLACED_ITEMS_DATA, data);
    }

    /**
     * Do not modify the {@link ItemStack} that is returned unless you're immediately clearing it.
     */
    public ItemStack getHeldStack(ItemStack stack) {
        Data data = stack.getComponents().getOrDefault(RegistryDataComponents.ITEM_BLOCK_PLACED_ITEMS_DATA, Data.DEFAULT);
        if (data.held == null)
            return ItemStack.EMPTY;
        return data.held;
    }

    public int getHeldRenderType(ItemStack stack) {
        Data data = stack.getComponents().getOrDefault(RegistryDataComponents.ITEM_BLOCK_PLACED_ITEMS_DATA, Data.DEFAULT);
        return data.renderType;
    }

    /**
     * Try to insert held item into the tile
     *
     * @param context UseOnContext that has the ItemBlockPlacedItems reference stack, which should contain renderType information.
     * @param tile    TilePlacedItems to insert into
     * @return true   if stack was at least partially successfully inserted
     */
    protected boolean tryInsertStack(UseOnContext context, TilePlacedItems tile) {
        ItemStack heldItem = getHeldStack(context.getItemInHand());
        int renderType = getHeldRenderType(context.getItemInHand());
        ItemStack remainder = tile.insertStack(heldItem, renderType);
        tile.setChanged();
        tile.clean();
        if (remainder != heldItem) {
            setHeldStack(context.getItemInHand(), remainder, renderType);
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack heldStack = getHeldStack(context.getItemInHand());
        if (heldStack.isEmpty() || !PlonkConfig.canPlace(heldStack))
            return InteractionResult.FAIL;

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();

        TilePlacedItems tile = null;
        if (world.getBlockState(pos).getBlock() == RegistryBlocks.placed_items) {
            tile = (TilePlacedItems) world.getBlockEntity(pos);
        } else {
            BlockPos pos2 = pos.relative(facing);
            if (world.getBlockState(pos2).getBlock() == RegistryBlocks.placed_items) {
                tile = (TilePlacedItems) world.getBlockEntity(pos2);
            }
        }

        Player player = context.getPlayer();

        if (tile != null && tryInsertStack(context, tile)) {
            BlockState state = world.getBlockState(pos);
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            return InteractionResult.SUCCESS;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.useOn(context);
    }

    @Override
    public boolean placeBlock(BlockPlaceContext context, BlockState newState) {
        ItemStack heldStack = getHeldStack(context.getItemInHand());
        if (heldStack.isEmpty())
            return false;
        if (!super.placeBlock(context, newState))
            return false;

        TilePlacedItems tile = (TilePlacedItems) context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile == null)
            return false;

        // Insert into freshly placed tile
        return tryInsertStack(context, tile);
    }
}
