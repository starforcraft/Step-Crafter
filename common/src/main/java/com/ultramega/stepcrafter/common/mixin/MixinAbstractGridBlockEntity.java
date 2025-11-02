package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.NetworkGetter;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.common.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractGridBlockEntity.class)
public abstract class MixinAbstractGridBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<GridNetworkNode> implements NetworkGetter {
    protected MixinAbstractGridBlockEntity(final BlockEntityType<?> type,
                                           final BlockPos pos,
                                           final BlockState state, final GridNetworkNode networkNode) {
        super(type, pos, state, networkNode);
    }

    @Nullable
    @Override
    public Network stepcrafter$getNetwork() {
        return this.mainNetworkNode.getNetwork();
    }
}
