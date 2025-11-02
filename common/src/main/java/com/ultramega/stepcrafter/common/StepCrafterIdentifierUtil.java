package com.ultramega.stepcrafter.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class StepCrafterIdentifierUtil {
    public static final String MOD_ID = "stepcrafter";

    private StepCrafterIdentifierUtil() {
    }

    public static ResourceLocation createStepCrafterIdentifier(final String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
    }

    public static MutableComponent createStepCrafterTranslation(final String category, final String value) {
        return Component.translatable(createStepCrafterTranslationKey(category, value));
    }

    public static String createStepCrafterTranslationKey(final String category, final String value) {
        return String.format("%s.%s.%s", category, MOD_ID, value);
    }

    public static MutableComponent createStepCrafterTranslationAsHeading(final String category, final String value) {
        return Component.literal("<")
            .append(createStepCrafterTranslation(category, value))
            .append(">")
            .withStyle(ChatFormatting.DARK_GRAY);
    }
}
