package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.mixin.AutocraftingNetworkComponentImplInvoker;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.autocrafting.PatternProvider;
import com.refinedmods.refinedstorage.api.network.impl.autocrafting.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StepRequesterNetworkNode extends SimpleNetworkNode {
    private final Actor actor = new NetworkNodeActor(this);
    private final Map<Integer, TaskId> runningTasks = new HashMap<>();

    private StepRequesterBlockEntity blockEntity;

    public StepRequesterNetworkNode(final long energyUsage) {
        super(energyUsage);
    }

    @Override
    public void doWork() {
        super.doWork();

        if (this.network == null || !this.isActive()) {
            return;
        }

        final AutocraftingNetworkComponent autocraftingComponent = this.network.getComponent(AutocraftingNetworkComponent.class);
        final StorageNetworkComponent storageComponent = this.network.getComponent(StorageNetworkComponent.class);
        final PatternResourceContainerImpl filterContainer = this.blockEntity.getFilterContainer();
        for (int i = 0; i < filterContainer.getContainerSize(); i++) {
            final ResourceMinMaxAmount resource = filterContainer.get(i);
            if (resource == null || resource.batchSize() == 0) {
                continue;
            }

            for (int j = 0; j < this.blockEntity.getSpeed() + 1; j++) {
                final long stored = storageComponent.get(resource.resource());
                if (!resource.isCrafting() && stored >= resource.minAmount()) {
                    break;
                }
                if (resource.isCrafting() && stored >= resource.maxAmount()) {
                    filterContainer.set(i, resource.toBuilder().isCrafting(false).build());
                    break;
                }

                try {
                    if (this.isAlreadyRunningTask(i, autocraftingComponent)) {
                        break;
                    }

                    final long needed = resource.maxAmount() - stored;
                    final long batchSize = Math.min(needed, resource.batchSize());

                    final Optional<TaskId> task = autocraftingComponent.startTask(resource.resource(), batchSize, this.actor, false, new TimeoutableCancellationToken());
                    this.runningTasks.put(i, task.orElse(null));
                    if (task.isEmpty()) {
                        if (resource.isCrafting()) {
                            filterContainer.set(i, resource.toBuilder().isCrafting(false).build());
                        }
                        break;
                    } else {
                        if (!resource.isCrafting()) {
                            filterContainer.set(i, resource.toBuilder().isCrafting(true).build());
                        }
                    }
                } catch (final IllegalStateException ignored) {
                    // TODO: add cooldown if task couldn't be started
                    if (resource.isCrafting()) {
                        filterContainer.set(i, resource.toBuilder().isCrafting(false).build());
                    }
                    this.runningTasks.remove(i);
                    break;
                }
            }
        }
    }

    private boolean isAlreadyRunningTask(final int index, final AutocraftingNetworkComponent autocraftingComponent) {
        if (this.runningTasks.get(index) == null) {
            return false;
        }

        if (autocraftingComponent instanceof AutocraftingNetworkComponentImplInvoker autocrafting) {
            final PatternProvider patternProvider = autocrafting.stepcrafter$getProviderByTaskId().get(this.runningTasks.get(index));
            return patternProvider != null;
        }

        return false;
    }

    public void setBlockEntity(final StepRequesterBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }
}
