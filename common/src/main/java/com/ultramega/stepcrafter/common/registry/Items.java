package com.ultramega.stepcrafter.common.registry;

import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<AbstractUpgradeItem> slotUpgrade;

    private final List<Supplier<BaseBlockItem>> allStepCrafterManagers = new ArrayList<>();
    private final List<Supplier<BaseBlockItem>> allStepRequesterManagers = new ArrayList<>();

    private Items() {
    }

    public AbstractUpgradeItem getSlotUpgrade() {
        return requireNonNull(this.slotUpgrade).get();
    }

    public void setSlotUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.slotUpgrade = supplier;
    }

    public void addStepCrafterManager(final Supplier<BaseBlockItem> supplier) {
        this.allStepCrafterManagers.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getStepCrafterManagers() {
        return Collections.unmodifiableList(this.allStepCrafterManagers);
    }

    public void addStepRequesterManager(final Supplier<BaseBlockItem> supplier) {
        this.allStepRequesterManagers.add(supplier);
    }

    public List<Supplier<BaseBlockItem>> getStepRequesterManagers() {
        return Collections.unmodifiableList(this.allStepRequesterManagers);
    }
}
