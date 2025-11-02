package com.ultramega.stepcrafter.common.steprequester;

import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.FilterTransfer;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;
import static com.ultramega.stepcrafter.common.steprequester.StepRequesterBlockEntity.UPGRADES;

public class StepRequesterContainerMenu extends AbstractPatternResourceContainerMenu {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    @Nullable
    private StepRequesterBlockEntity stepRequester;

    private int amountSlotUpgrades = 0;

    public StepRequesterContainerMenu(final int syncId, final Inventory playerInventory, final StepRequesterData data) {
        super(Menus.INSTANCE.getStepRequester(), syncId, playerInventory.player);
        this.registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.addSlots(
            StepRequesterBlockEntity.createFilterContainer(data, playerInventory.player::level, this::getAmountSlotUpgrades),
            new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_REQUESTER, (c, upgradeEnergyUsage) -> {
                this.amountSlotUpgrades = c.getAmount(Items.INSTANCE.getSlotUpgrade());
                this.amountSlotUpgradesChanged(this.amountSlotUpgrades);
            }, 9)
        );
    }

    public StepRequesterContainerMenu(final int syncId,
                                      final Inventory playerInventory,
                                      final StepRequesterBlockEntity stepRequester) {
        super(Menus.INSTANCE.getStepRequester(), syncId, playerInventory.player);
        this.stepRequester = stepRequester;
        this.registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            stepRequester::getRedstoneMode,
            stepRequester::setRedstoneMode
        ));
        this.addSlots(stepRequester.getFilterContainer(), stepRequester.getUpgradeContainer());
    }

    @Override
    public boolean stillValid(final Player p) {
        if (this.stepRequester == null) {
            return true;
        }
        return Container.stillValidBlockEntity(this.stepRequester, p);
    }

    @Override
    protected void addSlots(final PatternResourceContainerImpl filterContainer, final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < filterContainer.size(); ++i) {
            super.addSlot(this.createFilterSlot(filterContainer, i, this.player.level()));
        }
        super.addSlots(filterContainer, upgradeContainer);
        if (this.transferManager instanceof FilterTransfer filterTransfer) {
            filterTransfer.stepcrafter$addFilterTransfer(this.player.getInventory());
        }
    }

    private Slot createFilterSlot(final PatternResourceContainerImpl filterContainer,
                                  final int index,
                                  final Level level) {
        final int x = FILTER_SLOT_X + (index % 9) * 18;
        final int y = FILTER_SLOT_Y + (index / 9) * 18;
        return new PatternResourceSlot(filterContainer, index, createStepCrafterTranslation("gui", "step_requester.filter_help"), x, y, level, true);
    }

    @Override
    public int getAmountSlotUpgrades() {
        return this.amountSlotUpgrades;
    }
}
