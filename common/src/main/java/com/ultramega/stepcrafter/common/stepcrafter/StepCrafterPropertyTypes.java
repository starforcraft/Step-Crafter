package com.ultramega.stepcrafter.common.stepcrafter;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

final class StepCrafterPropertyTypes {
    /*static final PropertyType<Integer> PRIORITY = PropertyTypes.createIntegerProperty(
        createIdentifier("crafter_priority")
    );*/ //TODO

    static final PropertyType<Boolean> VISIBLE_TO_THE_STEP_CRAFTER_MANAGER = PropertyTypes.createBooleanProperty(
        createStepCrafterIdentifier("visible_to_the_step_crafter_manager")
    );

    private StepCrafterPropertyTypes() {
    }
}
