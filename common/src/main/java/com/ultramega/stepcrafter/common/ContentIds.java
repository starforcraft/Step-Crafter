package com.ultramega.stepcrafter.common;

import net.minecraft.resources.Identifier;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public final class ContentIds {
    public static final Identifier STEP_CRAFTER = createStepCrafterIdentifier("step_crafter");
    public static final Identifier STEP_REQUESTER = createStepCrafterIdentifier("step_requester");
    public static final Identifier STEP_CRAFTER_MANAGER = createStepCrafterIdentifier("step_crafter_manager");
    public static final Identifier STEP_CRAFTING_MONITOR = createStepCrafterIdentifier("step_crafting_monitor");
    public static final Identifier STEP_REQUESTER_MANAGER = createStepCrafterIdentifier("step_requester_manager");
    public static final Identifier SLOT_UPGRADE = createStepCrafterIdentifier("slot_upgrade");
    public static final Identifier STEP_CRAFTING_PREVIEW = createStepCrafterIdentifier("step_crafting_preview");

    private ContentIds() {
    }
}
