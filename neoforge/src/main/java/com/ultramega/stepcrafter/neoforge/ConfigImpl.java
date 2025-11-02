package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.Config;
import com.ultramega.stepcrafter.common.DefaultConfig;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslationKey;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";
    private static final String SPEED_MULTIPLIER = "speedMultiplier";

    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final SimpleEnergySpeedUsageEntry stepCrafter;
    private final SimpleEnergyUsageEntry stepRequester;
    private final SimpleEnergyUsageEntry slotUpgrade;

    public ConfigImpl() {
        this.stepCrafter = new SimpleEnergySpeedUsageEntryImpl("stepCrafter", DefaultConfig.STEP_CRAFTER_ENERGY_USAGE, DefaultConfig.STEP_CRAFTER_SPEED_MULTIPLIER);
        this.stepRequester = new SimpleEnergyUsageEntryImpl("stepRequester", DefaultConfig.STEP_REQUESTER_ENERGY_USAGE);
        this.slotUpgrade = new SimpleEnergyUsageEntryImpl("slotUpgrade", DefaultConfig.SLOT_UPGRADE_ENERGY_USAGE);
        this.spec = this.builder.build();
    }

    public ModConfigSpec getSpec() {
        return this.spec;
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

    private static String translationKey(final String value) {
        return createStepCrafterTranslationKey("text.autoconfig", "option." + value);
    }

    private class SimpleEnergySpeedUsageEntryImpl extends SimpleEnergyUsageEntryImpl implements SimpleEnergySpeedUsageEntry {
        private final ModConfigSpec.IntValue speedMultiplier;

        SimpleEnergySpeedUsageEntryImpl(final String path, final long energyDefaultValue, final int speedDefaultValue) {
            super(path, energyDefaultValue, false);
            this.speedMultiplier = ConfigImpl.this.builder
                .translation(translationKey(path + "." + SPEED_MULTIPLIER))
                .defineInRange(SPEED_MULTIPLIER, speedDefaultValue, 0, Integer.MAX_VALUE);
            ConfigImpl.this.builder.pop();
        }

        @Override
        public int getSpeedMultiplier() {
            return this.speedMultiplier.get();
        }
    }

    private class SimpleEnergyUsageEntryImpl implements SimpleEnergyUsageEntry {
        private final ModConfigSpec.LongValue energyUsage;

        SimpleEnergyUsageEntryImpl(final String path, final long defaultValue) {
            this(path, defaultValue, true);
        }

        SimpleEnergyUsageEntryImpl(final String path, final long defaultValue, final boolean pop) {
            ConfigImpl.this.builder.translation(translationKey(path)).push(path);
            this.energyUsage = ConfigImpl.this.builder
                .translation(translationKey(path + "." + ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, defaultValue, 0, Long.MAX_VALUE);
            if (pop) {
                ConfigImpl.this.builder.pop();
            }
        }

        @Override
        public long getEnergyUsage() {
            return this.energyUsage.get();
        }
    }
}
