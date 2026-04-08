package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.client.render.tile.state.PlacedItemsRenderState;
import com.breakinblocks.plonk.client.util.RenderUtils;
import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems.ItemMeta;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_BLOCK;
import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_ITEM;

/***
 * @see net.minecraft.client.renderer.blockentity.ShelfRenderer
 * @see net.minecraft.client.renderer.blockentity.CampfireRenderer
 */
public class TESRPlacedItems implements BlockEntityRenderer<TilePlacedItems, PlacedItemsRenderState> {

    public static final EnumProperty<Direction> FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getInstance();
    private static final BlockModelResolver blockModelResolver = mc.getBlockModelResolver();
    private static final ItemModelResolver itemModelResolver = mc.getItemModelResolver();

    private static final double EPS = 0.001;

    public TESRPlacedItems(BlockEntityRendererProvider.Context context) {
    }

    public static int getRenderTypeFromStack(ItemStack itemstack, @Nullable Level level, @Nullable ItemOwner itemOwner, int seed) {
        Matrix4f matrixFixed = RenderUtils.getModelTransformMatrix(itemstack, ItemDisplayContext.FIXED, level, itemOwner, seed);
        Matrix4f matrixGui = RenderUtils.getModelTransformMatrix(itemstack, ItemDisplayContext.GUI, level, itemOwner, seed);
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

    @Override
    public PlacedItemsRenderState createRenderState() {
        return new PlacedItemsRenderState();
    }

    @Override
    public void extractRenderState(TilePlacedItems blockEntity, PlacedItemsRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@org.jspecify.annotations.Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        int seed = (int) blockEntity.getBlockPos().asLong();
        state.direction = blockEntity.getBlockState().getValueOrElse(BlockPlacedItems.FACING, Direction.UP);
        state.tileRotationAngle = blockEntity.getTileRotationAngle();
        int size = blockEntity.getContainerSize();
        ItemMeta[] itemMetas = blockEntity.getContentsMeta();
        state.items = new ItemStackRenderState[size];
        state.itemMetas = new ItemMeta[size];
        for (int slot = 0; slot < size; slot++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, blockEntity.getItem(slot), ItemDisplayContext.FIXED, blockEntity.level(), blockEntity, seed + slot);
            state.items[slot] = itemState;
            state.itemMetas[slot] = itemMetas.length <= size ? itemMetas[slot] : ItemMeta.DEFAULT;
            if (!state.items[slot].isEmpty()) {
                state.numItems = slot + 1;
            }
        }
    }

    /**
     * @see ShulkerBoxRenderer
     */
    @Override
    public void submit(PlacedItemsRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Direction facing = state.direction;
        poseStack.pushPose();
        // Centre of Block
        poseStack.translate(0.5, 0.5, 0.5);

        //Rotate Facing
        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                //GL11.glRotated(180, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 1.0, 0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case SOUTH: // NORTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                break;
            case NORTH: // SOUTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 0.0, 1.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                break;
            case EAST: // WEST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(-90, 0.0, 0.0, 1.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
                break;
            case WEST: // EAST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(90, 0.0, 0.0, 1.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
                break;
        }

        poseStack.translate(0.0, -0.5, 0.0);

        //Rotate about axis
        poseStack.mulPose(Axis.YP.rotationDegrees((float) -state.tileRotationAngle));
        ItemStackRenderState[] contents = state.items;
        ItemMeta[] contentsMeta = state.itemMetas;
        int num = state.numItems;
        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                ItemStackRenderState stack = contents[slot];
                if (stack.isEmpty()) continue;
                poseStack.pushPose();
                switch (num) {
                    case 1:
                        // No shift
                        break;
                    case 2:
                        // Shift left for slot 0
                        if (slot == 0) {
                            poseStack.translate(-0.25, 0.0, 0.0);
                        } else {
                            poseStack.translate(0.25, 0.0, 0.0);
                        }
                        break;
                    default:
                        boolean left = slot % 2 == 0;
                        boolean top = (slot / 2) == 0;
                        poseStack.translate(left ? -0.25 : 0.25, 0.0, top ? -0.25 : 0.25);
                        break;
                }
                //renderStack(world, stack, partialTicks, halfSize, world.rand.nextBoolean());
                //renderStack(partialTicks, matrixStackIn, bufferIn, combinedLightIn, level, seed + slot, stack, contentsMeta[slot], halfSize);
                renderStack(poseStack, submitNodeCollector, state.lightCoords, stack, contentsMeta[slot], halfSize);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param poseStack           transformation matrix stack
     * @param submitNodeCollector idk what this is
     * @param stack               ItemStack to render
     * @param meta                Metadata for the stack
     * @param halfSize            If items should be rendered at half size (blocks are always rendered half size)
     */
    public void renderStack(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemStackRenderState stack, ItemMeta meta, boolean halfSize) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        //GlStateManager.disableLighting();

        // Rotate item
        //GL11.glRotated(-meta.getRotationAngle(), 0.0, 1.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) -meta.getRotationAngle()));

        // GROUND
        //matrixStackIn.translate(0f, -0.125f, 0f);
        //matrixStackIn.scale(2F, 2F, 2F);

        // FIXED
        //GlStateManager.rotate(180f, 0f, 1f, 0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        //GlStateManager.pushLightingAttributes();
        //RenderHelper.enableStandardItemLighting();
        switch (meta.renderType) {
            case RENDER_TYPE_BLOCK: {
                poseStack.translate(0f, 0.25f, 0f);
            }
            break;
            case RENDER_TYPE_ITEM:
            default:
                poseStack.translate(0f, 2f / 48, 0f);
                //GlStateManager.rotate(90f, 1f, 0f, 0f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                if (halfSize)
                    poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        stack.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        //RenderHelper.disableStandardItemLighting();
        //GlStateManager.popAttributes();

        //GlStateManager.enableLighting();
        poseStack.popPose();
    }
}
