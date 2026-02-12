package com.ultramega.stepcrafter.common.stepcrafter.preview;

import com.ultramega.stepcrafter.common.mixin.AutocraftingRequestInvoker;

import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;
import com.refinedmods.refinedstorage.common.support.amount.AbstractAmountScreen;
import com.refinedmods.refinedstorage.common.support.amount.ActionIcon;
import com.refinedmods.refinedstorage.common.support.amount.AmountScreenConfiguration;
import com.refinedmods.refinedstorage.common.support.amount.DoubleAmountOperations;
import com.refinedmods.refinedstorage.common.support.widget.CheckboxWidget;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector3f;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class StepCraftingPreviewScreen extends AbstractAmountScreen<StepCraftingPreviewContainerMenu, Double>
    implements StepCraftingPreviewListener {
    private static final ResourceLocation TEXTURE = createStepCrafterIdentifier("textures/gui/step_crafting_preview.png");
    private static final MutableComponent TITLE = createStepCrafterTranslation("gui", "stepcrafting_preview.title");
    private static final MutableComponent START = createTranslation("gui", "autocrafting_preview.start");
    private static final MutableComponent PENDING = createTranslation("gui", "autocrafting_preview.pending");
    private static final MutableComponent CANCELLING = createTranslation("gui", "autocrafting_preview.cancelling");
    private static final MutableComponent CANCELLING_FORCE_CLOSE = createTranslation("gui",
        "autocrafting_preview.cancelling.force_close");
    private static final MutableComponent NOTIFY = createTranslation("gui", "autocrafting_preview.notify");
    private static final MutableComponent NOTIFY_HELP = createTranslation("gui", "autocrafting_preview.notify.help");

    @Nullable
    private CheckboxWidget notifyCheckbox;

    private final RateLimiter requestRateLimiter = RateLimiter.create(1);

    private boolean requestedCancellation;

    public StepCraftingPreviewScreen(final Screen parent,
                                     final Inventory playerInventory,
                                     final AutocraftingRequest request) {
        this(new StepCraftingPreviewContainerMenu(request), parent, playerInventory);
    }

    public StepCraftingPreviewScreen(final StepCraftingPreviewContainerMenu menu, final Inventory playerInventory) {
        this(menu, null, playerInventory);
    }

    public StepCraftingPreviewScreen(final StepCraftingPreviewContainerMenu menu,
                                     @Nullable final Screen parent,
                                     final Inventory playerInventory) {
        super(
            menu,
            parent,
            playerInventory,
            TITLE,
            AmountScreenConfiguration.AmountScreenConfigurationBuilder.<Double>create()
                .withInitialAmount(1D)
                .withIncrementsTop(1, 10, 64)
                .withIncrementsTopStartPosition(new Vector3f(80, 20, 0))
                .withIncrementsBottom(-1, -10, -64)
                .withIncrementsBottomStartPosition(new Vector3f(80, 71, 0))
                .withAmountFieldPosition(new Vector3f(77, 51, 0))
                .withActionButtonsStartPosition(new Vector3f(7, 222 - 107, 0))
                .withHorizontalActionButtons(true)
                .withMinAmount(menu::getMinAmount)
                .withResetAmount(1D)
                .withConfirmButtonText(START)
                .build(),
            DoubleAmountOperations.INSTANCE
        );
        this.imageWidth = 254;
        this.imageHeight = 142;
        this.getMenu().setListener(this);
    }

    @Override
    protected void init() {
        super.init();

        if (this.confirmButton != null) {
            this.confirmButton.setMessage(START);
            this.confirmButton.setIcon(ActionIcon.START);
        }

        final boolean selected = this.notifyCheckbox == null ? this.menu.isNotify() : this.notifyCheckbox.isSelected();
        this.notifyCheckbox = new CheckboxWidget(
            this.leftPos + this.imageWidth - this.font.width(NOTIFY) - 4 - 9 - 6,
            this.topPos + 222 - 107 + 6,
            NOTIFY,
            Minecraft.getInstance().font,
            selected,
            CheckboxWidget.Size.SMALL
        );
        this.notifyCheckbox.setHelpTooltip(NOTIFY_HELP);
        this.notifyCheckbox.setOnPressed((checkbox, notify) -> this.menu.setNotify(notify));
        this.addRenderableWidget(this.notifyCheckbox);
    }

    @Nullable
    @Override
    protected ActionIcon getConfirmButtonIcon() {
        return null;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void onAmountFieldChanged() {
        if (this.amountField == null || this.confirmButton == null) {
            return;
        }
        this.getAndValidateAmount().ifPresentOrElse(amount -> {
            this.confirmButton.active = true;
            this.confirmButton.setIcon(ActionIcon.START);
            this.amountField.setTextColor(0xFFFFFF);
        }, () -> {
            this.confirmButton.active = false;
            this.confirmButton.setIcon(ActionIcon.ERROR);
            this.amountField.setTextColor(0xFF5555);
        });
    }

    @Override
    protected boolean beforeClose() {
        if (this.requestedCancellation) {
            return true;
        }
        this.requestedCancellation = true;
        if (this.cancelButton != null) {
            this.cancelButton.active = false;
            this.cancelButton.setMessage(CANCELLING);
            this.cancelButton.setTooltip(Tooltip.create(CANCELLING_FORCE_CLOSE));
        }
        this.getMenu().sendCancelRequest();
        return false;
    }

    public void cancelResponseReceived() {
        // If we get the cancellation response late, and have force closed the screen,
        // and meanwhile have a new screen open already, we do not want to close right now.
        if (!this.requestedCancellation) {
            return;
        }
        this.close();
    }

    @Override
    protected void reset() {
        this.updateAmount(((AutocraftingRequestInvoker) this.getMenu().getRequest()).stepcrafter$getAmount());
    }

    @Override
    protected boolean confirm(final Double amount) {
        this.getMenu().sendRequest(amount, this.notifyCheckbox == null ? this.menu.isNotify() : this.notifyCheckbox.isSelected());
        this.close();
        return false;
    }
}
