package com.ultramega.stepcrafter.common.network.stepcrafter;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterProvider;
import com.ultramega.stepcrafter.common.stepcrafter.StepCraftingParentContainer;
import com.ultramega.stepcrafter.common.support.PatternMinMax;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class StepCraftingNetworkComponentImpl implements StepCraftingNetworkComponent, StepCraftingParentContainer {
    private final Set<PatternMinMax> patterns = new HashSet<>();
    private final Map<ResourceKey, PriorityQueue<PatternHolder>> patternsByOutput = new HashMap<>();
    private final Set<ResourceMinMaxAmount> outputs = new HashSet<>();

    public StepCraftingNetworkComponentImpl() {
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StepCrafterProvider provider) {
            provider.onAddedIntoContainer(this);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        if (container.getNode() instanceof StepCrafterProvider provider) {
            provider.onRemovedFromContainer(this);
        }
    }

    @Override
    public Set<ResourceMinMaxAmount> getOutputs() {
        return this.outputs;
    }

    @Override
    public void add(final StepCrafterProvider provider, final PatternMinMax pattern) {
        final int priority = 0; //TODO

        this.patterns.add(pattern);
        pattern.pattern().layout().outputs().forEach(output ->
            this.outputs.add(new ResourceMinMaxAmount(output.resource(), pattern.minAmount(), pattern.maxAmount(), 1L, false)));
        for (final ResourceAmount output : pattern.pattern().layout().outputs()) {
            this.patternsByOutput.computeIfAbsent(output.resource(), k -> new PriorityQueue<>(
                Comparator.comparingInt(PatternHolder::priority).reversed()
            )).add(new PatternHolder(pattern, priority));
        }
    }

    @Override
    public void remove(final StepCrafterProvider provider, final PatternMinMax pattern) {
        this.patterns.remove(pattern);
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

    private record PatternHolder(PatternMinMax pattern, int priority) {
    }
}
