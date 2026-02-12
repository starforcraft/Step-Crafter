package com.ultramega.stepcrafter.common.stepcrafter.task;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterProvider;
import com.ultramega.stepcrafter.common.stepcrafter.StepCraftingParentContainer;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepTaskContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StepTaskContainer.class);

    private final StepCrafterProvider stepCrafterProvider;
    private final List<StepTask> tasks = new CopyOnWriteArrayList<>();
    private final List<StepTask> tasksView = Collections.unmodifiableList(this.tasks);
    private final Set<StepCraftingParentContainer> parents = new HashSet<>();

    public StepTaskContainer(final StepCrafterProvider stepCrafterProvider) {
        this.stepCrafterProvider = stepCrafterProvider;
    }

    public List<StepTask> getAll() {
        return this.tasksView;
    }

    public List<StepTaskStatus> getStatuses() {
        return this.tasks.stream().map(StepTask::getStatus).toList();
    }

    public void onRemovedFromContainer(final StepCraftingParentContainer parent) {
        this.tasks.forEach(parent::taskRemoved);
        this.parents.remove(parent);
    }

    public void onAddedIntoContainer(final StepCraftingParentContainer parent) {
        this.tasks.forEach(task -> parent.taskAdded(this.stepCrafterProvider, task));
        this.parents.add(parent);
    }

    public void add(final StepTask task, @Nullable final Network network) {
        this.tasks.add(task);
    }

    public void cancel(final TaskId id) {
        for (final StepTask task : this.tasks) {
            if (task.getId().equals(id)) {
                task.cancel();
                return;
            }
        }
        throw new IllegalArgumentException("StepTask %s not found".formatted(id));
    }

    public void craft(final Network network) {
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        this.tasks.removeIf(task -> this.craft(task, storage));
    }

    private boolean craft(final StepTask task,
                          final StorageNetworkComponent storage) {
        boolean changed;
        boolean completed;
        try {
            changed = task.craft(storage);
            completed = task.getState() == StepTaskState.COMPLETED;
        } catch (final Exception e) {
            LOGGER.error("Exception while crafting step task {} {}, removing task", task.getResource(), task.getAmount(), e);
            changed = false;
            completed = true;
        }
        if (completed) {
            this.parents.forEach(parent -> parent.taskCompleted(task));
        } else if (changed) {
            this.parents.forEach(parent -> parent.taskChanged(task));
        }
        return completed;
    }
}
