package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;

import org.jspecify.annotations.Nullable;

public interface StepCraftingMonitorListener {
    void currentTaskChanged(@Nullable StepTaskStatus taskStatus);

    void taskAdded(StepTaskStatus taskStatus);

    void taskRemoved(TaskId taskId);
}
