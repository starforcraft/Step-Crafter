package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

public interface StepTask {
    Actor getActor();

    boolean shouldNotify();

    ResourceKey getResource();

    long getAmount();

    TaskId getId();

    StepTaskState getState();

    boolean craft(RootStorage rootStorage);

    void cancel();

    StepTaskStatus getStatus();
}
