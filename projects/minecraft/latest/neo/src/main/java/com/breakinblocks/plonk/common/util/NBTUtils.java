package com.breakinblocks.plonk.common.util;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;

import java.util.Optional;

public class NBTUtils {
    public static Logger LOGGER = LogUtils.getLogger();

    public static CompoundTag toTag(ValueInput input) {
        CompoundTag output = new CompoundTag();
        for (String key : input.keySet()) {
            Optional<CompoundTag> compound = input.child(key).map(NBTUtils::toTag);
            if (compound.isPresent()) {
                output.put(key, compound.get());
                continue;
            }

            // Where's byte and long arrays? I guess we just... Can't get them?
            Optional<int[]> intArray = input.getIntArray(key);
            if (intArray.isPresent()) {
                output.putIntArray(key, intArray.get());
                continue;
            }

            Optional<ValueInput.ValueInputList> list = input.childrenList(key);
            if (list.isPresent()) {
                ListTag listTag = new ListTag();
                list.get().stream().map(NBTUtils::toTag).forEach(listTag::add);
                output.put(key, listTag);
                continue;
            }

            // Why are there so many missing optionals?
            short shortValue = (short) input.getShortOr(key, (short) -1);
            if (shortValue == (short) input.getShortOr(key, (short) 0)) {
                output.putShort(key, shortValue);
                continue;
            }

            double doubleValue = input.getDoubleOr(key, -1);
            if (doubleValue == input.getDoubleOr(key, 0)) {
                output.putDouble(key, doubleValue);
                continue;
            }

            float floatValue = input.getFloatOr(key, -1);
            if (floatValue == input.getFloatOr(key, 0)) {
                output.putFloat(key, floatValue);
                continue;
            }

            byte byteValue = input.getByteOr(key, (byte) -1);
            if (byteValue == input.getByteOr(key, (byte) 0)) {
                output.putByte(key, byteValue);
                continue;
            }

            Optional<Integer> maybeInt = input.getInt(key);
            if (maybeInt.isPresent()) {
                output.putInt(key, maybeInt.get());
                continue;
            }

            Optional<Long> maybeLong = input.getLong(key);
            if (maybeLong.isPresent()) {
                output.putLong(key, maybeLong.get());
                continue;
            }

            Optional<String> maybeString = input.getString(key);
            if (maybeString.isPresent()) {
                output.putString(key, maybeString.get());
                continue;
            }

            LOGGER.warn("Unknown value associated with key: {}", key);
        }

        return output;
    }
}
