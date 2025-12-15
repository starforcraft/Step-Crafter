package com.ultramega.stepcrafter.common;

import net.minecraft.resources.ResourceLocation;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public final class ContentIds {
    public static final ResourceLocation STEP_CRAFTER = createStepCrafterIdentifier("step_crafter");
    public static final ResourceLocation STEP_REQUESTER = createStepCrafterIdentifier("step_requester");
    public static final ResourceLocation STEP_CRAFTER_MANAGER = createStepCrafterIdentifier("step_crafter_manager");
    public static final ResourceLocation STEP_REQUESTER_MANAGER = createStepCrafterIdentifier("step_requester_manager");
    public static final ResourceLocation SLOT_UPGRADE = createStepCrafterIdentifier("slot_upgrade");

    private ContentIds() {
    }
}
