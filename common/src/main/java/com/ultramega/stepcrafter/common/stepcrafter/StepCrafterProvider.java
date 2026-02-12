package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import java.util.List;

public interface StepCrafterProvider {
    void onAddedIntoContainer(StepCraftingParentContainer parentContainer);

    void onRemovedFromContainer(StepCraftingParentContainer parentContainer);

    void addTask(StepTask task);

    void cancelTask(TaskId taskId);

    List<StepTaskStatus> getTaskStatuses();
}
