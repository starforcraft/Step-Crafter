package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StepCraftingMonitorData(List<StepTaskStatus> statuses, boolean active) {
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingMonitorData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, StepTaskStatus.STREAM_CODEC),
        StepCraftingMonitorData::statuses,
        ByteBufCodecs.BOOL, StepCraftingMonitorData::active,
        StepCraftingMonitorData::new
    );
}
