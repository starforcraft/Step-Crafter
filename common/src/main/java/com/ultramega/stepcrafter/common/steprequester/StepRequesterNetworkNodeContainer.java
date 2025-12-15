package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.stepmanager.StepBlock;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionStrategy;
import com.refinedmods.refinedstorage.common.support.network.InWorldNetworkNodeContainerImpl;

import net.minecraft.network.chat.Component;

public class StepRequesterNetworkNodeContainer extends InWorldNetworkNodeContainerImpl implements StepBlock {
    private final StepRequesterBlockEntity blockEntity;

    StepRequesterNetworkNodeContainer(final StepRequesterBlockEntity blockEntity,
                                      final NetworkNode node,
                                      final String name,
                                      final ConnectionStrategy connectionStrategy) {
        super(blockEntity, node, name, 0, connectionStrategy, null);
        this.blockEntity = blockEntity;
    }

    @Override
    public Component getBlockName() {
        return this.blockEntity.getName();
    }

    @Override
    public PatternResourceContainerImpl getPatternContainer() {
        return this.blockEntity.getFilterContainer();
    }

    @Override
    public int getSlotUpgradesCount() {
        return this.blockEntity.getSlotUpgradesCount();
    }

    @Override
    public boolean isVisibleToTheStepManager() {
        return this.blockEntity.isVisibleToTheStepRequesterManager();
    }

    @Override
    public boolean isStepCrafter() {
        return false;
    }
}
