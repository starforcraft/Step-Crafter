package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;
import com.ultramega.stepcrafter.common.support.PatternMinMax;

public interface StepCraftingParentContainer {
    void add(StepCrafterProvider provider, PatternMinMax pattern, int priority);

    void remove(StepCrafterProvider provider, PatternMinMax pattern);

    void update(PatternMinMax pattern, int priority);

    void taskAdded(StepCrafterProvider provider, StepTask task);

    void taskRemoved(StepTask task);

    void taskCompleted(StepTask task);

    void taskChanged(StepTask task);
}
