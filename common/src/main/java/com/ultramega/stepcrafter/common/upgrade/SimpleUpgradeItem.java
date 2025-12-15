package com.ultramega.stepcrafter.common.upgrade;

import com.ultramega.stepcrafter.common.Platform;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import java.util.function.LongSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class SimpleUpgradeItem extends AbstractUpgradeItem {
    private final LongSupplier energyUsageResolver;

    protected SimpleUpgradeItem(final UpgradeRegistry registry,
                                final LongSupplier energyUsageResolver,
                                final Component helpText) {
        super(new Item.Properties(), registry, helpText);
        this.energyUsageResolver = energyUsageResolver;
    }

    @Override
    public long getEnergyUsage() {
        return this.energyUsageResolver.getAsLong();
    }

    public static SimpleUpgradeItem slotUpgrade() {
        return new SimpleUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.INSTANCE.getConfig().getSlotUpgrade()::getEnergyUsage,
            createStepCrafterTranslation("item", "slot_upgrade.help")
        );
    }
}
