package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotChangePacket;
import com.ultramega.stepcrafter.common.stepmanager.widget.SearchModeSideButtonWidget;
import com.ultramega.stepcrafter.common.stepmanager.widget.ViewTypeSideButtonWidget;
import com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlotRendering;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.AutoSelectedSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.drawSlotMinMax;
import static com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen.getTooltipsForResource;

public abstract class AbstractStepManagerScreen<T extends AbstractStepManagerContainerMenu> extends AbstractStretchingScreen<T>
    implements PatternOutputRenderingScreen {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/autocrafter_manager.png");
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final ResourceLocation AUTOCRAFTER_NAME = createIdentifier("autocrafter_manager/autocrafter_name");
    private static final int COLUMNS = 9;
    private static final int INACTIVE_COLOR = 0xFF5B5B5B;

    private static final MutableComponent HELP_ALL =
        createTranslation("gui", "autocrafter_manager.search_mode.all.help");
    private static final MutableComponent HELP_PATTERN_INPUTS =
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_inputs.help");
    private static final MutableComponent HELP_PATTERN_OUTPUTS =
        createTranslation("gui", "autocrafter_manager.search_mode.pattern_outputs.help");
    private static final MutableComponent HELP_AUTOCRAFTER_NAMES =
        createTranslation("gui", "autocrafter_manager.search_mode.autocrafter_names.help");

    private final Inventory playerInventory;

    @Nullable
    private SearchFieldWidget searchField;

    @Nullable
    private List<ClientTooltipComponent> tooltip;

    public AbstractStepManagerScreen(final T menu,
                                     final Inventory playerInventory,
                                     final Component title) {
        super(menu, playerInventory, new TextMarquee(title, 70));
        this.playerInventory = playerInventory;
        this.inventoryLabelY = 75;
        this.imageWidth = 193;
        this.imageHeight = 176;
    }

    @Override
    protected void init(final int rows) {
        super.init(rows);

        this.getMenu().setListener(() -> {
            this.resize();
            this.updateScrollbar();
            this.scrollbarChanged(rows);
        });

        if (this.searchField == null) {
            this.searchField = new SearchFieldWidget(
                this.font,
                this.leftPos + 94 + 1,
                this.topPos + 6 + 1,
                73 - 6,
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            this.searchField.setX(this.leftPos + 94 + 1);
            this.searchField.setY(this.topPos + 6 + 1);
        }
        this.updateScrollbar();

        this.addWidget(this.searchField);
        this.searchField.setResponder(value -> this.getMenu().setQuery(value));

        this.addRenderableWidget(new SearchIconWidget(
            this.leftPos + 79,
            this.topPos + 5,
            () -> this.getSearchModeHelp().copy().withStyle(ChatFormatting.GRAY),
            this.searchField
        ));

        this.addSideButton(new RedstoneModeSideButtonWidget(this.getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        this.addSideButton(new ViewTypeSideButtonWidget(this.getMenu()));
        this.addSideButton(new SearchModeSideButtonWidget(this.getMenu(), this::getSearchModeHelp, this.menu.isStepCrafterManager()));
        this.addSideButton(new AutoSelectedSideButtonWidget(this.searchField));
    }

    private Component getSearchModeHelp() {
        return switch (this.menu.getSearchMode()) {
            case ALL -> HELP_ALL;
            case PATTERN_INPUTS -> HELP_PATTERN_INPUTS;
            case PATTERN_OUTPUTS -> HELP_PATTERN_OUTPUTS;
            case STEP_NAMES -> HELP_AUTOCRAFTER_NAMES;
        };
    }

    private void updateScrollbar() {
        final int totalRows = this.menu.getGroups()
            .stream()
            .map(group -> group.isVisible() ? group.getVisibleRows() + 1 : 0)
            .reduce(0, Integer::sum);
        this.updateScrollbar(totalRows);
    }

    @Override
    protected void scrollbarChanged(final int rows) {
        super.scrollbarChanged(rows);
        final int scrollbarOffset = this.getScrollbarOffset();
        for (int i = 0; i < this.menu.getStepSlots().size(); ++i) {
            final StepManagerSlot slot = this.menu.getStepSlots().get(i);
            Platform.INSTANCE.setSlotY(slot, slot.getOriginalY() - scrollbarOffset);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (this.searchField != null) {
            this.searchField.render(graphics, 0, 0, 0);
        }
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (this.searchField != null && this.searchField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (this.searchField != null && this.searchField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (this.menu.isStepCrafterManager()) {
            if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
                if (AbstractAdvancedBaseScreen.tryOpenResourceAmountScreen(this.minecraft, this.getMenu(), this.playerInventory,
                    this, resourceSlot, this.showBatchSize(), this.pressShiftToOpenConfig())) {
                    return true;
                }
            }
        } else {
            if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
                if (!AbstractAdvancedBaseScreen.tryOpenResourceAmountScreen(this.minecraft, this.getMenu(), this.playerInventory,
                    this, resourceSlot, this.showBatchSize(), this.pressShiftToOpenConfig())) {
                    Platform.INSTANCE.sendPacketToServer(new PatternResourceSlotChangePacket(this.hoveredSlot.index, clickedButton == 1));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    protected abstract boolean showBatchSize();

    protected abstract boolean pressShiftToOpenConfig();

    @Override
    protected void renderRows(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        if (!this.menu.isActive()) {
            graphics.fill(
                RenderType.guiOverlay(),
                x + 7 + 1,
                y + TOP_HEIGHT + 1,
                x + 7 + (ROW_SIZE * COLUMNS) - 1,
                y + TOP_HEIGHT + 1 + (ROW_SIZE * rows) - 2,
                INACTIVE_COLOR
            );
            return;
        }
        this.renderGroups(graphics, x, y, topHeight, rows);
        this.renderSlotContents(graphics, mouseX, mouseY, y, topHeight, rows);
    }

    private void renderGroups(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows) {
        final int rowX = x + 7;
        int rowY = y + topHeight - this.getScrollbarOffset();
        for (final AbstractStepManagerContainerMenu.ViewGroup group : this.menu.getGroups()) {
            if (!group.isVisible()) {
                continue;
            }
            if (!isOutOfFrame(y, topHeight, rows, rowY)) {
                graphics.blitSprite(AUTOCRAFTER_NAME, rowX, rowY, 162, ROW_SIZE);
                graphics.drawString(this.font, group.getName(), rowX + 4, rowY + 6, 4210752, false);
            }
            renderGroup(graphics, y, topHeight, rows, group, rowX, rowY);
            rowY += (group.getVisibleRows() + 1) * ROW_SIZE;
        }
    }

    private static void renderGroup(final GuiGraphics graphics,
                                    final int y,
                                    final int topHeight,
                                    final int rows,
                                    final AbstractStepManagerContainerMenu.ViewGroup group,
                                    final int rowX,
                                    final int rowY) {
        int j = 0;
        for (final AbstractStepManagerContainerMenu.SubViewGroup subGroup : group.getSubViewGroups()) {
            for (int i = 0; i < subGroup.getVisibleSlots(); i++) {
                final int slotX = rowX + ((j % COLUMNS) * 18);
                final int slotY = rowY + 18 + ((j / COLUMNS) * 18);
                if (!isOutOfFrame(y, topHeight, rows, slotY)) {
                    graphics.blitSprite(Sprites.SLOT, slotX, slotY, 18, 18);
                }
                ++j;
            }
        }
    }

    private void renderSlotContents(final GuiGraphics graphics,
                                    final int mouseX,
                                    final int mouseY,
                                    final int y,
                                    final int topHeight,
                                    final int rows) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0);
        for (final StepManagerSlot slot : this.menu.getStepSlots()) {
            if (isOutOfFrame(y, topHeight, rows, this.topPos + slot.y)) {
                continue;
            }
            if (this.menu.isStepCrafterManager()) {
                super.renderSlot(graphics, slot);
            } else {
                PatternResourceSlotRendering.render(graphics, slot, 0, 0);
            }
            final boolean hovering = mouseX >= slot.x + this.leftPos
                && mouseX < slot.x + this.leftPos + 16
                && mouseY >= slot.y + this.topPos
                && mouseY < slot.y + this.topPos + 16;
            if (slot.isActive() && hovering) {
                renderSlotHighlight(graphics, slot.x, slot.y, 0);

                if (!this.menu.isStepCrafterManager()) {
                    final PlatformResourceKey resource = slot.getResource();
                    if (resource != null) {
                        this.tooltip = getTooltipsForResource(resource);
                    }
                }
            }
            drawSlotMinMax(graphics, this.font, slot, 0, 0, this.showBatchSize());
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (this.tooltip != null && !this.tooltip.isEmpty()) {
            Platform.INSTANCE.renderTooltip(graphics, this.tooltip, x, y);
            this.tooltip = null;
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderSlot(final GuiGraphics guiGraphics, final Slot slot) {
        if (slot instanceof StepManagerSlot) {
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }

    private static boolean isOutOfFrame(final int y,
                                        final int topHeight,
                                        final int rows,
                                        final int rowY) {
        return (rowY < y + topHeight - ROW_SIZE)
            || (rowY > y + topHeight + (ROW_SIZE * rows));
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphics graphics, final int x, final int y, final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(this.getTexture(), x, y + (ROW_SIZE * row), 0, textureY, this.imageWidth, ROW_SIZE);
        }
    }

    @Override
    protected int getBottomHeight() {
        return 99;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return this.getMenu().containsPattern(stack);
    }
}
