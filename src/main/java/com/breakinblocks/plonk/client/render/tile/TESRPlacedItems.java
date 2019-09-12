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
        // Centre of Block
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        //Rotate Facing
        int meta = tileIn.getBlockMetadata();
        switch (meta) {
            case 0: // DOWN
                break;
            case 1: // UP
                GL11.glRotated(180, 1.0, 0.0, 0.0);
                GL11.glRotated(180, 0.0, 1.0, 0.0);
                break;
            case 2: // NORTH
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                break;
            case 3: // SOUTH
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(180, 0.0, 0.0, 1.0);
                break;
            case 4: // WEST
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(-90, 0.0, 0.0, 1.0);
                break;
            case 5: // EAST
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(90, 0.0, 0.0, 1.0);
                break;
        }

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

        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                GL11.glPushMatrix();
                ItemStack stack = tile.getStackInSlot(slot);
                switch (num) {
                    case 1:
                        // No shift
                        break;
                    case 2:
                        // Shift left for slot 0
                        if (slot == 0) {
                            GL11.glTranslated(-0.25, 0.0, 0.0);
                        } else {
                            GL11.glTranslated(0.25, 0.0, 0.0);
                        }
                        break;
                    default:
                        boolean left = slot % 2 == 0;
                        boolean top = (slot / 2) == 0;
                        GL11.glTranslated(left ? -0.25 : 0.25, 0.0, top ? -0.25 : 0.25);
                        break;
                }
                //renderStack(world, stack, partialTicks, halfSize, world.rand.nextBoolean());
                renderStack(world, stack, partialTicks, halfSize, false);
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param world         Client world
     * @param stack         ItemStack to render
     * @param halfSize      If items should be rendered at half size (blocks are always rendered half size)
     * @param renderInFrame If should set RenderItem.renderInFrame
     */
    public void renderStack(World world, ItemStack stack, float partialTicks, boolean halfSize, boolean renderInFrame) {
        if (stack != null) {
            GL11.glPushMatrix();
            EntityItem entityItem = new EntityItem(world, 0.0, 0.0, 0.0, stack);
            entityItem.getEntityItem().stackSize = 1;
            entityItem.hoverStart = 0.0f;
            boolean isRenderBlock = isGoingToRenderAsBlock(entityItem);
            double t = world.getTotalWorldTime() + partialTicks;

            if (halfSize || isRenderBlock) {
                GL11.glScaled(0.5, 0.5, 0.5);
            }

            if (isRenderBlock) {
                // RenderBlocks.renderBlockAsItem
                GL11.glTranslated(0.0, 0.5, 0.0);
                GL11.glRotated(-90.0, 0.0, 1.0, 0.0);
                // RenderItem.doRender
                GL11.glScaled(4.0, 4.0, 4.0);

                if (renderInFrame) {
                    GL11.glRotated(90.0, 0.0, 1.0, 0.0);
                    GL11.glTranslated(0.0, -0.05, 0.0);
                    GL11.glScaled(1.0 / 1.25, 1.0 / 1.25, 1.0 / 1.25);
                }
            } else {
                GL11.glRotated(-90.0, 1.0, 0.0, 0.0);
                // RenderItem.renderDroppedItem
                //GL11.glTranslated(0.0, 0.0, -1.0*(0.0625 + 0.021875));
                //GL11.glTranslated(0.5, 0.25, 0.5*(0.0625 + 0.021875));
                //GL11.glTranslated(-0.5, -0.5, 0.0625);

                GL11.glTranslated(0.0, -0.25, 0.0625);

                if (RenderManager.instance.options.fancyGraphics) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    if (renderInFrame) {
                        GL11.glRotatef(-180.0F, 0.0F, 1.0F, 0.0F);
                    }
                } else {
                    if (!renderInFrame) {
                        GL11.glRotatef(-(180.0F - RenderManager.instance.playerViewY), 0.0F, 1.0F, 0.0F);
                    }
                }

                // RenderItem.doRender
                if (renderInFrame) {
                    GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                    GL11.glScaled(1.0 / 0.5128205, 1.0 / 0.5128205, 1.0 / 0.5128205);
                } else {
                    GL11.glScaled(2.0, 2.0, 2.0);
                }
            }

            boolean prevRenderInFrame = RenderItem.renderInFrame;
            RenderItem.renderInFrame = renderInFrame;
            // RenderItem.doRender
            GL11.glTranslated(0, -0.1, 0.0);
            RenderManager.instance.renderEntityWithPosYaw(entityItem, 0.0, 0.0, 0.0, 0.0f, 0.0f);
            RenderItem.renderInFrame = prevRenderInFrame;

            GL11.glPopMatrix();
        }
    }
}
