package com.breakinblocks.plonk.common.item;

import com.breakinblocks.plonk.common.config.PlonkConfig;
import com.breakinblocks.plonk.common.registry.RegistryBlocks;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockPlacedItems extends BlockItem {
    private static final String TAG_HELD = "Held";
    private static final String TAG_RENDER_TYPE = TilePlacedItems.TAG_RENDER_TYPE;

    public ItemBlockPlacedItems(Item.Properties builder) {
        super(RegistryBlocks.placed_items, builder);
    }

    public void setHeldStack(ItemStack stack, ItemStack held, int renderType) {
        CompoundNBT tagCompound = stack.getOrCreateTag();

        CompoundNBT tagCompoundHeld = tagCompound.getCompound(TAG_HELD);
        held.save(tagCompoundHeld);
        tagCompound.put(TAG_HELD, tagCompoundHeld);

        tagCompound.putInt(TAG_RENDER_TYPE, renderType);

        stack.setTag(tagCompound);
    }

    public ItemStack getHeldStack(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains(TAG_HELD))
            return ItemStack.EMPTY;
        return ItemStack.of(tagCompound.getCompound(TAG_HELD));
    }

    public int getHeldRenderType(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains(TAG_RENDER_TYPE))
            return 0;
        return tagCompound.getInt(TAG_RENDER_TYPE);
    }

    /**
     * Try to insert held item into the tile
     *
     * @param context ItemUseContext that has the ItemBlockPlacedItems reference stack, which should contain renderType information.
     * @param tile    TilePlacedItems to insert into
     * @return true   if stack was at least partially successfully inserted
     */
    protected boolean tryInsertStack(ItemUseContext context, TilePlacedItems tile) {
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
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack heldStack = getHeldStack(context.getItemInHand());
        if (heldStack.isEmpty() || !PlonkConfig.canPlace(heldStack))
            return ActionResultType.FAIL;

        World world = context.getLevel();
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

        PlayerEntity player = context.getPlayer();

        if (tile != null && tryInsertStack(context, tile)) {
            BlockState state = world.getBlockState(pos);
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            return ActionResultType.SUCCESS;
        }

        // Upon failing to insert anything into existing placed items, try place a new block instead
        return super.useOn(context);
    }

    @Override
    public boolean placeBlock(BlockItemUseContext context, BlockState newState) {
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
