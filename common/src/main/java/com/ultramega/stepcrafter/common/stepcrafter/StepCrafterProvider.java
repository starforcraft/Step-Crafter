package com.ultramega.stepcrafter.common.stepcrafter;

public interface StepCrafterProvider {
    void onAddedIntoContainer(StepCraftingParentContainer parentContainer);

    void onRemovedFromContainer(StepCraftingParentContainer parentContainer);
}
