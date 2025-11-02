package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.support.PatternMinMax;

public interface StepCraftingParentContainer {
    void add(StepCrafterProvider provider, PatternMinMax pattern);

    void remove(StepCrafterProvider provider, PatternMinMax pattern);
}
