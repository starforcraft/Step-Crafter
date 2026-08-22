package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.ClientConfig;
import com.ultramega.stepcrafter.common.DefaultConfig;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslationKey;

public final class ClientConfigImpl implements ClientConfig {
    private static final String RESOURCE_CONFIGURATION_DEFAULTS = "resourceConfigurationDefaults";
    private static final String DEFAULT_MIN_AMOUNT = "defaultMinAmount";
    private static final String DEFAULT_MAX_AMOUNT = "defaultMaxAmount";
    private static final String DEFAULT_BATCH_SIZE = "defaultBatchSize";

    private final ModConfigSpec spec;
    private final ModConfigSpec.DoubleValue defaultMinAmount;
    private final ModConfigSpec.DoubleValue defaultMaxAmount;
    private final ModConfigSpec.DoubleValue defaultBatchSize;

    public ClientConfigImpl() {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation(translationKey(RESOURCE_CONFIGURATION_DEFAULTS)).push(RESOURCE_CONFIGURATION_DEFAULTS);

        this.defaultMinAmount = builder
            .translation(translationKey(RESOURCE_CONFIGURATION_DEFAULTS + "." + DEFAULT_MIN_AMOUNT))
            .defineInRange(DEFAULT_MIN_AMOUNT, DefaultConfig.DEFAULT_MIN_AMOUNT, 0D, Double.MAX_VALUE);
        this.defaultMaxAmount = builder
            .translation(translationKey(RESOURCE_CONFIGURATION_DEFAULTS + "." + DEFAULT_MAX_AMOUNT))
            .defineInRange(DEFAULT_MAX_AMOUNT, DefaultConfig.DEFAULT_MAX_AMOUNT, 0D, Double.MAX_VALUE);
        this.defaultBatchSize = builder
            .translation(translationKey(RESOURCE_CONFIGURATION_DEFAULTS + "." + DEFAULT_BATCH_SIZE))
            .defineInRange(DEFAULT_BATCH_SIZE, DefaultConfig.DEFAULT_BATCH_SIZE, 0D, Double.MAX_VALUE);

        builder.pop();
        this.spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public double getDefaultMinAmount() {
        return this.defaultMinAmount.get();
    }

    @Override
    public double getDefaultMaxAmount() {
        return this.defaultMaxAmount.get();
    }

    @Override
    public double getDefaultBatchSize() {
        return this.defaultBatchSize.get();
    }

    private static String translationKey(final String value) {
        return createStepCrafterTranslationKey("text.autoconfig", "option." + value);
    }
}
