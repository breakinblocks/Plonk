package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.client.util.RenderUtils;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems.ItemMeta;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ShulkerBoxTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

import javax.vecmath.Matrix4f;

import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_BLOCK;
import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_ITEM;

public class TESRPlacedItems extends TileEntityRenderer<TilePlacedItems> {

    public static final DirectionProperty FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ItemRenderer itemRenderer = mc.getItemRenderer();

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder bufferbuilder = tessellator.getBuffer();
    private static final BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
    private static final double EPS = 0.001;

    public static int getRenderTypeFromStack(ItemStack itemstack) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(itemstack, null, null);
        Matrix4f matrixFixed = RenderUtils.getModelTransformMatrix(model, ItemCameraTransforms.TransformType.FIXED);
        Matrix4f matrixGui = RenderUtils.getModelTransformMatrix(model, ItemCameraTransforms.TransformType.GUI);
        Matrix4f difference = MatrixUtils.difference(matrixFixed, matrixGui);
        MatrixUtils.TransformData transform = new MatrixUtils.TransformData(difference);

        // Check difference between item frame vs how it's displayed in the inventory to see?
        // Scaling Factor
        double hS = Math.abs((transform.sx + transform.sy + transform.sz) / 3.0);
        // Rotation angles
        double hRotP = (360 + transform.pitch) % 90;
        hRotP = Math.min(Math.abs(hRotP), Math.abs(hRotP - 90));
        double hRotY = (360 + transform.yaw) % 90;
        hRotY = Math.min(Math.abs(hRotY), Math.abs(hRotY - 90));
        double hRotR = (360 + transform.roll) % 90;
        hRotR = Math.min(Math.abs(hRotR), Math.abs(hRotR - 90));
        double hRot = hRotP + hRotY + hRotR;
        //String message = String.format("hS=%.3f hRot=%.3f\n", hS, hRot) + transform.toString();
        //for (String line : message.split("\r?\n"))
        //    Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(line));
        // The following is a heuristic
        // Use block rendering: hRot ~= 83.80586
        final double blockRot = 83.80586;
        if (blockRot - EPS <= hRot && hRot <= blockRot + EPS)
            return RENDER_TYPE_BLOCK;
        return RENDER_TYPE_ITEM;
    }

    /**
     * @see ShulkerBoxTileEntityRenderer
     */
    @Override
    public void render(TilePlacedItems tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        // See Shulker Box Renderer
        Direction facing = Direction.UP;
        if (tileEntityIn.hasWorld()) {
            BlockState blockstate = tileEntityIn.getBlockState();
            if (blockstate.getBlock() instanceof BlockPlacedItems) {
                facing = blockstate.get(FACING);
            }
        }

        GlStateManager.pushMatrix();
        // Centre of Block
        GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);

        //Rotate Facing
        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                GlStateManager.rotated(180, 1.0, 0.0, 0.0);
                GlStateManager.rotated(180, 0.0, 1.0, 0.0);
                break;
            case SOUTH: // NORTH
                GlStateManager.rotated(90, 1.0, 0.0, 0.0);
                break;
            case NORTH: // SOUTH
                GlStateManager.rotated(90, 1.0, 0.0, 0.0);
                GlStateManager.rotated(180, 0.0, 0.0, 1.0);
                break;
            case EAST: // WEST
                GlStateManager.rotated(90, 1.0, 0.0, 0.0);
                GlStateManager.rotated(-90, 0.0, 0.0, 1.0);
                break;
            case WEST: // EAST
                GlStateManager.rotated(90, 1.0, 0.0, 0.0);
                GlStateManager.rotated(90, 0.0, 0.0, 1.0);
                break;
        }

        GlStateManager.translated(0.0, -0.5, 0.0);

        //Rotate about axis
        GlStateManager.rotated(-tileEntityIn.getTileRotationAngle(), 0.0, 1.0, 0.0);

        ItemStack[] contents = tileEntityIn.getContentsDisplay();
        ItemMeta[] contentsMeta = tileEntityIn.getContentsMeta();
        int num = contents.length;

        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                ItemStack stack = tileEntityIn.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                GlStateManager.pushMatrix();
                switch (num) {
                    case 1:
                        // No shift
                        break;
                    case 2:
                        // Shift left for slot 0
                        if (slot == 0) {
                            GlStateManager.translated(-0.25, 0.0, 0.0);
                        } else {
                            GlStateManager.translated(0.25, 0.0, 0.0);
                        }
                        break;
                    default:
                        boolean left = slot % 2 == 0;
                        boolean top = (slot / 2) == 0;
                        GlStateManager.translated(left ? -0.25 : 0.25, 0.0, top ? -0.25 : 0.25);
                        break;
                }
                //renderStack(world, stack, partialTicks, halfSize, world.rand.nextBoolean());
                renderStack(partialTicks, stack, contentsMeta[slot], halfSize);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param partialTicks fractional tick
     * @param stack        ItemStack to render
     * @param meta         Metadata for the stack
     * @param halfSize     If items should be rendered at half size (blocks are always rendered half size)
     * @see ItemFrameRenderer#doRender(ItemFrameEntity, double, double, double, float, float)
     */
    public void renderStack(float partialTicks, ItemStack stack, ItemMeta meta, boolean halfSize) {
        if (stack.isEmpty()) return;
        GlStateManager.pushMatrix();
        //GlStateManager.disableLighting();

        // Rotate item
        GlStateManager.rotated(-meta.getRotationAngle(), 0.0, 1.0, 0.0);

        // GROUND
        //matrixStackIn.translate(0f, -0.125f, 0f);
        //matrixStackIn.scale(2F, 2F, 2F);

        // FIXED
        GlStateManager.rotatef(180f, 0f, 1f, 0f);

        //GlStateManager.pushLightingAttributes();
        //RenderHelper.enableStandardItemLighting();
        switch (meta.renderType) {
            case RENDER_TYPE_BLOCK: {
                GlStateManager.translatef(0f, 0.25f, 0f);

                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            }
            break;
            case RENDER_TYPE_ITEM:
            default:
                GlStateManager.translatef(0f, 2f / 48, 0f);
                GlStateManager.rotatef(90f, 1f, 0f, 0f);
                if (halfSize)
                    GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        }
        //RenderHelper.disableStandardItemLighting();
        //GlStateManager.popAttributes();

        //GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
