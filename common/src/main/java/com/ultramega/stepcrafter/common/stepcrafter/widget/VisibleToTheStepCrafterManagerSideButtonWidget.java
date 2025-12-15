package com.ultramega.stepcrafter.common.stepcrafter.widget;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractYesNoSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class VisibleToTheStepCrafterManagerSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final MutableComponent TITLE =
        createStepCrafterTranslation("gui", "step_crafter.visible_to_the_step_crafter_manager");
    private static final MutableComponent HELP =
        createStepCrafterTranslation("gui", "step_crafter.visible_to_the_step_crafter_manager.help");
    private static final ResourceLocation YES =
        createIdentifier("widget/side_button/autocrafter/visible_to_the_autocrafter_manager/yes");
    private static final ResourceLocation NO =
        createIdentifier("widget/side_button/autocrafter/visible_to_the_autocrafter_manager/no");

    public VisibleToTheStepCrafterManagerSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE, YES, NO);
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
