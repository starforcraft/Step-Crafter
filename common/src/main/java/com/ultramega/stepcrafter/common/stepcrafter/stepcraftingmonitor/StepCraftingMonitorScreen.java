package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus;
import com.ultramega.stepcrafter.common.stepcrafter.status.StepTaskStatus.ItemType;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.ERROR;
import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.MathUtil.darkenARGB;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;

public class StepCraftingMonitorScreen extends AbstractBaseScreen<AbstractStepCraftingMonitorContainerMenu>
    implements StepCraftingMonitorListener {
    static final int TASK_BUTTON_HEIGHT = 168 / 7;
    static final int TASK_BUTTON_WIDTH = 64;

    private static final int ROWS_VISIBLE = 6;
    private static final int COLUMNS = 3;
    private static final int ITEMS_AREA_HEIGHT = 179;

    private static final int ITEM_COLOR = 0xFFDBDBDB;
    private static final int MISSING_COLOR = 0xFFDB5E5E;

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_WIDTH = 221;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/autocrafting_monitor.png");
    private static final ResourceLocation ROW = createIdentifier("autocrafting_monitor/row");
    private static final ResourceLocation TASKS = createIdentifier("autocrafting_monitor/tasks");

    private static final MutableComponent CANCEL = createTranslation("gui", "autocrafting_monitor.cancel");
    private static final MutableComponent CANCEL_ALL = createTranslation("gui", "autocrafting_monitor.cancel_all");

    private static final MutableComponent DISCLAIMER = createStepCrafterTranslation("gui", "step_crafting_monitor.may_not_reflect_actual_amount");

    private static final int TASKS_WIDTH = 91;
    private static final int TASKS_HEIGHT = 183;
    private static final int TASKS_INNER_WIDTH = 64;
    private static final int TASKS_INNER_HEIGHT = 168;
    private static final int TASKS_VISIBLE = 7;

    @Nullable
    private ScrollbarWidget taskItemsScrollbar;
    @Nullable
    private ScrollbarWidget taskButtonsScrollbar;

    @Nullable
    private Button cancelButton;
    @Nullable
    private Button cancelAllButton;

    private final List<StepCraftingTaskButton> taskButtons = new ArrayList<>();

    public StepCraftingMonitorScreen(final AbstractStepCraftingMonitorContainerMenu menu,
                                     final Inventory playerInventory,
                                     final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 254;
        this.imageHeight = 231;
    }

    @Override
    protected void init() {
        super.init();
        this.taskItemsScrollbar = new ScrollbarWidget(
            this.leftPos + 235,
            this.topPos + 20,
            ScrollbarWidget.Type.NORMAL,
            ITEMS_AREA_HEIGHT
        );
        this.taskItemsScrollbar.setEnabled(false);
        this.initTaskButtons();
        this.getMenu().setListener(this);
        this.getExclusionZones().add(new Rect2i(
            this.leftPos - TASKS_WIDTH + 4,
            this.topPos,
            TASKS_WIDTH,
            TASKS_HEIGHT
        ));
        final int cancelButtonsY = this.topPos + 204;
        this.cancelButton = this.addRenderableWidget(Button.builder(CANCEL, button -> this.getMenu().cancelCurrentTask())
            .pos(this.leftPos + 7, cancelButtonsY)
            .size(this.font.width(CANCEL) + 14, 20).build());
        this.cancelButton.active = false;
        this.cancelAllButton = this.addRenderableWidget(Button.builder(CANCEL_ALL, button -> this.getMenu().cancelAllTasks())
            .pos(this.cancelButton.getX() + this.cancelButton.getWidth() + 4, cancelButtonsY)
            .size(this.font.width(CANCEL_ALL) + 14, 20).build());
        this.cancelAllButton.active = false;
        this.getMenu().loadCurrentTask();
        if (this.getMenu().hasProperty(PropertyTypes.REDSTONE_MODE)) {
            this.addSideButton(new RedstoneModeSideButtonWidget(this.getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        }
    }

    @Override
    protected int getSideButtonX() {
        return this.leftPos + this.imageWidth + 2;
    }

    private void initTaskButtons() {
        this.taskButtons.clear();
        this.taskButtonsScrollbar = new ScrollbarWidget(
            this.leftPos - 17 + 4,
            this.getTaskButtonsInnerY(),
            ScrollbarWidget.Type.NORMAL,
            168
        );
        this.taskButtonsScrollbar.setListener(value -> {
            final int scrollOffset = this.taskButtonsScrollbar.isSmoothScrolling()
                ? (int) this.taskButtonsScrollbar.getOffset()
                : (int) this.taskButtonsScrollbar.getOffset() * TASK_BUTTON_HEIGHT;
            for (int i = 0; i < this.taskButtons.size(); i++) {
                final StepCraftingTaskButton taskButton = this.taskButtons.get(i);
                final int y = this.getTaskButtonY(i) - scrollOffset;
                taskButton.setY(y);
                taskButton.visible = this.isTaskButtonVisible(y);
            }
        });
        this.updateTaskButtonsScrollbar();
        for (int i = 0; i < this.getMenu().getTasksView().size(); ++i) {
            final TaskStatus.TaskInfo taskId = this.getMenu().getTasksView().get(i);
            final int buttonY = this.getTaskButtonY(i);
            final StepCraftingTaskButton button = new StepCraftingTaskButton(
                this.getTaskButtonsInnerX(),
                buttonY,
                taskId,
                this.menu::setCurrentTaskId,
                this.menu
            );
            button.visible = this.isTaskButtonVisible(buttonY);
            this.taskButtons.add(this.addWidget(button));
        }
    }

    private boolean isTaskButtonVisible(final int y) {
        if (!this.getMenu().isActive()) {
            return false;
        }
        final int innerY = this.getTaskButtonsInnerY();
        return y >= innerY - TASK_BUTTON_HEIGHT && y <= innerY + TASKS_INNER_HEIGHT;
    }

    private int getTaskButtonY(final int i) {
        return this.getTaskButtonsInnerY() + (i * TASK_BUTTON_HEIGHT);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (this.taskItemsScrollbar != null) {
            this.taskItemsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
        if (this.taskButtonsScrollbar != null) {
            this.taskButtonsScrollbar.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        graphics.blitSprite(TASKS, this.leftPos - TASKS_WIDTH + 4, this.topPos, TASKS_WIDTH, TASKS_HEIGHT);
        final List<StepTaskStatus.Item> items = this.getMenu().getCurrentItems();
        if (items.isEmpty() || this.taskItemsScrollbar == null || !this.getMenu().isActive()) {
            return;
        }
        final int x = this.leftPos + 8;
        final int y = this.topPos + 20;
        graphics.enableScissor(x, y, x + 221, y + ITEMS_AREA_HEIGHT);
        final int rows = Math.ceilDiv(items.size(), COLUMNS);
        for (int i = 0; i < rows; ++i) {
            final int scrollOffset = this.taskItemsScrollbar.isSmoothScrolling()
                ? (int) this.taskItemsScrollbar.getOffset()
                : (int) this.taskItemsScrollbar.getOffset() * ROW_HEIGHT;
            final int yy = y + (i * ROW_HEIGHT) - scrollOffset;
            this.renderRow(graphics, x, yy, i, items, mouseX, mouseY);
        }
        graphics.disableScissor();

        final int tasksInnerX = this.getTaskButtonsInnerX();
        final int tasksInnerY = this.getTaskButtonsInnerY();
        graphics.enableScissor(
            tasksInnerX,
            tasksInnerY,
            tasksInnerX + TASKS_INNER_WIDTH,
            tasksInnerY + TASKS_INNER_HEIGHT
        );
        for (final StepCraftingTaskButton taskButton : this.taskButtons) {
            taskButton.render(graphics, mouseX, mouseY, delta);
        }
        graphics.disableScissor();
    }

    private void renderRow(final GuiGraphics graphics,
                           final int x,
                           final int y,
                           final int i,
                           final List<StepTaskStatus.Item> items,
                           final double mouseX,
                           final double mouseY) {
        if (y <= this.topPos + 20 - ROW_HEIGHT || y > this.topPos + 20 + ITEMS_AREA_HEIGHT) {
            return;
        }
        graphics.blitSprite(ROW, x, y, ROW_WIDTH, ROW_HEIGHT);
        for (int column = i * COLUMNS; column < Math.min(i * COLUMNS + COLUMNS, items.size()); ++column) {
            final StepTaskStatus.Item item = items.get(column);
            final int xx = x + (column % COLUMNS) * 74;
            this.renderItem(graphics, xx, y, item, mouseX, mouseY);
        }
    }

    private static int getItemColor(final StepTaskStatus.Item item, final boolean hovering) {
        return hovering ? darkenARGB(getItemColor(item), 0.1) : getItemColor(item);
    }

    private static int getItemColor(final StepTaskStatus.Item item) {
        if (item.type() == ItemType.MISSING) {
            return MISSING_COLOR;
        }
        return ITEM_COLOR;
    }

    private void renderItem(final GuiGraphics graphics,
                            final int x,
                            final int y,
                            final StepTaskStatus.Item item,
                            final double mouseX,
                            final double mouseY) {
        final boolean hovering = this.isHovering(x - this.leftPos, y - this.topPos, 73, 29, mouseX, mouseY);
        final int color = getItemColor(item, hovering);
        if (color != ITEM_COLOR) {
            graphics.fill(x, y, x + 73, y + 29, color);
        }
        if (item.type() != StepTaskStatus.ItemType.NORMAL) {
            renderItemErrorIcon(graphics, x, y);
        }
        int xx = x + 2;
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(
            item.resource().getClass()
        );
        int yy = y + 7;
        rendering.render(item.resource(), graphics, xx, yy);
        if (this.isHovering(x - this.leftPos, y - this.topPos, 73, 29, mouseX, mouseY)
            && this.isHoveringOverItems(mouseX, mouseY)) {
            this.setDeferredTooltip(List.of(new StepCraftingMonitorItemTooltip(item)));
        }
        if (!SmallText.isSmall()) {
            yy -= 2;
        }
        xx += 16 + 3;
        this.renderItemText(graphics, item, rendering, xx, yy);
    }

    private static void renderItemErrorIcon(final GuiGraphics graphics, final int x, final int y) {
        graphics.blitSprite(
            ERROR,
            x + 73 - ICON_SIZE - 3,
            y + 29 - ICON_SIZE - 3,
            ICON_SIZE,
            ICON_SIZE
        );
    }

    private void renderItemText(final GuiGraphics graphics,
                                final StepTaskStatus.Item item,
                                final ResourceRendering rendering,
                                final int x,
                                final int y) {
        int yy = y;
        this.renderItemText(graphics, "required", rendering, x, yy, item.required());
        yy += 7;
        this.renderItemText(graphics, "used", rendering, x, yy, item.used());
    }

    private void renderItemText(final GuiGraphics graphics,
                                final String type,
                                final ResourceRendering rendering,
                                final int x,
                                final int y,
                                final long amount) {
        SmallText.render(
            graphics,
            this.font,
            createStepCrafterTranslation("gui", "step_crafting_monitor." + type, rendering.formatAmount(amount, true))
                .getVisualOrderText(),
            x,
            y,
            0x404040,
            false,
            SmallText.DEFAULT_SCALE
        );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (this.taskItemsScrollbar != null
            && this.taskItemsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        if (this.taskButtonsScrollbar != null && this.taskButtonsScrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (this.taskItemsScrollbar != null) {
            this.taskItemsScrollbar.mouseMoved(mx, my);
        }
        if (this.taskButtonsScrollbar != null) {
            this.taskButtonsScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        if (this.taskItemsScrollbar != null && this.taskItemsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        if (this.taskButtonsScrollbar != null && this.taskButtonsScrollbar.mouseReleased(mx, my, button)) {
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double z, final double delta) {
        final boolean didTaskItemsScrollbar = this.taskItemsScrollbar != null
            && this.isHoveringOverItems(x, y)
            && this.taskItemsScrollbar.mouseScrolled(x, y, z, delta);
        final boolean didTaskButtonsScrollbar = !didTaskItemsScrollbar
            && this.taskButtonsScrollbar != null
            && this.isHoveringOverTaskButtons(x, y)
            && this.taskButtonsScrollbar.mouseScrolled(x, y, z, delta);
        return didTaskItemsScrollbar || didTaskButtonsScrollbar || super.mouseScrolled(x, y, z, delta);
    }

    private boolean isHoveringOverItems(final double x, final double y) {
        return this.isHovering(8, 20, 221, ITEMS_AREA_HEIGHT, x, y);
    }

    private boolean isHoveringOverTaskButtons(final double x, final double y) {
        final int tasksInnerX = this.getTaskButtonsInnerX() - 1;
        final int tasksInnerY = this.getTaskButtonsInnerY() - 1;
        return this.isHovering(tasksInnerX - this.leftPos, tasksInnerY - this.topPos, 80, 170, x, y);
    }

    private int getTaskButtonsInnerY() {
        return this.topPos + 8;
    }

    private int getTaskButtonsInnerX() {
        return this.leftPos - 83 + 4;
    }

    @Override
    public void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 0.5F);

        final int x = this.imageWidth * 2 - this.font.width(DISCLAIMER) - 8;
        final int y = this.imageHeight * 2 - this.font.lineHeight * 2;

        graphics.drawString(this.font, DISCLAIMER, x, y, 0xFF808080, false);

        graphics.pose().popPose();
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void currentTaskChanged(@Nullable final StepTaskStatus taskStatus) {
        this.updateTaskItemsScrollbar(taskStatus);
        this.updateTaskButtonsScrollbar();
        if (this.cancelButton != null) {
            this.cancelButton.active = this.getMenu().isActive() && taskStatus != null;
        }
        if (this.cancelAllButton != null) {
            this.cancelAllButton.active = this.getMenu().isActive() && !this.menu.getTasksView().isEmpty();
        }
        for (final StepCraftingTaskButton taskButton : this.taskButtons) {
            taskButton.active = taskStatus == null
                || !taskButton.getTaskId().equals(taskStatus.info().id());
            taskButton.visible = this.getMenu().isActive();
        }
    }

    private void updateTaskButtonsScrollbar() {
        if (this.taskButtonsScrollbar == null) {
            return;
        }
        final int totalTaskButtons = this.getMenu().isActive() ? this.getMenu().getTasksView().size() - TASKS_VISIBLE : 0;
        final int maxOffset = this.taskButtonsScrollbar.isSmoothScrolling()
            ? totalTaskButtons * TASK_BUTTON_HEIGHT
            : totalTaskButtons;
        this.taskButtonsScrollbar.setEnabled(maxOffset > 0);
        this.taskButtonsScrollbar.setMaxOffset(maxOffset);
    }

    private void updateTaskItemsScrollbar(@Nullable final StepTaskStatus taskStatus) {
        if (this.taskItemsScrollbar == null) {
            return;
        }
        if (taskStatus == null || !this.getMenu().isActive()) {
            this.taskItemsScrollbar.setEnabled(false);
            this.taskItemsScrollbar.setMaxOffset(0);
            return;
        }
        final int items = taskStatus.ingredients().size();
        final int rows = Math.ceilDiv(items, COLUMNS) - ROWS_VISIBLE;
        this.taskItemsScrollbar.setMaxOffset(this.taskItemsScrollbar.isSmoothScrolling() ? rows * ROW_HEIGHT : rows);
        this.taskItemsScrollbar.setEnabled(rows > 0);
    }

    @Override
    public void taskAdded(final StepTaskStatus taskStatus) {
        this.updateTaskButtonsScrollbar();
        final int buttonY = this.getTaskButtonY(this.getMenu().getTasksView().size() - 1);
        final StepCraftingTaskButton button = new StepCraftingTaskButton(
            this.getTaskButtonsInnerX(),
            buttonY,
            taskStatus.info(),
            this.menu::setCurrentTaskId,
            this.menu
        );
        button.visible = this.isTaskButtonVisible(buttonY);
        this.taskButtons.add(this.addWidget(button));
    }

    @Override
    public void taskRemoved(final TaskId taskId) {
        this.updateTaskButtonsScrollbar();
        this.taskButtons.stream().filter(b -> b.getTaskId().equals(taskId)).findFirst().ifPresent(button -> {
            this.removeWidget(button);
            this.taskButtons.remove(button);
        });
        for (int i = 0; i < this.taskButtons.size(); i++) {
            final StepCraftingTaskButton button = this.taskButtons.get(i);
            button.setY(this.getTaskButtonY(i));
            button.visible = this.isTaskButtonVisible(button.getY());
        }
    }
}
