package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskContainer;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.ResourceStatus;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternType;
import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink.Result;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.canInsertIntoNetwork;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.extractResolvedInputs;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.insertIntoNetwork;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.prepareCraft;

public class StepCrafterNetworkNode extends SimpleNetworkNode implements StepCrafterProvider {
    private final Actor actor = new NetworkNodeActor(this);
    private final Set<StepCraftingParentContainer> parents = new HashSet<>();
    private final StepTaskContainer tasks = new StepTaskContainer(this);
    private int priority;
    private final PatternMinMax[] patterns;

    private StepCrafterBlockEntity blockEntity;

    public StepCrafterNetworkNode(final long energyUsage, final int patterns) {
        super(energyUsage);
        this.patterns = new PatternMinMax[patterns];
    }

    @Override
    public void doWork() {
        super.doWork();

        if (this.network == null || !this.isActive()) {
            return;
        }

        this.tasks.craft(this.network);

        final StorageNetworkComponent storageComponent = this.network.getComponent(StorageNetworkComponent.class);
        final PatternResourceContainerImpl patternResources = this.blockEntity.getPatternResourceContainer();
        for (int i = 0; i < patternResources.getContainerSize(); i++) {
            final ResourceMinMaxAmount resource = patternResources.get(i);
            final PatternMinMax pattern = this.patterns[i];
            if (resource == null || pattern == null || pattern.pattern().layout().type() == PatternType.EXTERNAL) {
                continue;
            }

            for (int j = 0; j < this.blockEntity.getSpeed() + 1; j++) {
                // Only the first output is checked however as everything except processing patterns (which aren't allowed) only have one output
                // this *shouldn't* be an issue
                final ResourceKey resourceKey = pattern.pattern().layout().outputs().getFirst().resource();
                final long stored = storageComponent.get(resourceKey);
                final ResourceStatus status = resource.status();
                if (status != ResourceStatus.CRAFTING && stored >= resource.minAmount()) {
                    break;
                }
                if (status == ResourceStatus.CRAFTING && stored >= resource.maxAmount()) {
                    patternResources.set(i, resource.toBuilder().status(ResourceStatus.FINISHED).build());
                    break;
                }

                final ResourceStatus result = this.craftOneItemNew(storageComponent, pattern.pattern());
                if (result == ResourceStatus.NOT_ENOUGH_INGREDIENTS || result == ResourceStatus.NETWORK_FULL || result == ResourceStatus.EXTERNAL_CONTAINER_FULL) {
                    patternResources.set(i, resource.toBuilder().status(result).build());
                    break;
                } else {
                    if (status != ResourceStatus.CRAFTING) {
                        patternResources.set(i, resource.toBuilder().status(ResourceStatus.CRAFTING).build());
                    }
                }
            }
        }
    }

    private ResourceStatus craftOneItemNew(final StorageNetworkComponent storageComponent, final Pattern pattern) {
        final StepCraftingAlgorithm.CraftPreparation preparation = prepareCraft(storageComponent, pattern, null, this.actor);
        if (preparation == null) {
            return ResourceStatus.NOT_ENOUGH_INGREDIENTS;
        }

        final Result result = this.blockEntity.accept(preparation.totalResources(), Action.SIMULATE);
        if (result == Result.SKIPPED) {
            // No Sink (connected machine) so try to import into network
            if (!canInsertIntoNetwork(storageComponent, preparation.totalResources(), this.actor)) {
                return ResourceStatus.NETWORK_FULL;
            }

            extractResolvedInputs(storageComponent, preparation.resolvedInputs(), null, this.actor);
            insertIntoNetwork(storageComponent, preparation.totalResources(), null, 0, this.actor);
            return ResourceStatus.FINISHED;
        } else if (result == Result.ACCEPTED) {
            // Has Sink (connected machine) and was accepted
            extractResolvedInputs(storageComponent, preparation.resolvedInputs(), null, this.actor);
            this.blockEntity.accept(preparation.totalResources(), Action.EXECUTE);
            return ResourceStatus.FINISHED;
        }

        return ResourceStatus.EXTERNAL_CONTAINER_FULL;
    }

    public void setBlockEntity(final StepCrafterBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setPattern(final int index, @Nullable final PatternMinMax pattern) {
        final PatternMinMax oldPattern = this.patterns[index];
        if (oldPattern != null) {
            this.parents.forEach(parent -> parent.remove(this, oldPattern));
        }
        this.patterns[index] = pattern;
        if (pattern != null) {
            this.parents.forEach(parent -> parent.add(this, pattern, this.priority));
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (!newActive) {
            for (final PatternMinMax pattern : this.patterns) {
                if (pattern != null) {
                    this.parents.forEach(parent -> parent.remove(this, pattern));
                }
            }
            return;
        }
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                this.parents.forEach(parent -> parent.add(this, pattern, this.priority));
            }
        }
    }

    @Override
    public void onAddedIntoContainer(final StepCraftingParentContainer parentContainer) {
        this.parents.add(parentContainer);
        this.tasks.onAddedIntoContainer(parentContainer);
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                parentContainer.add(this, pattern, this.priority);
            }
        }
    }

    @Override
    public void onRemovedFromContainer(final StepCraftingParentContainer parentContainer) {
        this.tasks.onRemovedFromContainer(parentContainer);
        this.parents.remove(parentContainer);
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                parentContainer.remove(this, pattern);
            }
        }
    }

    @Override
    public void addTask(final StepTask task) {
        this.tasks.add(task, this.network);
        this.parents.forEach(parent -> parent.taskAdded(this, task));
    }

    @Override
    public void cancelTask(final TaskId taskId) {
        this.tasks.cancel(taskId);
    }

    @Override
    public List<StepTaskStatus> getTaskStatuses() {
        return this.tasks.getStatuses();
    }

    public List<StepTask> getTasks() {
        return this.tasks.getAll();
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
        for (final PatternMinMax pattern : this.patterns) {
            if (pattern != null) {
                this.parents.forEach(parent -> parent.update(pattern, priority));
            }
        }
    }
}
