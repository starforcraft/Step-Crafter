package com.ultramega.stepcrafter.common.stepcrafter.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

public interface StepTaskStatusListener {
    void taskStatusChanged(StepTaskStatus status);

    void taskRemoved(TaskId id);

    void taskAdded(StepTaskStatus status);
}
