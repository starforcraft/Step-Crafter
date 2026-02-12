package com.ultramega.stepcrafter.common.network.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatusProvider;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskProvider;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.network.NetworkComponent;

import java.util.Set;

public interface StepCraftingNetworkComponent extends NetworkComponent, StepTaskProvider, StepTaskStatusProvider {
    Set<ResourceMinMaxAmount> getOutputs();
}
