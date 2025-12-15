package com.ultramega.stepcrafter.common.stepcraftermanager;

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

public class StepCrafterManagerBlockEntity extends AbstractStepManagerBlockEntity {
    public StepCrafterManagerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getStepCrafterManager(), pos, state, Platform.INSTANCE.getConfig().getStepCrafterManager().getEnergyUsage());
    }

    @Override
    public Component getName() {
        return ContentNames.STEP_CRAFTER_MANAGER;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StepCrafterManagerContainerMenu(syncId, inventory, this, this.getGroups());
    }

    @Override
    protected boolean isStepCrafterManager() {
        return true;
    }
}
