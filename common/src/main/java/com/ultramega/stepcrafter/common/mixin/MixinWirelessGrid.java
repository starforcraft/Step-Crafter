package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.network.stepcrafter.StepCraftingNetworkComponent;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskProvider;
import com.ultramega.stepcrafter.common.support.NetworkGetter;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;

import java.util.Optional;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.refinedmods.refinedstorage.common.grid.WirelessGrid")
public class MixinWirelessGrid implements NetworkGetter, StepTaskProvider {
    @Shadow(remap = false)
    @Final
    private NetworkItemContext context;

    @Nullable
    @Override
    public Network stepcrafter$getNetwork() {
        return this.context.resolveNetwork().orElse(null);
    }

    @Override
    @Unique
    public Optional<TaskId> stepcrafter$startStepCraftingTask(final ResourceKey resource,
                                                              final long amount,
                                                              final Actor actor,
                                                              final boolean notify,
                                                              final CancellationToken cancellationToken) {
        final Network network = this.stepcrafter$getNetwork();
        if (network != null) {
            network.getComponent(StepCraftingNetworkComponent.class).stepcrafter$startStepCraftingTask(resource, amount, actor, notify, cancellationToken);
        }

        return Optional.empty();
    }
}
