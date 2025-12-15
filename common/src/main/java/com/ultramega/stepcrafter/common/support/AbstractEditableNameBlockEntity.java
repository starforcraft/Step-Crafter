package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractEditableNameBlockEntity<T extends SimpleNetworkNode> extends AbstractBaseNetworkNodeContainerBlockEntity<T> {
    protected AbstractEditableNameBlockEntity(final BlockEntityType<?> type,
                                              final BlockPos pos,
                                              final BlockState state,
                                              final T networkNode) {
        super(type, pos, state, networkNode);
    }

    void setCustomName(final String name) {
        this.setCustomName(name.trim().isBlank() ? null : Component.literal(name));
        this.setChanged();
    }
}
