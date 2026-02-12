package com.ultramega.stepcrafter.common.network.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterProvider;
import com.ultramega.stepcrafter.common.stepcrafter.StepCraftingParentContainer;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatusListener;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTask;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskImpl;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.ResourceStatus;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

public class StepCraftingNetworkComponentImpl implements StepCraftingNetworkComponent, StepCraftingParentContainer {
    private final Set<PatternMinMax> patterns = new HashSet<>();
    private final Set<StepCrafterProvider> providers = new HashSet<>();
    private final Map<PatternMinMax, StepCrafterProvider> providerByPattern = new HashMap<>();
    private final Map<ResourceKey, PriorityQueue<PatternHolder>> patternsByOutput = new HashMap<>();
    private final Set<ResourceMinMaxAmount> outputs = new HashSet<>();
    private final Map<TaskId, StepCrafterProvider> providerByTaskId = new HashMap<>();
    private final Set<StepTaskStatusListener> statusListeners = new HashSet<>();

    public StepCraftingNetworkComponentImpl() {
    }

    @Override
    public Optional<TaskId> stepcrafter$startStepCraftingTask(final ResourceKey resource,
                                                              final long amount,
                                                              final Actor actor,
                                                              final boolean notify,
                                                              final CancellationToken cancellationToken) {
        ResourceAmount.validate(resource, amount);
        final PriorityQueue<PatternHolder> holders = this.patternsByOutput.get(resource);
        if (holders == null) {
            return Optional.empty();
        }
        final PatternHolder holder = holders.peek();
        if (holder == null) {
            return Optional.empty();
        }

        final StepTask task = new StepTaskImpl(resource, amount, holder.pattern.pattern(), actor, notify);
        final StepCrafterProvider provider = this.providerByPattern.get(holder.pattern);
        provider.addTask(task);
        return Optional.of(task.getId());
    }

    @Override
    public void addListener(final StepTaskStatusListener listener) {
        this.statusListeners.add(listener);
    }

    @Override
    public void removeListener(final StepTaskStatusListener listener) {
        this.statusListeners.remove(listener);
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StepCrafterProvider provider) {
            provider.onAddedIntoContainer(this);
            this.providers.add(provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StepCrafterProvider provider) {
            provider.onRemovedFromContainer(this);
            this.providers.remove(provider);
        }
    }

    @Override
    public Set<ResourceMinMaxAmount> getOutputs() {
        return this.outputs;
    }

    @Override
    public List<StepTaskStatus> getStatuses() {
        return this.providers.stream().map(StepCrafterProvider::getTaskStatuses).flatMap(List::stream).toList();
    }

    @Override
    public void cancel(final TaskId taskId) {
        final StepCrafterProvider provider = this.providerByTaskId.get(taskId);
        if (provider == null) {
            return;
        }
        provider.cancelTask(taskId);
    }

    @Override
    public void cancelAll() {
        for (final Map.Entry<TaskId, StepCrafterProvider> entry : this.providerByTaskId.entrySet()) {
            final StepCrafterProvider provider = entry.getValue();
            final TaskId taskId = entry.getKey();
            provider.cancelTask(taskId);
        }
    }

    @Override
    public void add(final StepCrafterProvider provider, final PatternMinMax pattern, final int priority) {
        this.patterns.add(pattern);
        this.providerByPattern.put(pattern, provider);
        pattern.pattern().layout().outputs().forEach(output ->
            this.outputs.add(new ResourceMinMaxAmount(output.resource(), pattern.minAmount(), pattern.maxAmount(), 1L, ResourceStatus.FINISHED)));
        for (final ResourceAmount output : pattern.pattern().layout().outputs()) {
            this.patternsByOutput.computeIfAbsent(output.resource(), k -> new PriorityQueue<>(
                Comparator.comparingInt(PatternHolder::priority).reversed()
            )).add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void remove(final StepCrafterProvider provider, final PatternMinMax pattern) {
        this.patterns.remove(pattern);
        this.providerByPattern.remove(pattern);
        for (final ResourceAmount output : pattern.pattern().layout().outputs()) {
            final PriorityQueue<PatternHolder> holders = this.patternsByOutput.get(output.resource());
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            if (holders.isEmpty()) {
                this.patternsByOutput.remove(output.resource());
            }

            final boolean noOtherPatternHasThisOutput = this.patterns.stream()
                .noneMatch(otherPattern -> otherPattern.pattern().layout().outputs().stream()
                    .anyMatch(o -> o.resource().equals(output.resource()))
                    && otherPattern.minAmount() == pattern.minAmount()
                    && otherPattern.maxAmount() == pattern.maxAmount()
                    && otherPattern.batchSize() == pattern.batchSize());
            if (noOtherPatternHasThisOutput) {
                this.outputs.removeIf(r -> r.resource().equals(output.resource())
                    && r.minAmount() == pattern.minAmount()
                    && r.maxAmount() == pattern.maxAmount()
                    && r.batchSize() == pattern.batchSize());
            }
        }
    }

    @Override
    public void update(final PatternMinMax pattern, final int priority) {
        for (final ResourceAmount output : pattern.pattern().layout().outputs()) {
            final PriorityQueue<PatternHolder> holders = this.patternsByOutput.get(output.resource());
            if (holders == null) {
                continue;
            }
            holders.removeIf(holder -> holder.pattern.equals(pattern));
            holders.add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void taskAdded(final StepCrafterProvider provider, final StepTask task) {
        this.providerByTaskId.put(task.getId(), provider);
        this.statusListeners.forEach(listener -> listener.taskAdded(task.getStatus()));
    }

    @Override
    public void taskRemoved(final StepTask task) {
        this.providerByTaskId.remove(task.getId());
        this.statusListeners.forEach(listener -> listener.taskRemoved(task.getId()));
    }

    @Override
    public void taskCompleted(final StepTask task) {
        this.taskRemoved(task);
    }

    @Override
    public void taskChanged(final StepTask task) {
        if (this.statusListeners.isEmpty()) {
            return;
        }
        final StepTaskStatus status = task.getStatus();
        this.statusListeners.forEach(listener -> listener.taskStatusChanged(status));
    }

    private record PatternHolder(PatternMinMax pattern, int priority) {
    }
}
