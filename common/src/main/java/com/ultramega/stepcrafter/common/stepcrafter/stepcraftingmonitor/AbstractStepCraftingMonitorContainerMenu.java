package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.packet.c2s.StepCraftingMonitorCancelAllPacket;
import com.ultramega.stepcrafter.common.packet.c2s.StepCraftingMonitorCancelPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorActivePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskAddedPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskRemovedPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskStatusChangedPacket;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatusListener;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskState;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus.TaskInfo;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorWatcher;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractStepCraftingMonitorContainerMenu extends AbstractBaseContainerMenu
    implements StepTaskStatusListener, AutocraftingMonitorWatcher, StepCraftingTaskButton.StateProvider {
    private final Map<TaskId, StepTaskStatus> statusByTaskId;
    private final List<TaskInfo> tasks;
    private final List<TaskStatus.TaskInfo> tasksView;
    @Nullable
    private final StepCraftingMonitor stepCraftingMonitor;
    private final Player player;

    @Nullable
    private StepCraftingMonitorListener listener;
    @Nullable
    private TaskId currentTaskId;
    private boolean active;

    protected AbstractStepCraftingMonitorContainerMenu(final MenuType<?> menuType,
                                                       final int syncId,
                                                       final Inventory playerInventory,
                                                       final StepCraftingMonitorData data) {
        super(menuType, syncId);
        this.statusByTaskId = data.statuses().stream().collect(Collectors.toMap(
            s -> s.info().id(),
            s -> s
        ));
        this.tasks = data.statuses().stream().map(StepTaskStatus::info).collect(Collectors.toList());
        this.tasksView = Collections.unmodifiableList(this.tasks);
        this.currentTaskId = data.statuses().isEmpty() ? null : data.statuses().getFirst().info().id();
        this.stepCraftingMonitor = null;
        this.active = data.active();
        this.player = playerInventory.player;
    }

    AbstractStepCraftingMonitorContainerMenu(final MenuType<?> menuType,
                                             final int syncId,
                                             final Player player,
                                             final StepCraftingMonitor stepCraftingMonitor) {
        super(menuType, syncId);
        this.statusByTaskId = Collections.emptyMap();
        this.tasks = Collections.emptyList();
        this.tasksView = Collections.emptyList();
        this.currentTaskId = null;
        this.stepCraftingMonitor = stepCraftingMonitor;
        this.player = player;
        this.stepCraftingMonitor.addListener(this);
        this.stepCraftingMonitor.addWatcher(this);
    }

    @Override
    public void removed(final Player removedPlayer) {
        super.removed(removedPlayer);
        if (this.stepCraftingMonitor != null) {
            this.stepCraftingMonitor.removeListener(this);
            this.stepCraftingMonitor.removeWatcher(this);
        }
    }

    void setListener(@Nullable final StepCraftingMonitorListener listener) {
        this.listener = listener;
    }

    List<StepTaskStatus.Item> getCurrentItems() {
        final StepTaskStatus status = this.statusByTaskId.get(this.currentTaskId);
        if (status == null) {
            return Collections.emptyList();
        }
        return status.ingredients();
    }

    List<TaskStatus.TaskInfo> getTasksView() {
        return this.tasksView;
    }

    @Override
    public double getPercentageCompleted(final TaskId taskId) {
        final StepTaskStatus status = this.statusByTaskId.get(taskId);
        return status == null ? 0 : status.percentageCompleted();
    }

    @Override
    @Nullable
    public StepTaskState getState(final TaskId taskId) {
        final StepTaskStatus status = this.statusByTaskId.get(taskId);
        return status == null ? null : status.state();
    }

    void setCurrentTaskId(@Nullable final TaskId taskId) {
        this.currentTaskId = taskId;
        this.loadCurrentTask();
    }

    void loadCurrentTask() {
        if (this.listener != null) {
            this.listener.currentTaskChanged(this.currentTaskId == null ? null : this.statusByTaskId.get(this.currentTaskId));
        }
    }

    @Override
    public void taskStatusChanged(final StepTaskStatus status) {
        if (this.stepCraftingMonitor != null && this.player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer, new StepCraftingMonitorTaskStatusChangedPacket(status));
            return;
        }
        this.statusByTaskId.put(status.info().id(), status);
    }

    @Override
    public void taskRemoved(final TaskId id) {
        if (this.stepCraftingMonitor != null && this.player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer, new StepCraftingMonitorTaskRemovedPacket(id));
            return;
        }
        this.statusByTaskId.remove(id);
        this.tasks.removeIf(task -> task.id().equals(id));
        if (this.listener != null) {
            this.listener.taskRemoved(id);
        }
        if (id.equals(this.currentTaskId)) {
            this.currentTaskId = this.tasks.isEmpty() ? null : this.tasks.getFirst().id();
            this.loadCurrentTask();
        }
    }

    @Override
    public void taskAdded(final StepTaskStatus status) {
        if (this.stepCraftingMonitor != null && this.player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer, new StepCraftingMonitorTaskAddedPacket(status));
            return;
        }
        this.statusByTaskId.put(status.info().id(), status);
        this.tasks.add(status.info());
        if (this.listener != null) {
            this.listener.taskAdded(status);
        }
        if (this.currentTaskId == null) {
            this.currentTaskId = status.info().id();
            this.loadCurrentTask();
        }
    }

    public void cancelTask(final TaskId taskId) {
        if (this.stepCraftingMonitor != null) {
            this.stepCraftingMonitor.cancel(taskId);
        }
    }

    void cancelCurrentTask() {
        if (this.currentTaskId != null) {
            Platform.INSTANCE.sendPacketToServer(new StepCraftingMonitorCancelPacket(this.currentTaskId));
        }
    }

    public void cancelAllTasks() {
        if (this.stepCraftingMonitor != null) {
            this.stepCraftingMonitor.cancelAll();
        } else {
            Platform.INSTANCE.sendPacketToServer(StepCraftingMonitorCancelAllPacket.INSTANCE);
        }
    }

    @Override
    public void activeChanged(final boolean newActive) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            Platform.INSTANCE.sendPacketToClient(serverPlayer, new StepCraftingMonitorActivePacket(newActive));
        } else {
            this.active = newActive;
            this.loadCurrentTask();
        }
    }

    boolean isActive() {
        return this.active;
    }
}
