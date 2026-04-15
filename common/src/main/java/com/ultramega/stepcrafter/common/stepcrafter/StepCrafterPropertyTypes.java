package com.ultramega.stepcrafter.common.stepcrafter;

import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

final class StepCrafterPropertyTypes {
    static final PropertyType<Integer> PRIORITY = PropertyTypes.createIntegerProperty(
        createStepCrafterIdentifier("step_crafter_priority")
    );

    static final PropertyType<Boolean> VISIBLE_TO_THE_STEP_CRAFTER_MANAGER = PropertyTypes.createBooleanProperty(
        createStepCrafterIdentifier("visible_to_the_step_crafter_manager")
    );

    static final PropertyType<Boolean> INSERT_INTO_POINTED_CONTAINER = PropertyTypes.createBooleanProperty(
        createStepCrafterIdentifier("insert_into_pointed_container")
    );

    private StepCrafterPropertyTypes() {
    }
}
