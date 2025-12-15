package com.ultramega.stepcrafter.common.stepmanager.widget;

import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerContainerMenu;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class SearchModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "autocrafter_manager.search_mode");
    private static final List<MutableComponent> SUBTEXT_ALL = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.all").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_PATTERN_INPUTS = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_inputs").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_PATTERN_OUTPUTS = List.of(
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_outputs").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_STEP_CRAFTER_NAMES = List.of(
        createStepCrafterTranslation("gui", "step_crafter_manager.search_mode.step_crafter_names").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_STEP_REQUESTER_NAMES = List.of(
        createStepCrafterTranslation("gui", "step_requester_manager.search_mode.step_requester_names").withStyle(ChatFormatting.GRAY)
    );
    private static final ResourceLocation SPRITE_ALL =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/all");
    private static final ResourceLocation SPRITE_PATTERN_INPUTS =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/pattern_inputs");
    private static final ResourceLocation SPRITE_PATTERN_OUTPUTS =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/pattern_outputs");
    private static final ResourceLocation SPRITE_AUTOCRAFTER_NAMES =
        createIdentifier("widget/side_button/autocrafter_manager/search_mode/autocrafter_names");

    private final AbstractStepManagerContainerMenu containerMenu;
    private final Supplier<Component> helpTextSupplier;
    private final boolean isStepCrafterManager;

    public SearchModeSideButtonWidget(final AbstractStepManagerContainerMenu containerMenu,
                                      final Supplier<Component> helpTextSupplier,
                                      final boolean isStepCrafterManager) {
        super(createPressAction(containerMenu));
        this.containerMenu = containerMenu;
        this.helpTextSupplier = helpTextSupplier;
        this.isStepCrafterManager = isStepCrafterManager;
    }

    private static OnPress createPressAction(final AbstractStepManagerContainerMenu containerMenu) {
        return btn -> containerMenu.setSearchMode(containerMenu.getSearchMode().toggle());
    }

    @Override
    protected ResourceLocation getSprite() {
        return switch (this.containerMenu.getSearchMode()) {
            case ALL -> SPRITE_ALL;
            case PATTERN_INPUTS -> SPRITE_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> SPRITE_PATTERN_OUTPUTS;
            case STEP_NAMES -> SPRITE_AUTOCRAFTER_NAMES;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (this.containerMenu.getSearchMode()) {
            case ALL -> SUBTEXT_ALL;
            case PATTERN_INPUTS -> SUBTEXT_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> SUBTEXT_PATTERN_OUTPUTS;
            case STEP_NAMES -> this.isStepCrafterManager ? SUBTEXT_STEP_CRAFTER_NAMES : SUBTEXT_STEP_REQUESTER_NAMES;
        };
    }

    @Override
    protected Component getHelpText() {
        return this.helpTextSupplier.get();
    }
}
