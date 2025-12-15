package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.UpgradeDestinations;
import com.ultramega.stepcrafter.common.registry.Items;
import com.ultramega.stepcrafter.common.registry.Menus;
import com.ultramega.stepcrafter.common.support.AbstractEditableNameContainerMenu;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;
import static com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlockEntity.UPGRADES;

public class StepCrafterContainerMenu extends AbstractEditableNameContainerMenu {
    private static final int PATTERN_SLOT_X = 8;
    private static final int PATTERN_SLOT_Y = 20;

    @Nullable
    private StepCrafterBlockEntity stepCrafter;

    private int amountSlotUpgrades = 0;

    public StepCrafterContainerMenu(final int syncId, final Inventory playerInventory, final StepCrafterData data) {
        super(Menus.INSTANCE.getStepCrafter(), syncId, playerInventory.player, null);
        this.registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.registerProperty(new ClientProperty<>(StepCrafterPropertyTypes.VISIBLE_TO_THE_STEP_CRAFTER_MANAGER, true));
        this.addSlots(
            StepCrafterBlockEntity.createPatternResourcesContainer(data, playerInventory.player::level, this::getAmountSlotUpgrades),
            new UpgradeContainer(UPGRADES, UpgradeDestinations.STEP_CRAFTER, (c, upgradeEnergyUsage) -> {
                this.amountSlotUpgrades = c.getAmount(Items.INSTANCE.getSlotUpgrade());
                this.amountSlotUpgradesChanged(this.amountSlotUpgrades);
            }, 9)
        );
        this.name = Component.empty();
    }

    public StepCrafterContainerMenu(final int syncId,
                                    final Inventory playerInventory,
                                    final StepCrafterBlockEntity stepCrafter) {
        super(Menus.INSTANCE.getStepCrafter(), syncId, playerInventory.player, stepCrafter);
        this.stepCrafter = stepCrafter;
        this.name = stepCrafter.getDisplayName();
        this.registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            stepCrafter::getRedstoneMode,
            stepCrafter::setRedstoneMode
        ));
        this.registerProperty(new ServerProperty<>(
            StepCrafterPropertyTypes.VISIBLE_TO_THE_STEP_CRAFTER_MANAGER,
            stepCrafter::isVisibleToTheStepCrafterManager,
            stepCrafter::setVisibleToTheStepCrafterManager
        ));
        this.addSlots(stepCrafter.getPatternResourceContainer(), stepCrafter.getUpgradeContainer());
    }

    @Override
    public boolean stillValid(final Player p) {
        if (this.stepCrafter == null) {
            return true;
        }
        return Container.stillValidBlockEntity(this.stepCrafter, p);
    }

    @Override
    protected void addSlots(final PatternResourceContainerImpl patternContainer, final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < patternContainer.getContainerSize(); ++i) {
            super.addSlot(this.createPatternSlot(patternContainer, i, this.player.level()));
        }
        super.addSlots(patternContainer, upgradeContainer);
        this.transferManager.addBiTransfer(this.player.getInventory(), patternContainer);
    }

    private Slot createPatternSlot(final PatternResourceContainerImpl patternContainer,
                                   final int index,
                                   final Level level) {
        final int x = PATTERN_SLOT_X + (index % 9) * 18;
        final int y = PATTERN_SLOT_Y + (index / 9) * 18;
        return new PatternResourceSlot(patternContainer, index, createStepCrafterTranslation("gui", "step_crafter.filter_help"), x, y, level, false);
    }

    public boolean containsPattern(final ItemStack stack) {
        for (final Slot slot : this.slots) {
            if (slot instanceof PatternResourceSlot patternSlot && patternSlot.getItem() == stack) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAmountSlotUpgrades() {
        return this.amountSlotUpgrades;
    }
}
