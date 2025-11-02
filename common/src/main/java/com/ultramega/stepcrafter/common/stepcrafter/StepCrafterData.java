package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerData;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record StepCrafterData(AutocrafterData autocrafterData, PatternResourceContainerData patternResources) {
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCrafterData> STREAM_CODEC = StreamCodec.composite(
        AutocrafterData.STREAM_CODEC, StepCrafterData::autocrafterData,
        PatternResourceContainerData.STREAM_CODEC, StepCrafterData::patternResources,
        StepCrafterData::new
    );
}
