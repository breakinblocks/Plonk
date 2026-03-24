package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.client.util.RenderUtils;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems.ItemMeta;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_BLOCK;
import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_ITEM;

public class TESRPlacedItems implements BlockEntityRenderer<TilePlacedItems> {

    public static final DirectionProperty FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ItemRenderer itemRenderer = mc.getItemRenderer();

    private static final double EPS = 0.001;

    public TESRPlacedItems(BlockEntityRendererProvider.Context context) {
    }

    public static int getRenderTypeFromStack(ItemStack itemstack) {
        BakedModel model = itemRenderer.getModel(itemstack, null, null, 0);
        Matrix4f matrixFixed = RenderUtils.getModelTransformMatrix(model, ItemDisplayContext.FIXED);
        Matrix4f matrixGui = RenderUtils.getModelTransformMatrix(model, ItemDisplayContext.GUI);
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
        //    Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new TextComponent(line));
        // The following is a heuristic
        // Use block rendering: hRot ~= 75
        final double blockRot = 75;
        if (blockRot - EPS <= hRot && hRot <= blockRot + EPS)
            return RENDER_TYPE_BLOCK;
        return RENDER_TYPE_ITEM;
    }

    @Override
    public AABB getRenderBoundingBox(TilePlacedItems blockEntity) {
        // TODO: This doesn't work properly for custom item renders... since they can go outside the normal bounds
        // TODO: Maybe find out a way to get the render bounding boxes for each of the items??? Bit worse fps for now...
        // return blockEntity.contentsBoxes.getRenderBoundingBox(blockEntity);
        return AABB.INFINITE;
    }

    /**
     * @see ShulkerBoxRenderer
     */
    @Override
    public void render(TilePlacedItems tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        // See Shulker Box Renderer
        Direction facing = Direction.UP;
        if (tileEntityIn.hasLevel()) {
            BlockState blockstate = tileEntityIn.getBlockState();
            if (blockstate.getBlock() instanceof BlockPlacedItems) {
                facing = blockstate.getValue(FACING);
            }
        }

        matrixStackIn.pushPose();
        // Centre of Block
        //matrixStackIn.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        matrixStackIn.translate(0.5, 0.5, 0.5);

        //Rotate Facing
        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                //GL11.glRotated(180, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 1.0, 0.0);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(180));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case SOUTH: // NORTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
                break;
            case NORTH: // SOUTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 0.0, 1.0);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(180));
                break;
            case EAST: // WEST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(-90, 0.0, 0.0, 1.0);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-90));
                break;
            case WEST: // EAST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(90, 0.0, 0.0, 1.0);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(90));
                break;
        }

        matrixStackIn.translate(0.0, -0.5, 0.0);

        //Rotate about axis
        matrixStackIn.mulPose(Axis.YP.rotationDegrees((float) -tileEntityIn.getTileRotationAngle()));

        ItemStack[] contents = tileEntityIn.getContentsDisplay();
        ItemMeta[] contentsMeta = tileEntityIn.getContentsMeta();
        int num = contents.length;

        Level level = tileEntityIn.getLevel();
        int seed = (int) tileEntityIn.getBlockPos().asLong();
        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                ItemStack stack = tileEntityIn.getItem(slot);
                if (stack.isEmpty()) continue;
                matrixStackIn.pushPose();
                switch (num) {
                    case 1:
                        // No shift
                        break;
                    case 2:
                        // Shift left for slot 0
                        if (slot == 0) {
                            matrixStackIn.translate(-0.25, 0.0, 0.0);
                        } else {
                            matrixStackIn.translate(0.25, 0.0, 0.0);
                        }
                        break;
                    default:
                        boolean left = slot % 2 == 0;
                        boolean top = (slot / 2) == 0;
                        matrixStackIn.translate(left ? -0.25 : 0.25, 0.0, top ? -0.25 : 0.25);
                        break;
                }
                //renderStack(world, stack, partialTicks, halfSize, world.rand.nextBoolean());
                renderStack(partialTicks, matrixStackIn, bufferIn, combinedLightIn, level, seed + slot, stack, contentsMeta[slot], halfSize);
                matrixStackIn.popPose();
            }
        }

        matrixStackIn.popPose();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param partialTicks    fractional tick
     * @param matrixStackIn   transformation matrix stack
     * @param bufferIn        render type buffer
     * @param combinedLightIn Block and Sky light combined
     * @param level           Level the stack is being rendered in
     * @param seed            Random seed for rendering
     * @param stack           ItemStack to render
     * @param meta            Metadata for the stack
     * @param halfSize        If items should be rendered at half size (blocks are always rendered half size)
     * @see ItemFrameRenderer#render(ItemFrame, float, float, PoseStack, MultiBufferSource, int)
     */
    public void renderStack(float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, @Nullable Level level, int seed, ItemStack stack, ItemMeta meta, boolean halfSize) {
        if (stack.isEmpty()) return;
        matrixStackIn.pushPose();
        //GlStateManager.disableLighting();

        // Rotate item
        //GL11.glRotated(-meta.getRotationAngle(), 0.0, 1.0, 0.0);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees((float) -meta.getRotationAngle()));

        // GROUND
        //matrixStackIn.translate(0f, -0.125f, 0f);
        //matrixStackIn.scale(2F, 2F, 2F);

        // FIXED
        //GlStateManager.rotate(180f, 0f, 1f, 0f);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180f));

        //GlStateManager.pushLightingAttributes();
        //RenderHelper.enableStandardItemLighting();
        switch (meta.renderType) {
            case RENDER_TYPE_BLOCK: {
                matrixStackIn.translate(0f, 0.25f, 0f);

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, level, seed);
            }
            break;
            case RENDER_TYPE_ITEM:
            default:
                matrixStackIn.translate(0f, 2f / 48, 0f);
                //GlStateManager.rotate(90f, 1f, 0f, 0f);
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(90f));
                if (halfSize)
                    matrixStackIn.scale(0.5F, 0.5F, 0.5F);
                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, level, seed);
        }
        //RenderHelper.disableStandardItemLighting();
        //GlStateManager.popAttributes();

        //GlStateManager.enableLighting();
        matrixStackIn.popPose();
    }
}
