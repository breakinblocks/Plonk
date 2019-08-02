package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.ENTITY;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

public class TESRPlacedItems extends TileEntitySpecialRenderer {
    private final RenderBlocks renderBlocks = new RenderBlocks();

    public static boolean isGoingToRenderAsBlock(EntityItem entityItem) {
        ItemStack itemstack = entityItem.getEntityItem();
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ENTITY);
        if (customRenderer != null) {
            boolean is3D = customRenderer.shouldUseRenderHelper(ENTITY, itemstack, BLOCK_3D);
            Block block = itemstack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(itemstack.getItem()) : null;
            return is3D || (block != null && RenderBlocks.renderItemIn3d(block.getRenderType()));
        } else if (itemstack.getItemSpriteNumber() == 0 && itemstack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileIn, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        //Rotate Facing
        GL11.glRotated(90, 1.0, 0.0, 0.0);

        GL11.glTranslated(0.0, -0.5, 0.0);

        World world = tileIn.getWorldObj();
        TilePlacedItems tile = (TilePlacedItems) tileIn;

        int num = 0;
        int size = tile.getSizeInventory();
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = tile.getStackInSlot(slot);
            if (stack == null) break;
            num++;
        }

        ItemStack stack = tile.getStackInSlot(0);
        if (stack != null) {
            //TODO: Render Items
            renderStackAsBlock(world, stack);
        }

        GL11.glPopMatrix();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param world Client world
     * @param stack ItemStack to render
     */
    public void renderStackAsBlock(World world, ItemStack stack) {
        if (stack != null) {
            GL11.glPushMatrix();
            EntityItem entityItem = new EntityItem(world, 0.0, 0.0, 0.0, stack);
            entityItem.getEntityItem().stackSize = 1;
            entityItem.hoverStart = 0.0f;
            boolean isRenderBlock = isGoingToRenderAsBlock(entityItem);

            if (isRenderBlock) {
                GL11.glScaled(1.5, 1.5, 1.5);
                //GL11.glRotated(-90, 0.0, 1.0, 0.0);
            } else {
                GL11.glScaled(1.5, 1.5, 1.5);
                GL11.glTranslated(0, 0.03125, 0.0);
                GL11.glRotated(-90, 1.0, 0.0, 0.0);
                GL11.glTranslated(0, -0.2, 0.0);
            }

            boolean prevRenderInFrame = RenderItem.renderInFrame;
            RenderItem.renderInFrame = true;
            RenderManager.instance.renderEntityWithPosYaw(entityItem, 0.0, 0.0, 0.0, 0.0f, 0.0f);
            RenderItem.renderInFrame = prevRenderInFrame;

            GL11.glPopMatrix();
        }
    }
}
