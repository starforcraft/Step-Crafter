package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Optional;

public interface StepTaskProvider {
    Optional<TaskId> stepcrafter$startStepCraftingTask(ResourceKey resource, long amount, Actor actor, boolean notify,
                                                       CancellationToken cancellationToken);
}
