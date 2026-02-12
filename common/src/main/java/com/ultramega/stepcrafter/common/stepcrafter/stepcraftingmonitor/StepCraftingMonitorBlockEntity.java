package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.network.stepcrafter.StepCraftingNetworkComponent;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatusListener;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorWatcher;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class StepCraftingMonitorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<StepCraftingMonitorData>, StepCraftingMonitor {
    private final Set<AutocraftingMonitorWatcher> watchers = new HashSet<>();

    //TODO: add wireless variant
    public StepCraftingMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getStepCraftingMonitor(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getStepCraftingMonitor().getEnergyUsage()
        ));
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        this.watchers.forEach(watcher -> watcher.activeChanged(newActive));
    }

    @Override
    public Component getName() {
        return this.overrideName(ContentNames.STEP_CRAFTING_MONITOR);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    public StepCraftingMonitorData getMenuData() {
        return new StepCraftingMonitorData(this.getStatuses(), this.isStepCraftingMonitorActive());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, StepCraftingMonitorData> getMenuCodec() {
        return StepCraftingMonitorData.STREAM_CODEC;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StepCraftingMonitorContainerMenu(syncId, player, this);
    }

    private Optional<StepCraftingNetworkComponent> getStepCrafting() {
        final Network network = this.mainNetworkNode.getNetwork();
        if (network == null) {
            return Optional.empty();
        }
        return Optional.of(network.getComponent(StepCraftingNetworkComponent.class));
    }

    @Override
    public List<StepTaskStatus> getStatuses() {
        return this.getStepCrafting().map(StepCraftingNetworkComponent::getStatuses).orElse(Collections.emptyList());
    }

    @Override
    public void addListener(final StepTaskStatusListener listener) {
        this.getStepCrafting().ifPresent(stepCrafting -> stepCrafting.addListener(listener));
    }

    @Override
    public void removeListener(final StepTaskStatusListener listener) {
        this.getStepCrafting().ifPresent(stepCrafting -> stepCrafting.removeListener(listener));
    }

    @Override
    public void cancel(final TaskId taskId) {
        this.getStepCrafting().ifPresent(stepCrafting -> stepCrafting.cancel(taskId));
    }

    @Override
    public void cancelAll() {
        this.getStepCrafting().ifPresent(StepCraftingNetworkComponent::cancelAll);
    }

    @Override
    public void addWatcher(final AutocraftingMonitorWatcher watcher) {
        this.watchers.add(watcher);
    }

    @Override
    public void removeWatcher(final AutocraftingMonitorWatcher watcher) {
        this.watchers.remove(watcher);
    }

    @Override
    public boolean isStepCraftingMonitorActive() {
        return this.mainNetworkNode.isActive();
    }
}
