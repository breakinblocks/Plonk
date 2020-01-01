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

import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_BLOCK;
import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_ITEM;

public class TESRPlacedItems extends TileEntitySpecialRenderer<TilePlacedItems> {

    public static final PropertyDirection FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final RenderItem renderItem = mc.getRenderItem();

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder bufferbuilder = tessellator.getBuffer();
    private static final BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();

    public TESRPlacedItems() {
    }

    public static int getRenderTypeFromStack(ItemStack itemstack) {
        IBakedModel model = renderItem.getItemModelWithOverrides(itemstack, null, null);
        Matrix4f matrixFixed = model.handlePerspective(ItemCameraTransforms.TransformType.FIXED).getRight();
        if (matrixFixed == null) {
            matrixFixed = new Matrix4f();
            matrixFixed.setIdentity();
        }
        Matrix4f matrixGui = model.handlePerspective(ItemCameraTransforms.TransformType.GUI).getRight();
        if (matrixGui == null) {
            matrixGui = new Matrix4f();
            matrixGui.setIdentity();
        }

        Matrix4d difference = MatrixUtils.difference(new Matrix4d(matrixFixed), new Matrix4d(matrixGui));
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
        //    Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString(line));
        // Change in scaling is > 1 and change in rotation at non-right angles;
        return hS > (1.0 - 0.001) && hRot > 0.001 ? RENDER_TYPE_BLOCK : RENDER_TYPE_ITEM;
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
        int[] contentsRenderType = te.getContentsRenderType();
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
                renderStack(te.getWorld(), stack, contentsRenderType[slot], partialTicks, halfSize);
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
    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param world        Client world
     * @param stack        ItemStack to render
     * @param renderType   renderType
     * @param partialTicks fractional tick
     * @param halfSize     If items should be rendered at half size (blocks are always rendered half size)
     */
    public void renderStack(World world, ItemStack stack, int renderType, float partialTicks, boolean halfSize) {
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
        switch (renderType) {
            case RENDER_TYPE_BLOCK: {
                GlStateManager.translate(0f, 0.25f, 0f);
                renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            }
            break;
            case RENDER_TYPE_ITEM:
            default:
                GlStateManager.translate(0f, 2f / 48, 0f);
                GlStateManager.rotate(90f, 1f, 0f, 0f);
                if (halfSize)
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                renderItem.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
