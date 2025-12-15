package com.ultramega.stepcrafter.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public final class Tags {
    public static final TagKey<Item> STEP_CRAFTER_MANAGERS = createTag("step_crafter_managers");
    public static final TagKey<Item> STEP_REQUESTER_MANAGERS = createTag("step_requester_managers");

    private Tags() {
    }

    private static TagKey<Item> createTag(final String id) {
        return TagKey.create(Registries.ITEM, createStepCrafterIdentifier(id));
    }
}
