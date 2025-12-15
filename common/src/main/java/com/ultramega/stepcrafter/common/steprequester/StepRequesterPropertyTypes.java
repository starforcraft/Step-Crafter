package com.ultramega.stepcrafter.common.steprequester;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

final class StepRequesterPropertyTypes {
    static final PropertyType<Boolean> VISIBLE_TO_THE_STEP_REQUESTER_MANAGER = PropertyTypes.createBooleanProperty(
        createStepCrafterIdentifier("visible_to_the_step_requester_manager")
    );

    private StepRequesterPropertyTypes() {
    }
}
