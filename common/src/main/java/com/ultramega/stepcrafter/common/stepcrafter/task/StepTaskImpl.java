package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus.ItemType;
import com.ultramega.stepcrafter.common.support.ResourceStatus;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus.TaskInfo;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.canInsertIntoNetwork;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.extractResolvedInputs;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.insertIntoNetwork;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.mergeIngredients;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCraftingAlgorithm.prepareCraft;

public class StepTaskImpl implements StepTask {
    private final TaskId id;
    private final ResourceKey resource;
    private final long amount;
    private final Pattern pattern;
    private final Actor actor;
    private final boolean notify;
    private final long startTime;
    private StepTaskState state = StepTaskState.READY;
    private long craftedAmount;
    private boolean cancelled;

    private final Map<ResourceKey, Long> ingredientsUsed = new HashMap<>();
    private final Map<ResourceKey, StepTaskStatus.ItemType> ingredientsStatus = new HashMap<>();

    public StepTaskImpl(final StepTaskSnapshot snapshot) {
        this.id = snapshot.id();
        this.resource = snapshot.resource();
        this.amount = snapshot.amount();
        this.pattern = snapshot.pattern();
        this.actor = snapshot.actor();
        this.notify = snapshot.notifyActor();
        this.startTime = snapshot.startTime();
        this.state = snapshot.state();
        this.craftedAmount = snapshot.craftedAmount();
        this.cancelled = snapshot.cancelled();
    }

    public StepTaskImpl(final ResourceKey resource, final long amount, final Pattern pattern, final Actor actor, final boolean notify) {
        this.id = TaskId.create();
        this.resource = resource;
        this.amount = amount;
        this.pattern = pattern;
        this.actor = actor;
        this.notify = notify;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Actor getActor() {
        return this.actor;
    }

    @Override
    public boolean shouldNotify() {
        return this.notify && !this.cancelled;
    }

    @Override
    public ResourceKey getResource() {
        return this.resource;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public TaskId getId() {
        return this.id;
    }

    @Override
    public StepTaskState getState() {
        return this.state;
    }

    @Override
    public boolean craft(final RootStorage rootStorage) {
        if (this.state == StepTaskState.COMPLETED) {
            return false;
        }

        final ResourceStatus status = this.craftOneItemNew(rootStorage);
        this.updateState(status);
        return true;
    }

    private void updateState(final ResourceStatus status) {
        if (status == ResourceStatus.FINISHED) {
            this.state = StepTaskState.RUNNING;
        } else if (status == ResourceStatus.NOT_ENOUGH_INGREDIENTS) {
            this.state = StepTaskState.NOT_ENOUGH_INGREDIENTS;
        } else if (status == ResourceStatus.NETWORK_FULL) {
            this.state = StepTaskState.NETWORK_FULL;
        }

        if (this.craftedAmount >= this.amount) {
            this.state = StepTaskState.COMPLETED;
        }
    }

    private ResourceStatus craftOneItemNew(final RootStorage storageComponent) {
        final StepCraftingAlgorithm.CraftPreparation preparation = prepareCraft(storageComponent, this.pattern, this.ingredientsStatus, this.actor);
        if (preparation == null) {
            return ResourceStatus.NOT_ENOUGH_INGREDIENTS;
        }

        if (!canInsertIntoNetwork(storageComponent, preparation.totalResources(), this.actor)) {
            return ResourceStatus.NETWORK_FULL;
        }

        extractResolvedInputs(storageComponent, preparation.resolvedInputs(), this.ingredientsUsed, this.actor);
        this.craftedAmount = insertIntoNetwork(storageComponent, preparation.totalResources(), this.resource, this.craftedAmount, this.actor);

        return ResourceStatus.FINISHED;
    }

    private List<StepTaskStatus.Item> getIngredientsFromPattern() {
        final List<StepTaskStatus.Item> result = new ArrayList<>();

        final List<Ingredient> uniqueIngredients = mergeIngredients(this.pattern.layout().ingredients());
        for (final Ingredient ingredient : uniqueIngredients) {
            for (final ResourceKey input : ingredient.inputs()) {
                result.add(new StepTaskStatus.Item(input, this.ingredientsStatus.getOrDefault(input, ItemType.NORMAL),
                    ingredient.amount() * this.amount, this.ingredientsUsed.getOrDefault(input, 0L)));
            }
        }

        return result;
    }

    @Override
    public void cancel() {
        this.state = StepTaskState.COMPLETED;
        this.cancelled = true;
    }

    @Override
    public StepTaskStatus getStatus() {
        return new StepTaskStatus(new TaskInfo(this.id, this.resource, this.amount, this.startTime),
            this.state, (double) this.craftedAmount / this.amount, this.getIngredientsFromPattern());
    }

    public StepTaskSnapshot createSnapshot() {
        return new StepTaskSnapshot(
            this.id,
            this.resource,
            this.amount,
            this.pattern,
            this.actor,
            this.notify,
            this.startTime,
            this.state,
            this.craftedAmount,
            this.cancelled
        );
    }
}
