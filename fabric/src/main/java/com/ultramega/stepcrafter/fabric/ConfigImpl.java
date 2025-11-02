package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.DefaultConfig;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

@Config(name = MOD_ID)
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, com.ultramega.stepcrafter.common.Config {
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergySpeedUsageEntryImpl stepCrafter = new SimpleEnergySpeedUsageEntryImpl(DefaultConfig.STEP_CRAFTER_ENERGY_USAGE,
        DefaultConfig.STEP_CRAFTER_SPEED_MULTIPLIER);
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl stepRequester = new SimpleEnergyUsageEntryImpl(DefaultConfig.STEP_REQUESTER_ENERGY_USAGE);
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntryImpl slotUpgrade = new SimpleEnergyUsageEntryImpl(DefaultConfig.SLOT_UPGRADE_ENERGY_USAGE);

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public SimpleEnergySpeedUsageEntry getStepCrafter() {
        return this.stepCrafter;
    }

    @Override
    public SimpleEnergyUsageEntry getStepRequester() {
        return this.stepRequester;
    }

    @Override
    public SimpleEnergyUsageEntry getSlotUpgrade() {
        return this.slotUpgrade;
    }

    private static class SimpleEnergySpeedUsageEntryImpl implements SimpleEnergySpeedUsageEntry {
        private long energyUsage;
        private int speedMultiplier;

        SimpleEnergySpeedUsageEntryImpl(final long energyUsage, final int speedMultiplier) {
            this.energyUsage = energyUsage;
            this.speedMultiplier = speedMultiplier;
        }

        @Override
        public long getEnergyUsage() {
            return this.energyUsage;
        }

        @Override
        public int getSpeedMultiplier() {
            return this.speedMultiplier;
        }
    }

    private static class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private long energyUsage;

        SimpleEnergyUsageEntryImpl(final long energyUsage) {
            this.energyUsage = energyUsage;
        }

        @Override
        public long getEnergyUsage() {
            return this.energyUsage;
        }
    }
}
