package com.ultramega.stepcrafter.common.resourceconfiguration;

import com.ultramega.stepcrafter.common.mixin.ActionButtonInvoker;
import com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlotRendering;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.support.amount.ActionButton;
import com.refinedmods.refinedstorage.common.support.amount.ActionIcon;
import com.refinedmods.refinedstorage.common.support.amount.AmountOperations;
import com.refinedmods.refinedstorage.common.support.amount.DoubleAmountOperations;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public class ResourceConfigurationScreen extends AbstractAdvancedBaseScreen<ResourceConfigurationContainerMenu> implements PatternOutputRenderingScreen {
    private static final ResourceLocation TEXTURE_1 = createStepCrafterIdentifier("textures/gui/resource_configuration_1.png");
    private static final ResourceLocation TEXTURE_2 = createStepCrafterIdentifier("textures/gui/resource_configuration_2.png");
    private static final MutableComponent TITLE = createTranslation("gui", "configure_amount");
    private static final MutableComponent SET_TEXT = createTranslation("gui", "configure_amount.set");

    private static final int ACTION_BUTTON_HEIGHT = 20;
    private static final int ACTION_BUTTON_WIDTH = 58;

    @Nullable
    protected ActionButton confirmButton;
    @Nullable
    protected EditBox minAmountField;
    @Nullable
    protected EditBox maxAmountField;
    @Nullable
    protected EditBox batchSizeField;

    private final Screen parent;
    private final PatternResourceSlot resourceSlot;
    private final AmountOperations<Double> amountOperations;

    private final double minInitialAmount;
    private final double maxInitialAmount;
    private final double batchSizeInitialAmount;
    private final boolean showBatchSize;
    private final double minAmount;
    private final double maxAmount;

    public ResourceConfigurationScreen(final Screen parent, final Inventory playerInventory, final PatternResourceSlot resourceSlot, final boolean showBatchSize) {
        super(new ResourceConfigurationContainerMenu(resourceSlot, 120 + (showBatchSize ? 25 : 0), 42), playerInventory, TITLE);
        this.parent = parent;
        this.resourceSlot = resourceSlot;
        this.amountOperations = DoubleAmountOperations.INSTANCE;

        this.minInitialAmount = resourceSlot.getMinAmount();
        this.maxInitialAmount = resourceSlot.getMaxAmount();
        this.batchSizeInitialAmount = resourceSlot.getBatchSize();
        this.showBatchSize = showBatchSize;
        this.minAmount = resourceSlot.getResource() != null ? resourceSlot.getResource().getResourceType().getDisplayAmount(0) : 0;
        this.maxAmount = Double.MAX_VALUE;

        this.imageWidth = 179;
        this.imageHeight = 99;
    }

    @Override
    protected void init() {
        super.init();
        this.addConfirmButton();
        this.addAmountFields();
        //TODO: increment buttons?
    }

    @Override
    protected void renderSlot(final GuiGraphics graphics, final Slot slot) {
    }

    @Override
    protected void renderResourceSlots(final GuiGraphics graphics) {
        PatternResourceSlotRendering.render(graphics, this.getMenu().getResourceSlot(), this.leftPos, this.topPos);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.pose().popPose();
        final boolean hoveringOverTitle = this.isHovering(
            this.titleLabelX,
            this.titleLabelY,
            this.titleMarquee.getEffectiveWidth(this.font),
            this.font.lineHeight,
            mouseX,
            mouseY
        );
        this.titleMarquee.render(graphics, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, this.font, hoveringOverTitle);
        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0.0F);

        final Component minText = Component.translatable("gui.stepcrafter.resource_configuration.minimum");
        final Component maxText = Component.translatable("gui.stepcrafter.resource_configuration.maximum");
        final Component batchSizeText = Component.translatable("gui.stepcrafter.resource_configuration.batch_size");
        int biggestTextWidth = Math.max(this.font.width(minText), this.font.width(maxText));
        int width = 40;
        int y = 39;
        if (this.showBatchSize) {
            biggestTextWidth = Math.max(biggestTextWidth, this.font.width(batchSizeText));
            width += 27;
            y -= 6;
        }
        final int x = (width - biggestTextWidth) / 2;
        graphics.drawString(this.font, minText, x, y, 4210752, false);
        graphics.drawString(this.font, maxText, x, y + 14, 4210752, false);
        if (this.showBatchSize) {
            graphics.drawString(this.font, batchSizeText, x, y + 14 * 2, 4210752, false);
        }
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (this.hoveredSlot instanceof PatternResourceSlot patternSlot && patternSlot.getResource() != null) {
            final List<ClientTooltipComponent> tooltip = this.getPatternResourceSlotTooltip(patternSlot.getResource(), patternSlot);
            if (!tooltip.isEmpty()) {
                Platform.INSTANCE.renderTooltip(graphics, tooltip, x, y);
                return;
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    protected void addPatternResourceSlotTooltips(final PatternResourceSlot slot, final List<ClientTooltipComponent> tooltip) {
    }

    private void addAmountFields() {
        final int x = this.leftPos + 40 + (this.showBatchSize ? 25 : 0);
        final int y = this.topPos + 39 - (this.showBatchSize ? 6 : 0);
        final int width = 68 - 6;

        final String minOriginalValue = this.minAmountField != null ? this.minAmountField.getValue() : null;
        this.minAmountField = new EditBox(this.font, x, y, width, this.font.lineHeight, Component.empty());
        this.minAmountField.setBordered(false);
        this.minAmountField.setTextColor(0xFFFFFF);
        if (minOriginalValue != null) {
            this.minAmountField.setValue(minOriginalValue);
            this.onAmountFieldChanged(this.minAmountField, 0);
        }
        this.updateAmount(this.minAmountField, this.minInitialAmount);
        this.minAmountField.setVisible(true);
        this.minAmountField.setCanLoseFocus(true);
        this.minAmountField.setFocused(true);
        this.minAmountField.setResponder(value -> {
            this.onAmountFieldChanged(this.minAmountField, 0);
            this.onAmountFieldChanged(this.maxAmountField, 1);

            if (this.maxAmountField != null) {
                final Triple<Optional<Double>, Optional<Double>, Optional<Double>> amounts = this.getAndValidateAmount();
                if (amounts.getLeft().isPresent() && amounts.getMiddle().isEmpty()) {
                    this.maxAmountField.setValue(this.minAmountField.getValue());
                }
            }
        });
        this.setFocused(this.minAmountField);
        this.addRenderableWidget(this.minAmountField);

        final String maxOriginalValue = this.maxAmountField != null ? this.maxAmountField.getValue() : null;
        this.maxAmountField = new EditBox(this.font, x, y + 14, width, this.font.lineHeight, Component.empty());
        this.maxAmountField.setBordered(false);
        this.maxAmountField.setTextColor(0xFFFFFF);
        if (maxOriginalValue != null) {
            this.maxAmountField.setValue(maxOriginalValue);
            this.onAmountFieldChanged(this.maxAmountField, 1);
        }
        this.updateAmount(this.maxAmountField, this.maxInitialAmount);
        this.maxAmountField.setVisible(true);
        this.maxAmountField.setCanLoseFocus(true);
        this.maxAmountField.setResponder(value -> this.onAmountFieldChanged(this.maxAmountField, 1));
        this.addRenderableWidget(this.maxAmountField);

        if (!this.showBatchSize) {
            return;
        }
        final String batchOriginalValue = this.batchSizeField != null ? this.batchSizeField.getValue() : null;
        this.batchSizeField = new EditBox(this.font, x, y + 14 * 2, width, this.font.lineHeight, Component.empty());
        this.batchSizeField.setBordered(false);
        this.batchSizeField.setTextColor(0xFFFFFF);
        if (batchOriginalValue != null) {
            this.batchSizeField.setValue(batchOriginalValue);
            this.onAmountFieldChanged(this.batchSizeField, 2);
        }
        this.updateAmount(this.batchSizeField, this.batchSizeInitialAmount);
        this.batchSizeField.setVisible(true);
        this.batchSizeField.setCanLoseFocus(true);
        this.batchSizeField.setResponder(value -> this.onAmountFieldChanged(this.batchSizeField, 2));
        this.addRenderableWidget(this.batchSizeField);
    }

    private void addConfirmButton() {
        final ActionButton button = ActionButtonInvoker.create(
            this.leftPos + 116,
            this.topPos + 74,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT,
            SET_TEXT,
            btn -> this.tryConfirm(true)
        );
        button.setIcon(this.getConfirmButtonIcon());
        this.confirmButton = this.addRenderableWidget(button);
    }

    @Nullable
    protected ActionIcon getConfirmButtonIcon() {
        return ActionIcon.SET;
    }

    protected final void updateAmount(@Nullable final EditBox amountField, final Double amount) {
        if (amountField == null) {
            return;
        }
        amountField.setValue(this.amountOperations.format(amount));
    }

    protected void onAmountFieldChanged(@Nullable final EditBox amountField, final int type) {
        if (amountField == null) {
            return;
        }

        final Triple<Optional<Double>, Optional<Double>, Optional<Double>> amounts = this.getAndValidateAmount();
        final boolean valid = amounts.getLeft().isPresent() && amounts.getMiddle().isPresent() && amounts.getRight().isPresent();
        final boolean thisValid = switch (type) {
            case 0 -> amounts.getLeft().isPresent();
            case 1 -> amounts.getMiddle().isPresent();
            case 2 -> amounts.getRight().isPresent();
            default -> false;
        };
        if (this.confirmButton != null) {
            this.confirmButton.active = valid;
            this.confirmButton.setIcon(valid ? this.getConfirmButtonIcon() : ActionIcon.ERROR);
        } else {
            this.tryConfirm(false);
        }
        amountField.setTextColor(thisValid ? 0xFFFFFF : 0xFF5555);
    }

    protected final Triple<Optional<Double>, Optional<Double>, Optional<Double>> getAndValidateAmount() {
        if (this.minAmountField == null || this.maxAmountField == null) {
            return Triple.of(Optional.empty(), Optional.empty(), Optional.empty());
        }

        final Optional<Double> min = this.amountOperations.parse(this.minAmountField.getValue())
            .flatMap(amount -> this.amountOperations.validate(amount, this.minAmount, this.maxAmount));
        Optional<Double> max = this.amountOperations.parse(this.maxAmountField.getValue())
            .flatMap(amount -> this.amountOperations.validate(amount, this.minAmount, this.maxAmount));
        final Optional<Double> batchSize = this.batchSizeField != null ? this.amountOperations.parse(this.batchSizeField.getValue())
            .flatMap(amount -> this.amountOperations.validate(amount, this.minAmount, this.maxAmount)) : Optional.of(1D);

        // max must be >= min
        if (min.isPresent() && max.isPresent()) {
            if (max.get() < min.get()) {
                max = Optional.empty();
            }
        }

        return Triple.of(min, max, batchSize);
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return true;
    }

    private void tryConfirm(final boolean closeToParent) {
        final Triple<Optional<Double>, Optional<Double>, Optional<Double>> amounts = this.getAndValidateAmount();
        if (amounts.getLeft().isPresent() && amounts.getMiddle().isPresent() && amounts.getRight().isPresent()
            && this.confirm(amounts.getLeft().get(), amounts.getMiddle().get(), amounts.getRight().get())) {
            if (closeToParent) {
                this.tryCloseToParent();
            }
        }
    }

    @Override
    public void onClose() {
        this.tryCloseToParent();
    }

    private void tryCloseToParent() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    protected boolean confirm(final Double newMinAmount, final Double newMaxAmount, final Double newBatchSize) {
        this.resourceSlot.changeAmountOnClient(newMinAmount, newMaxAmount, newBatchSize);
        return true;
    }

    @Override
    protected ResourceLocation getTexture() {
        return !this.showBatchSize ? TEXTURE_1 : TEXTURE_2;
    }
}
