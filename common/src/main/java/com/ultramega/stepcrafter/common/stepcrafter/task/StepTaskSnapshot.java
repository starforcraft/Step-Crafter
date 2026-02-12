package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

public record StepTaskSnapshot(TaskId id,
                               ResourceKey resource,
                               long amount,
                               Pattern pattern,
                               Actor actor,
                               boolean notifyActor,
                               long startTime,
                               StepTaskState state,
                               long craftedAmount,
                               boolean cancelled) {
}
