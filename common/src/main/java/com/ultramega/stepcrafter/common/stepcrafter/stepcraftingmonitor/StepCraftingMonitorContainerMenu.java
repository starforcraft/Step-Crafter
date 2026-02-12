package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.registry.Menus;

import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class StepCraftingMonitorContainerMenu extends AbstractStepCraftingMonitorContainerMenu {
    private final Predicate<Player> stillValid;

    public StepCraftingMonitorContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final StepCraftingMonitorData data) {
        super(Menus.INSTANCE.getStepCraftingMonitor(), syncId, playerInventory, data);
        this.registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.stillValid = p -> true;
    }

    StepCraftingMonitorContainerMenu(final int syncId,
                                     final Player player,
                                     final StepCraftingMonitorBlockEntity stepCraftingMonitor) {
        super(Menus.INSTANCE.getStepCraftingMonitor(), syncId, player, stepCraftingMonitor);
        this.registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            stepCraftingMonitor::getRedstoneMode,
            stepCraftingMonitor::setRedstoneMode
        ));
        this.stillValid = p -> Container.stillValidBlockEntity(stepCraftingMonitor, p);
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.stillValid.test(player);
    }
}
