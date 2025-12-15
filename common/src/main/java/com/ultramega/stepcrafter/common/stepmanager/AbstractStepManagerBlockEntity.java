package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractStepManagerBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<StepManagerData> {
    private final Set<StepManagerWatcher> watchers = new HashSet<>();

    public AbstractStepManagerBlockEntity(final BlockEntityType<? extends AbstractStepManagerBlockEntity> blockEntity,
                                          final BlockPos pos,
                                          final BlockState state,
                                          final long energyUsage) {
        super(blockEntity, pos, state, new SimpleNetworkNode(energyUsage));
    }

    void addWatcher(final StepManagerWatcher watcher) {
        this.watchers.add(watcher);
    }

    void removeWatcher(final StepManagerWatcher watcher) {
        this.watchers.remove(watcher);
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        this.watchers.forEach(watcher -> watcher.activeChanged(newActive));
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    public StepManagerData getMenuData() {
        return new StepManagerData(
            this.getGroups().stream().map(StepManagerData.Group::of).toList(),
            this.mainNetworkNode.isActive()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, StepManagerData> getMenuCodec() {
        return StepManagerData.STREAM_CODEC;
    }

    protected abstract boolean isStepCrafterManager();

    private Stream<StepBlock> getStepBlocks() {
        final Network mainNetwork = this.mainNetworkNode.getNetwork();
        if (mainNetwork == null) {
            return Stream.empty();
        }
        return mainNetwork.getComponent(GraphNetworkComponent.class)
            .getContainers(StepBlock.class)
            .stream()
            .filter(stepBlock -> this.isStepCrafterManager() == stepBlock.isStepCrafter())
            .sorted(Comparator.comparing(StepBlock::getLocalPosition));
    }

    protected List<Group> getGroups() {
        return this.getStepBlocks()
            .collect(Collectors.groupingBy(a -> a.getBlockName().getString()))
            .entrySet()
            .stream()
            .map(entry -> new Group(
                entry.getKey(),
                entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(StepBlock::getLocalPosition))
                    .map(SubGroup::of)
                    .toList()
            ))
            .sorted(Comparator.comparing(group -> group.name))
            .toList();
    }

    public record Group(String name, List<SubGroup> subGroups) {
    }

    record SubGroup(PatternResourceContainerImpl container, int slotUpgradesCount, boolean visibleToTheStepManager, boolean full) {
        private static SubGroup of(final StepBlock stepBlock) {
            final PatternResourceContainerImpl container = stepBlock.getPatternContainer();
            final boolean full = isFull(container, stepBlock.getSlotUpgradesCount());
            return new SubGroup(container, stepBlock.getSlotUpgradesCount(), stepBlock.isVisibleToTheStepManager(), full);
        }

        private static boolean isFull(final Container container, final int slotUpgradesCount) {
            for (int i = 0; i < container.getContainerSize(); ++i) {
                if (container.getItem(i).isEmpty() && i < 9 * (slotUpgradesCount + 1)) {
                    return false;
                }
            }
            return true;
        }
    }
}
