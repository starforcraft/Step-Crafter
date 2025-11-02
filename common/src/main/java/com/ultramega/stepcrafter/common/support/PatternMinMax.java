package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.CoreValidations;

public record PatternMinMax(Pattern pattern, long minAmount, long maxAmount, long batchSize) {
    public PatternMinMax {
        CoreValidations.validateNotNegative(minAmount, "Min amount must be non-negative");
        CoreValidations.validateNotNegative(maxAmount, "Max amount must be non-negative");
        CoreValidations.validateNotNegative(batchSize, "Batch Size must be non-negative");
    }
}
