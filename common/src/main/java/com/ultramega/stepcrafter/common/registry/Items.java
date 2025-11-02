package com.ultramega.stepcrafter.common.registry;

import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<AbstractUpgradeItem> slotUpgrade;

    private Items() {
    }

    public AbstractUpgradeItem getSlotUpgrade() {
        return requireNonNull(this.slotUpgrade).get();
    }

    public void setSlotUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.slotUpgrade = supplier;
    }
}
