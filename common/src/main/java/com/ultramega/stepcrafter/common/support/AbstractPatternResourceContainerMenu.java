package com.ultramega.stepcrafter.common.support;

import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractPatternResourceContainerMenu extends AbstractBaseContainerMenu {
    protected final Player player;
    private final List<PatternResourceSlot> patternResourceSlots = new ArrayList<>();

    public AbstractPatternResourceContainerMenu(@Nullable final MenuType<?> type, final int syncId, final Player player) {
        super(type, syncId);
        this.player = player;
    }

    private Optional<PatternResourceSlot> getPatternResourceSlot(final int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return Optional.empty();
        }
        if (this.slots.get(slotIndex) instanceof PatternResourceSlot resourceSlot) {
            return Optional.of(resourceSlot);
        }
        return Optional.empty();
    }

    public List<PatternResourceSlot> getPatternResourceSlots() {
        return this.patternResourceSlots;
    }

    public void handlePatternResourceSlotUpdate(final int slotIndex, @Nullable final ResourceMinMaxAmount resourceAmount) {
        this.getPatternResourceSlot(slotIndex).ifPresent(slot -> slot.change(resourceAmount));
    }

    public void handlePatternResourceFilterSlotUpdate(final int slotIndex, final PlatformResourceKey resource) {
        this.getPatternResourceSlot(slotIndex).ifPresent(slot -> slot.setFilter(resource));
    }

    public void handlePatternResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        this.getPatternResourceSlot(slotIndex).ifPresent(slot -> slot.change(this.getCarried(), tryAlternatives));
    }

    public void handlePatternResourceSlotAmountChange(final int slotIndex, final long minAmount, final long maxAmount, final long batchSize) {
        this.getPatternResourceSlot(slotIndex).ifPresent(slot -> slot.changeAmount(minAmount, maxAmount, batchSize));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (final PatternResourceSlot resourceSlot : this.patternResourceSlots) {
            resourceSlot.broadcastChanges(this.player);
        }
    }

    protected void addSlots(final PatternResourceContainerImpl filterContainer, final UpgradeContainer upgradeContainer) {
        for (int i = 0; i < upgradeContainer.getContainerSize(); ++i) {
            super.addSlot(new UpgradeSlot(upgradeContainer, i, 187 + (i / 4) * 18, 6 + (i % 4) * 18));
        }
        super.addPlayerInventory(this.player.getInventory(), 8, 55);
        this.transferManager.addBiTransfer(this.player.getInventory(), upgradeContainer);
    }

    public void amountSlotUpgradesChanged(final int amountSlotUpgrades) {
        for (final Slot slot : this.slots) {
            if (slot instanceof PlayerInventorySlotMarker) {
                final int y = 55 + amountSlotUpgrades * 18;
                if (slot.getContainerSlot() < 9) {
                    Platform.INSTANCE.setSlotY(slot, y + 4 + (3 * 18));
                } else {
                    Platform.INSTANCE.setSlotY(slot, y + (slot.getContainerSlot() / 9 - 1) * 18);
                }
            }
        }
    }

    @Override
    protected Slot addSlot(final Slot slot) {
        if (slot instanceof PatternResourceSlot resourceSlot) {
            this.patternResourceSlots.add(resourceSlot);
        }
        return super.addSlot(slot);
    }

    @Override
    protected void resetSlots() {
        super.resetSlots();
        this.patternResourceSlots.clear();
    }

    public void addToResourceSlotIfNotExisting(final ItemStack stack) {
        for (final PatternResourceSlot resourceSlot : this.patternResourceSlots) {
            if (resourceSlot.contains(stack)) {
                return;
            }
        }
        for (final PatternResourceSlot resourceSlot : this.patternResourceSlots) {
            if (resourceSlot.changeIfEmpty(stack)) {
                return;
            }
        }
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof PatternResourceSlot) {
            return true;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    public abstract int getAmountSlotUpgrades();
}
