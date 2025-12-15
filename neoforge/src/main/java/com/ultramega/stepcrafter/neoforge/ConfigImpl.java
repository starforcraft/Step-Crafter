package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.Config;
import com.ultramega.stepcrafter.common.DefaultConfig;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerSearchMode;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslationKey;

public class ConfigImpl implements Config {
    private static final String ENERGY_USAGE = "energyUsage";
    private static final String SPEED_MULTIPLIER = "speedMultiplier";

    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final SimpleEnergySpeedUsageEntry stepCrafter;
    private final SimpleEnergyUsageEntry stepRequester;
    private final StepManagerEntry stepCrafterManager;
    private final StepManagerEntry stepRequesterManager;
    private final SimpleEnergyUsageEntry slotUpgrade;

    public ConfigImpl() {
        this.stepCrafter = new SimpleEnergySpeedUsageEntryImpl("stepCrafter", DefaultConfig.STEP_CRAFTER_ENERGY_USAGE, DefaultConfig.STEP_CRAFTER_SPEED_MULTIPLIER);
        this.stepRequester = new SimpleEnergyUsageEntryImpl("stepRequester", DefaultConfig.STEP_REQUESTER_ENERGY_USAGE);
        this.stepCrafterManager = new StepManagerEntryImpl("stepCrafterManager", DefaultConfig.STEP_CRAFTER_MANAGER_ENERGY_USAGE);
        this.stepRequesterManager = new StepManagerEntryImpl("stepRequesterManager", DefaultConfig.STEP_REQUESTER_MANAGER_ENERGY_USAGE);
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
    public StepManagerEntry getStepCrafterManager() {
        return this.stepCrafterManager;
    }

    @Override
    public StepManagerEntry getStepRequesterManager() {
        return this.stepRequesterManager;
    }

    @Override
    public SimpleEnergyUsageEntry getSlotUpgrade() {
        return this.slotUpgrade;
    }

    private static String translationKey(final String value) {
        return createStepCrafterTranslationKey("text.autoconfig", "option." + value);
    }

    private class StepManagerEntryImpl extends SimpleEnergyUsageEntryImpl implements StepManagerEntry {
        private final ModConfigSpec.EnumValue<StepManagerSearchMode> searchMode;
        private final ModConfigSpec.EnumValue<AutocrafterManagerViewType> viewType;

        StepManagerEntryImpl(final String path, final long energyDefaultValue) {
            super(path, energyDefaultValue, false);
            this.searchMode = ConfigImpl.this.builder
                .translation(translationKey(path + ".searchMode"))
                .defineEnum("searchMode", StepManagerSearchMode.ALL);
            this.viewType = ConfigImpl.this.builder
                .translation(translationKey(path + ".viewType"))
                .defineEnum("viewType", AutocrafterManagerViewType.VISIBLE);
            ConfigImpl.this.builder.pop();
        }

        @Override
        public void setSearchMode(final StepManagerSearchMode searchMode) {
            if (searchMode != this.searchMode.get()) {
                this.searchMode.set(searchMode);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public StepManagerSearchMode getSearchMode() {
            return this.searchMode.get();
        }

        @Override
        public void setViewType(final AutocrafterManagerViewType viewType) {
            if (viewType != this.viewType.get()) {
                this.viewType.set(viewType);
                ConfigImpl.this.spec.save();
            }
        }

        @Override
        public AutocrafterManagerViewType getViewType() {
            return this.viewType.get();
        }
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
