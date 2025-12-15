package com.ultramega.stepcrafter.common;

import net.minecraft.network.chat.MutableComponent;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public final class ContentNames {
    public static final MutableComponent STEP_CRAFTER = name("step_crafter");
    public static final MutableComponent STEP_REQUESTER = name("step_requester");
    public static final MutableComponent STEP_CRAFTER_MANAGER = name("step_crafter_manager");
    public static final MutableComponent STEP_REQUESTER_MANAGER = name("step_requester_manager");

    private ContentNames() {
    }

    private static MutableComponent name(final String name) {
        return createStepCrafterTranslation("block", name);
    }
}
