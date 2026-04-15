package com.ultramega.stepcrafter.common.stepcrafter.widget;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractYesNoSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class InsertIntoPointedContainerSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final MutableComponent TITLE =
        createStepCrafterTranslation("gui", "step_crafter.insert_into_pointed_container");
    private static final MutableComponent HELP =
        createStepCrafterTranslation("gui", "step_crafter.insert_into_pointed_container.help");
    private static final ResourceLocation YES =
        createStepCrafterIdentifier("widget/side_button/insert_into_pointed_container/yes");
    private static final ResourceLocation NO =
        createStepCrafterIdentifier("widget/side_button/insert_into_pointed_container/no");

    public InsertIntoPointedContainerSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE, YES, NO);
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
