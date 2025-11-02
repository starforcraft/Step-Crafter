package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StepRequesterData(PatternResourceContainerData filterContainerData) {
    public static final StreamCodec<RegistryFriendlyByteBuf, StepRequesterData> STREAM_CODEC = StreamCodec.composite(
        PatternResourceContainerData.STREAM_CODEC, StepRequesterData::filterContainerData,
        StepRequesterData::new
    );
}
