package com.ultramega.stepcrafter.common.stepcrafter.task;

import java.util.Locale;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum StepTaskState implements StringRepresentable {
    READY,
    RUNNING,
    NOT_ENOUGH_INGREDIENTS,
    NETWORK_FULL,
    COMPLETED;

    public static final IntFunction<StepTaskState> BY_ID = ByIdMap.continuous(
        StepTaskState::ordinal,
        StepTaskState.values(),
        ByIdMap.OutOfBoundsStrategy.ZERO
    );

    public static final StreamCodec<ByteBuf, StepTaskState> STREAM_CODEC = ByteBufCodecs.idMapper(StepTaskState.BY_ID, StepTaskState::ordinal);

    public static final Codec<StepTaskState> CODEC = StringRepresentable.fromEnum(StepTaskState::values);

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.toString();
    }
}
