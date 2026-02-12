package com.ultramega.stepcrafter.common.stepcrafter.status;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import java.util.List;

public interface StepTaskStatusProvider {
    List<StepTaskStatus> getStatuses();

    void addListener(StepTaskStatusListener listener);

    void removeListener(StepTaskStatusListener listener);

    void cancel(TaskId taskId);

    void cancelAll();
}
