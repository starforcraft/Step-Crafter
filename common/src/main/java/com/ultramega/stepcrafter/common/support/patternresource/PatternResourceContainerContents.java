package com.ultramega.stepcrafter.common.support.patternresource;

import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

public record PatternResourceContainerContents(List<Optional<ResourceMinMaxAmount>> slots) {
    public static final Codec<PatternResourceContainerContents> CODEC = Codec.list(ResourceMinMaxAmount.OPTIONAL_CODEC)
        .xmap(PatternResourceContainerContents::new, PatternResourceContainerContents::slots);

    public PatternResourceContainerContents(final List<Optional<ResourceMinMaxAmount>> slots) {
        this.slots = List.copyOf(slots);
    }

    public static PatternResourceContainerContents of(final PatternResourceContainerImpl container) {
        final List<Optional<ResourceMinMaxAmount>> slots = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            slots.add(Optional.ofNullable(container.get(i)));
        }
        return new PatternResourceContainerContents(slots);
    }
}
