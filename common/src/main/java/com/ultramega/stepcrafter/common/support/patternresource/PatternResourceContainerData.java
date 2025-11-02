package com.ultramega.stepcrafter.common.support.patternresource;

import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PatternResourceContainerData(List<Optional<ResourceMinMaxAmount>> resources) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceContainerData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceMinMaxAmount.STREAM_OPTIONAL_CODEC),
            PatternResourceContainerData::resources,
            PatternResourceContainerData::new
        );

    public static PatternResourceContainerData of(final PatternResourceContainerImpl resourceContainer) {
        final List<Optional<ResourceMinMaxAmount>> resources = new ArrayList<>();
        for (int i = 0; i < resourceContainer.size(); ++i) {
            resources.add(Optional.ofNullable(resourceContainer.get(i)));
        }
        return new PatternResourceContainerData(resources);
    }
}

