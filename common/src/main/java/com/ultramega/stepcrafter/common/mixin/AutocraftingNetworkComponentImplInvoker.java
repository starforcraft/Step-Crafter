package com.ultramega.stepcrafter.common.mixin;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.AutocraftingNetworkComponentImpl;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AutocraftingNetworkComponentImpl.class)
public interface AutocraftingNetworkComponentImplInvoker {
    @Accessor(value = "providerByTaskId", remap = false)
    Map<TaskId, PatternProvider> stepcrafter$getProviderByTaskId();
}
