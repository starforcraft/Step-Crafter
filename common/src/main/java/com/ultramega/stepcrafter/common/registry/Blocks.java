package com.ultramega.stepcrafter.common.registry;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlock;
import com.ultramega.stepcrafter.common.steprequester.StepRequesterBlock;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private Supplier<StepCrafterBlock> stepCrafter;
    @Nullable
    private Supplier<StepRequesterBlock> stepRequester;

    private Blocks() {
    }

    public void setStepCrafter(final Supplier<StepCrafterBlock> stepCrafter) {
        this.stepCrafter = stepCrafter;
    }

    public StepCrafterBlock getStepCrafter() {
        return requireNonNull(this.stepCrafter).get();
    }

    public void setStepRequester(final Supplier<StepRequesterBlock> stepRequester) {
        this.stepRequester = stepRequester;
    }

    public StepRequesterBlock getStepRequester() {
        return requireNonNull(this.stepRequester).get();
    }
}
