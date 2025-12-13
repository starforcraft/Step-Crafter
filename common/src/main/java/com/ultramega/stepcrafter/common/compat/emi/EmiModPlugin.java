package com.ultramega.stepcrafter.common.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class EmiModPlugin implements EmiPlugin {
    @Override
    public void register(final EmiRegistry registry) {
        registry.addGenericDragDropHandler(new PatternResourceEmiDragDropHandler());
    }
}
