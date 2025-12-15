package com.ultramega.stepcrafter.common.support;

import com.ultramega.stepcrafter.common.resourceconfiguration.ResourceConfigurationScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.resource.ItemResourceRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.MouseClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.support.Sprites.AUTOCRAFTING_INDICATOR;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslationAsHeading;

public abstract class AbstractAdvancedBaseScreen<T extends AbstractContainerMenu> extends AbstractBaseScreen<T> {
    private static final SmallTextClientTooltipComponent SHIFT_CLICK_TO_CLEAR = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("gui", "filter_slot.shift_click_to_clear")
    );
    private static final Component SHIFT_CLICK_TO_CONFIGURE_AMOUNT =
        createStepCrafterTranslationAsHeading("gui", "filter_slot.shift_click_to_configure_amount");
    private static final Component CLICK_TO_CONFIGURE_AMOUNT =
        createTranslationAsHeading("gui", "filter_slot.click_to_configure_amount");
    private static final ClientTooltipComponent EMPTY_FILTER = ClientTooltipComponent.create(
        createTranslationAsHeading("gui", "filter_slot.empty_filter").getVisualOrderText()
    );
    private static final ClientTooltipComponent EMPTY_PATTERN_SLOT = ClientTooltipComponent.create(
        createTranslationAsHeading("gui", "autocrafter.empty_pattern_slot").getVisualOrderText()
    );

    private static final ResourceLocation TEXTURE_0 = createStepCrafterIdentifier("textures/gui/step_crafter_requester_0.png");
    private static final ResourceLocation TEXTURE_1 = createStepCrafterIdentifier("textures/gui/step_crafter_requester_1.png");
    private static final ResourceLocation TEXTURE_2 = createStepCrafterIdentifier("textures/gui/step_crafter_requester_2.png");
    private static final ResourceLocation TEXTURE_3 = createStepCrafterIdentifier("textures/gui/step_crafter_requester_3.png");
    private static final ResourceLocation TEXTURE_4 = createStepCrafterIdentifier("textures/gui/step_crafter_requester_4.png");

    protected final Inventory playerInventory;

    private int lastAmountSlotUpgrades = -1;

    public AbstractAdvancedBaseScreen(final T menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 228;
        this.determineStuff();
    }

    protected AbstractAdvancedBaseScreen(final T menu, final Inventory playerInventory, final TextMarquee title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 228;
        this.determineStuff();
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        this.determineStuff();
        super.render(graphics, mouseX, mouseY, delta);
    }

    protected void renderSlotMinMax(final GuiGraphics graphics, final boolean showBatchSize) {
        if (!(this.getMenu() instanceof AbstractPatternResourceContainerMenu patternContainerMenu)) {
            return;
        }

        for (final PatternResourceSlot slot : patternContainerMenu.getPatternResourceSlots()) {
            drawSlotMinMax(graphics, this.font, slot, this.leftPos, this.topPos, showBatchSize);
        }
    }

    public static void drawSlotMinMax(final GuiGraphics graphics,
                                      final Font font,
                                      final PatternResourceSlot slot,
                                      final int leftPos,
                                      final int topPos,
                                      final boolean showBatchSize) {
        final PoseStack poseStack = graphics.pose();
        if (slot.isActive() && slot.getResource() != null) {
            final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(slot.getResource().getClass());
            final String formattedMinAmount = rendering.formatAmount(slot.getMinAmount(), true);
            final String formattedMaxAmount = rendering.formatAmount(slot.getMaxAmount(), true);

            poseStack.pushPose();
            poseStack.translate(leftPos + slot.x, topPos + slot.y, 260F);
            poseStack.scale(0.5F, 0.5F, 0.5F);

            graphics.drawString(font, formattedMinAmount, 0, 0, 0xFFFFFF);
            if (showBatchSize) {
                final String formattedBatchSize = ItemResourceRendering.INSTANCE.formatAmount(slot.getBatchSize(), true);
                graphics.drawString(font, formattedBatchSize, 0, 12, 0xFFFFFF);
            }
            graphics.drawString(font, formattedMaxAmount, 0, 24, 0xFFFFFF);

            if (slot.isCrafting()) {
                graphics.blitSprite(AUTOCRAFTING_INDICATOR, 22, -2, 10, 10);
            }
            poseStack.popPose();
        }
    }

    private void determineStuff() {
        int amountSlotUpgrades = 0;
        if (this.getMenu() instanceof AbstractPatternResourceContainerMenu patternContainerMenu) {
            amountSlotUpgrades = patternContainerMenu.getAmountSlotUpgrades();
        }
        if (this.lastAmountSlotUpgrades == amountSlotUpgrades) {
            return;
        }

        this.inventoryLabelY = 42 + amountSlotUpgrades * 18;
        this.imageHeight = 137 + amountSlotUpgrades * 18;
        if (this.lastAmountSlotUpgrades != -1) {
            this.init();
        }

        this.lastAmountSlotUpgrades = amountSlotUpgrades;
    }

    protected boolean tryOpenResourceAmountScreen(final PatternResourceSlot slot, final boolean showBatchSize, final boolean hasToPressShift) {
        return tryOpenResourceAmountScreen(this.minecraft, this.getMenu(), this.playerInventory, this, slot, showBatchSize, hasToPressShift);
    }

    public static boolean tryOpenResourceAmountScreen(@Nullable final Minecraft minecraft,
                                                      final AbstractContainerMenu containerMenu,
                                                      final Inventory playerInventory,
                                                      final Screen parent,
                                                      final PatternResourceSlot slot,
                                                      final boolean showBatchSize,
                                                      final boolean hasToPressShift) {
        final boolean isFilterSlot = slot.getResource() != null;
        final boolean isNotTryingToRemoveFilter = hasToPressShift == hasShiftDown();
        final boolean isNotCarryingItem = containerMenu.getCarried().isEmpty();
        final boolean canOpen = isFilterSlot
            && isNotTryingToRemoveFilter
            && isNotCarryingItem;
        if (canOpen && minecraft != null) {
            minecraft.setScreen(new ResourceConfigurationScreen(parent, playerInventory, slot, showBatchSize));
        }
        return canOpen;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (this.hoveredSlot instanceof PatternResourceSlot patternSlot) {
            final List<ClientTooltipComponent> tooltip = this.getPatternResourceSlotTooltip(this.menu.getCarried(), patternSlot);
            if (!tooltip.isEmpty()) {
                Platform.INSTANCE.renderTooltip(graphics, tooltip, x, y);
                return;
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    public final List<ClientTooltipComponent> getPatternResourceSlotTooltip(final ItemStack carried, final PatternResourceSlot slot) {
        final ResourceKey resource = slot.getResource();
        if (resource == null) {
            return this.getTooltipForEmptyPatternResourceSlot(carried, slot);
        }
        return this.getPatternResourceSlotTooltip(resource, slot);
    }

    protected List<ClientTooltipComponent> getPatternResourceSlotTooltip(final ResourceKey resource, final PatternResourceSlot slot) {
        final List<ClientTooltipComponent> tooltip = getTooltipsForResource(resource);
        this.addPatternResourceSlotTooltips(slot, tooltip);

        return tooltip;
    }

    public static List<ClientTooltipComponent> getTooltipsForResource(final ResourceKey resource) {
        return RefinedStorageClientApi.INSTANCE
            .getResourceRendering(resource.getClass())
            .getTooltip(resource)
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
    }

    private List<ClientTooltipComponent> getTooltipForEmptyPatternResourceSlot(final ItemStack carried,
                                                                               final PatternResourceSlot slot) {
        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(slot.isFilter() ? EMPTY_FILTER : EMPTY_PATTERN_SLOT);
        tooltip.addAll(this.getPatternResourceSlotHelpTooltip(carried, slot));
        tooltip.add(HelpClientTooltipComponent.create(slot.getHelpText()));
        return tooltip;
    }

    private List<ClientTooltipComponent> getPatternResourceSlotHelpTooltip(final ItemStack carried,
                                                                           final PatternResourceSlot slot) {
        if (carried.isEmpty() || slot.getPrimaryResourceFactory() == null || slot.getAlternativeResourceFactories() == null) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        slot.getPrimaryResourceFactory().create(carried).ifPresent(primaryResourceInstance -> lines.add(
            MouseClientTooltipComponent.resource(
                MouseClientTooltipComponent.Type.LEFT,
                primaryResourceInstance.resource(),
                null
            )
        ));
        for (final ResourceFactory alternativeResourceFactory : slot.getAlternativeResourceFactories()) {
            final var result = alternativeResourceFactory.create(carried);
            result.ifPresent(alternativeResourceInstance -> lines.add(MouseClientTooltipComponent.resource(
                MouseClientTooltipComponent.Type.RIGHT,
                alternativeResourceInstance.resource(),
                null
            )));
        }
        return lines;
    }

    protected void addPatternResourceSlotTooltips(final PatternResourceSlot slot, final List<ClientTooltipComponent> tooltip) {
        tooltip.add(ClientTooltipComponent.create(Component.translatable("tooltip.stepcrafter.resource_configuration.minimum_amount", slot.getMinAmount())
            .withStyle(ChatFormatting.GRAY).getVisualOrderText()));
        tooltip.add(ClientTooltipComponent.create(Component.translatable("tooltip.stepcrafter.resource_configuration.maximum_amount", slot.getMaxAmount())
            .withStyle(ChatFormatting.GRAY).getVisualOrderText()));
        if (slot.isFilter()) {
            tooltip.add(ClientTooltipComponent.create(Component.translatable("tooltip.stepcrafter.resource_configuration.batch_size", slot.getBatchSize())
                .withStyle(ChatFormatting.GRAY).getVisualOrderText()));
        }

        tooltip.add(new SmallTextClientTooltipComponent(slot.isFilter() ? CLICK_TO_CONFIGURE_AMOUNT : SHIFT_CLICK_TO_CONFIGURE_AMOUNT));
        if (slot.isFilter()) {
            tooltip.add(SHIFT_CLICK_TO_CLEAR);
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return switch (this.lastAmountSlotUpgrades) {
            case 1 -> TEXTURE_1;
            case 2 -> TEXTURE_2;
            case 3 -> TEXTURE_3;
            case 4 -> TEXTURE_4;
            default -> TEXTURE_0;
        };
    }
}
