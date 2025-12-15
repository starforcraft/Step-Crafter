package com.ultramega.stepcrafter.common.steprequestermanager;

import com.ultramega.stepcrafter.common.ContentNames;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerBlockEntity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class StepRequesterManagerBlockEntity extends AbstractStepManagerBlockEntity {
    public StepRequesterManagerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getStepRequesterManager(), pos, state, Platform.INSTANCE.getConfig().getStepRequesterManager().getEnergyUsage());
    }

    @Override
    public Component getName() {
        return ContentNames.STEP_REQUESTER_MANAGER;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StepRequesterManagerContainerMenu(syncId, inventory, this, this.getGroups());
    }

    @Override
    protected boolean isStepCrafterManager() {
        return false;
    }
}
