package com.ultramega.stepcrafter.common.support;

import java.util.Locale;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ResourceStatus implements StringRepresentable {
    FINISHED,
    CRAFTING,
    NOT_ENOUGH_INGREDIENTS,
    NETWORK_FULL,
    EXTERNAL_CONTAINER_FULL;

    public static final Codec<ResourceStatus> CODEC = StringRepresentable.fromEnum(ResourceStatus::values);

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.toString();
    }
}
