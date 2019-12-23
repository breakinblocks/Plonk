package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;

public class TESRPlacedItems extends TileEntitySpecialRenderer<TilePlacedItems> {

    public static final PropertyDirection FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final RenderItem renderItem = mc.getRenderItem();

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder bufferbuilder = tessellator.getBuffer();
    private static final BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();

    public TESRPlacedItems() {
    }

    public static boolean isGoingToRenderAsBlock(ItemStack itemstack) {
        IBakedModel model = renderItem.getItemModelWithOverrides(itemstack, null, null);
        Matrix4f matrixFixed = model.handlePerspective(ItemCameraTransforms.TransformType.FIXED).getRight();
        if (matrixFixed == null) {
            matrixFixed = new Matrix4f();
            matrixFixed.setIdentity();
        }
        Matrix4f matrixGround = model.handlePerspective(ItemCameraTransforms.TransformType.GROUND).getRight();
        if (matrixGround == null) {
            matrixGround = new Matrix4f();
            matrixGround.setIdentity();
        }

        Matrix4d difference = MatrixUtils.difference(new Matrix4d(matrixGround), new Matrix4d(matrixFixed));
        MatrixUtils.TransformData transform = new MatrixUtils.TransformData(difference);

        // TODO: Maybe check difference between item frame vs how it's displayed in the inventory to see?
        return Math.abs(transform.pitch) < 0.001;
//        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ENTITY);
//        if (customRenderer != null) {
//            boolean is3D = customRenderer.shouldUseRenderHelper(ENTITY, itemstack, BLOCK_3D);
//            Block block = itemstack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(itemstack.getItem()) : null;
//            return is3D || (block != null && RenderBlocks.renderItemIn3d(block.getRenderType()));
//        } else if (itemstack.getItemSpriteNumber() == 0 && itemstack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType())) {
//            return true;
//        } else {
//            return false;
//        }
    }

    @Override
    public void render(TilePlacedItems te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(FACING);

        GL11.glPushMatrix();
        // Centre of Block
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        //Rotate Facing
        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                GL11.glRotated(180, 1.0, 0.0, 0.0);
                GL11.glRotated(180, 0.0, 1.0, 0.0);
                break;
            case SOUTH: // NORTH
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                break;
            case NORTH: // SOUTH
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(180, 0.0, 0.0, 1.0);
                break;
            case EAST: // WEST
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(-90, 0.0, 0.0, 1.0);
                break;
            case WEST: // EAST
                GL11.glRotated(90, 1.0, 0.0, 0.0);
                GL11.glRotated(90, 0.0, 0.0, 1.0);
                break;
        }

        GL11.glTranslated(0.0, -0.5, 0.0);

        ItemStack[] contents = te.getContentsDisplay();
        boolean[] contentsIsBlock = te.getContentsIsBlock();
        int num = contents.length;

        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                GL11.glPushMatrix();
                ItemStack stack = te.getStackInSlot(slot);
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
                renderStack(te.getWorld(), stack, contentsIsBlock[slot], partialTicks, halfSize);
                GL11.glPopMatrix();
            }
        }

        GL11.glPopMatrix();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param world    Client world
     * @param stack    ItemStack to render
     * @param halfSize If items should be rendered at half size (blocks are always rendered half size)
     */
    public void renderStack(World world, ItemStack stack, boolean isBlock, float partialTicks, boolean halfSize) {
        // net.minecraft.client.renderer.entity.RenderItemFrame.renderItem
        if (stack.isEmpty()) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.pushAttrib();
        RenderHelper.enableStandardItemLighting();

        // GROUND
        //GlStateManager.translate(0f, -0.125f, 0f);
        //GlStateManager.scale(2F, 2F, 2F);

        // FIXED
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        if (isBlock) {
            GlStateManager.translate(0f, 0.25f, 0f);
            this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        } else {
            GlStateManager.translate(0f, 2f / 48, 0f);
            GlStateManager.rotate(90f, 1f, 0f, 0f);
            if (halfSize)
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        }
//        if (stack.getItem() instanceof ItemBlock) {
//            GlStateManager.translate(0f, 0.25f, 0f);
//            this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
//            ItemBlock itemBlock = (ItemBlock) stack.getItem();
//            IBlockState state = itemBlock.getBlock().getDefaultState();
//            GlStateManager.scale(0.5F, 0.5F, 0.5F);
//            GlStateManager.translate(-0.5f-blockPos.getX(), -blockPos.getY(), -0.5f-blockPos.getZ());
//            bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
//            blockRendererDispatcher.getBlockModelRenderer().renderModel(
//                    world, blockRendererDispatcher.getModelForState(state), state,
//                    blockPos, bufferbuilder, false, MathHelper.getPositionRandom(blockPos)
//            );
//            tessellator.draw();
//        } else {
//            GlStateManager.translate(0f, 2f/48, 0f);
//            GlStateManager.rotate(90f, 1f, 0f, 0f);
//            if (halfSize)
//                GlStateManager.scale(0.5F, 0.5F, 0.5F);
//            this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
//
//        }
        //this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        //this.renderItem.renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
