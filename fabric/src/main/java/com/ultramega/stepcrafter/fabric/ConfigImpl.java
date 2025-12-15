package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.DefaultConfig;
import com.ultramega.stepcrafter.common.stepmanager.StepManagerSearchMode;

import com.refinedmods.refinedstorage.common.Config.SimpleEnergyUsageEntry;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static com.terraformersmc.modmenu.config.ModMenuConfigManager.save;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

@Config(name = MOD_ID)
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, com.ultramega.stepcrafter.common.Config {
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergySpeedUsageEntryImpl stepCrafter = new SimpleEnergySpeedUsageEntryImpl(DefaultConfig.STEP_CRAFTER_ENERGY_USAGE,
        DefaultConfig.STEP_CRAFTER_SPEED_MULTIPLIER);
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntry stepRequester = new SimpleEnergyUsageEntryImpl(DefaultConfig.STEP_REQUESTER_ENERGY_USAGE);
    @ConfigEntry.Gui.CollapsibleObject
    private StepManagerEntry stepCrafterManager = new StepManagerEntryImpl(DefaultConfig.STEP_CRAFTER_MANAGER_ENERGY_USAGE);
    @ConfigEntry.Gui.CollapsibleObject
    private StepManagerEntry stepRequesterManager = new StepManagerEntryImpl(DefaultConfig.STEP_REQUESTER_MANAGER_ENERGY_USAGE);
    @ConfigEntry.Gui.CollapsibleObject
    private SimpleEnergyUsageEntry slotUpgrade = new SimpleEnergyUsageEntryImpl(DefaultConfig.SLOT_UPGRADE_ENERGY_USAGE);

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

    private static class StepManagerEntryImpl implements StepManagerEntry {
        private long energyUsage;
        private StepManagerSearchMode searchMode = StepManagerSearchMode.ALL;
        private AutocrafterManagerViewType viewType = AutocrafterManagerViewType.VISIBLE;

        StepManagerEntryImpl(final long energyUsage) {
            this.energyUsage = energyUsage;
        }

        @Override
        public void setSearchMode(final StepManagerSearchMode searchMode) {
            this.searchMode = searchMode;
            save();
        }

        @Override
        public StepManagerSearchMode getSearchMode() {
            return this.searchMode;
        }

        @Override
        public void setViewType(final AutocrafterManagerViewType viewType) {
            this.viewType = viewType;
            save();
        }

        @Override
        public AutocrafterManagerViewType getViewType() {
            return this.viewType;
        }


        @Override
        public long getEnergyUsage() {
            return this.energyUsage;
        }
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
