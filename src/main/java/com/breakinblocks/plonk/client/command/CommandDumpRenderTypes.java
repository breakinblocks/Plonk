package com.breakinblocks.plonk.client.command;

import com.breakinblocks.plonk.Plonk;
import com.breakinblocks.plonk.client.render.tile.TESRPlacedItems;
import com.breakinblocks.plonk.client.util.RenderUtils;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;

import javax.vecmath.Matrix4d;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandDumpRenderTypes extends CommandBase {
    private static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    private static LinkedHashSet<ItemStackRef> getAllStacks() {
        LinkedHashSet<ItemStackRef> items = new LinkedHashSet<>();

        Block.REGISTRY.forEach(block -> items.addAll(getAllStacks(Item.getItemFromBlock(block))));
        Item.REGISTRY.forEach(item -> items.addAll(getAllStacks(item)));

        return items;
    }

    private static List<ItemStackRef> getAllStacks(Item item) {
        NonNullList<ItemStack> subItems = NonNullList.create();
        item.getSubItems(net.minecraft.creativetab.CreativeTabs.SEARCH, subItems);
        return subItems.stream().map(ItemStackRef::new).collect(Collectors.toList());
    }

    /**
     * modid:item[:meta][{nbt}]
     */
    private static String describeStack(ItemStack stack) {
        StringBuilder build = new StringBuilder();
        build.append(Item.REGISTRY.getNameForObject(stack.getItem()));

        int meta = stack.getMetadata();
        if (meta != 0)
            build.append(":").append(stack.getMetadata());

        Optional.ofNullable(stack.getTagCompound())
                .ifPresent(build::append);
        return build.toString();
    }

    /**
     * For each transform, it'll describe the (translation, scale, rotation) and (hS, hRot)
     */
    private static Stream<Map.Entry<String, String>> getRenderData(ItemStack stack) {
        IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, null);
        TransformType[] types = new TransformType[]{
                TransformType.FIXED,
                TransformType.GUI
        };
        Map<String, Matrix4d> baseTransforms = Arrays.stream(types).collect(Collectors.toMap(
                type -> type.name().toLowerCase(Locale.ROOT),
                type -> new Matrix4d(RenderUtils.getModelTransformMatrix(model, type)),
                (a, b) -> b,
                LinkedHashMap::new
        ));
        Map<String, Matrix4d> transforms = new LinkedHashMap<>();
        transforms.put("fixed->gui", MatrixUtils.difference(baseTransforms.get("fixed"), baseTransforms.get("gui")));
        transforms.putAll(baseTransforms);

        return transforms.entrySet().stream()
                .flatMap(kv -> {
                    final String transformName = kv.getKey();
                    return describeTransform(kv.getValue()).entrySet().stream()
                            .map(e -> new AbstractMap.SimpleEntry<>(transformName + "." + e.getKey(), e.getValue()));
                });
    }

    /**
     * translation, scale, rotation
     * hS: average scale
     * hRot: sum of the angle differences from a multiple of 90 (min 0, max 135)
     */
    private static LinkedHashMap<String, String> describeTransform(Matrix4d mat) {
        MatrixUtils.TransformData transform = new MatrixUtils.TransformData(mat);
        LinkedHashMap<String, String> description = new LinkedHashMap<>();
        description.put("tx", String.valueOf(transform.tx));
        description.put("ty", String.valueOf(transform.ty));
        description.put("tz", String.valueOf(transform.tz));
        description.put("sx", String.valueOf(transform.sx));
        description.put("sy", String.valueOf(transform.sy));
        description.put("sz", String.valueOf(transform.sz));
        description.put("yaw", String.valueOf(transform.yaw));
        description.put("pitch", String.valueOf(transform.pitch));
        description.put("roll", String.valueOf(transform.roll));
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
        description.put("hS", String.valueOf(hS));
        description.put("hRot", String.valueOf(hRot));
        return description;
    }

    @Override
    public String getName() {
        return "dumprt";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/cplonk dumprt - Dumps render type information";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // renderData -> *itemDesc
        Multimap<String, String> data = LinkedListMultimap.create();

        final String[] renderDataHeadersTemp = {""};
        getAllStacks().stream().map(ref -> ref.stack).forEachOrdered(stack -> {
            LinkedHashMap<String, String> renderData = Stream.concat(
                    Stream.of(new AbstractMap.SimpleEntry<>("renderType", String.valueOf(TESRPlacedItems.getRenderTypeFromStack(stack)))),
                    getRenderData(stack)
            ).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> b,
                    LinkedHashMap::new
            ));
            if (renderDataHeadersTemp[0].isEmpty())
                renderDataHeadersTemp[0] = String.join("\t", renderData.keySet());
            data.put(String.join("\t", renderData.values()), describeStack(stack));
        });
        final String renderDataHeaders = renderDataHeadersTemp[0];

        if (data.isEmpty()) {
            sender.sendMessage(new TextComponentString("No data"));
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append(renderDataHeaders);
        output.append("\tstacks");
        data.keySet().forEach(k -> output
                .append("\n").append(k)
                .append("\t").append(String.join(",", data.get(k)))
        );
        Plonk.LOG.info(output);
        sender.sendMessage(new TextComponentString(
                "Render Type Data dumped (see logs)"
                        + "\nUnique transforms: " + data.keySet().size()
                        + "\nNum Stacks: " + data.size()
        ));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Based on item, meta and nbt only
     */
    private static class ItemStackRef {

        public final ItemStack stack;

        public ItemStackRef(Item item) {
            this(new ItemStack(item));
        }

        public ItemStackRef(ItemStack stack) {
            this.stack = stack.copy();
            this.stack.setCount(Math.min(stack.getCount(), 1));
        }

        @Override
        public int hashCode() {
            int result = stack.getItem().hashCode();
            result = 31 * result + stack.getMetadata();
            NBTTagCompound tag = stack.getTagCompound();
            result = 31 * result + (tag == null ? 7 : tag.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ItemStackRef)) return false;
            ItemStackRef o = (ItemStackRef) obj;
            if (stack.getItem() != o.stack.getItem()) return false;
            if (stack.getMetadata() != o.stack.getMetadata()) return false;
            return Objects.equals(stack.getTagCompound(), o.stack.getTagCompound());
        }
    }
}
