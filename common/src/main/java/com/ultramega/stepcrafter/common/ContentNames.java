package com.ultramega.stepcrafter.common;

import net.minecraft.network.chat.MutableComponent;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public final class ContentNames {
    public static final MutableComponent STEP_CRAFTER = name("step_crafter");
    public static final MutableComponent STEP_REQUESTER = name("step_requester");

    private ContentNames() {
    }

    private static MutableComponent name(final String name) {
        return createStepCrafterTranslation("block", name);
    }
}
