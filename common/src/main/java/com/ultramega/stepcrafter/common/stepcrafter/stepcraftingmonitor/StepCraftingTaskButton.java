package com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor;

import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskState;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.Locale;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterTranslation;
import static com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.StepCraftingMonitorScreen.TASK_BUTTON_HEIGHT;
import static com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.StepCraftingMonitorScreen.TASK_BUTTON_WIDTH;

public class StepCraftingTaskButton extends AbstractButton {
    private final TaskStatus.TaskInfo task;
    private final TextMarquee text;
    private final Consumer<TaskId> onPress;
    private final StepCraftingTaskButton.StateProvider stateProvider;

    StepCraftingTaskButton(final int x,
                           final int y,
                           final TaskStatus.TaskInfo task,
                           final Consumer<TaskId> onPress,
                           final StepCraftingTaskButton.StateProvider stateProvider) {
        super(x, y, TASK_BUTTON_WIDTH, TASK_BUTTON_HEIGHT, Component.empty());
        this.task = task;
        final ResourceKey resource = task.resource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        this.text = new TextMarquee(
            rendering.getDisplayName(resource),
            TASK_BUTTON_WIDTH - 16 - 4 - 4 - 4,
            0xFFFFFF,
            true,
            true
        );
        this.onPress = onPress;
        this.stateProvider = stateProvider;
    }

    TaskId getTaskId() {
        return this.task.id();
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        this.renderResourceIcon(graphics);
        final int yOffset = SmallText.isSmall() ? 5 : 3;
        final int textX = this.getX() + 3 + 16 + 3;
        final int textY = this.getY() + yOffset;
        this.text.render(graphics, textX, textY, Minecraft.getInstance().font, this.isHovered);
        final int ySpacing = SmallText.isSmall() ? 7 : 8;
        final long percentageCompleted = Math.round(
            this.stateProvider.getPercentageCompleted(this.task.id()) * 100
        );
        SmallText.render(graphics, Minecraft.getInstance().font, percentageCompleted + "%", textX, textY + ySpacing,
            0xFFFFFF, true, SmallText.DEFAULT_SCALE);
        this.updateTooltip();
    }

    private void renderResourceIcon(final GuiGraphics graphics) {
        final ResourceKey resource = this.task.resource();
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        final int resourceX = this.getX() + 3;
        final int resourceY = this.getY() + 4;
        rendering.render(resource, graphics, resourceX, resourceY);
        ResourceSlotRendering.renderAmount(graphics, resourceX, resourceY, this.task.amount(), rendering);
    }

    private void updateTooltip() {
        if (this.isHovered) {
            final String runningTime = this.getRunningTimeText();
            final MutableComponent runningTimeText =
                createTranslation("gui", "autocrafting_monitor.running_time", runningTime);
            final StepTaskState state = this.stateProvider.getState(this.task.id());
            if (state == null) {
                this.setTooltip(Tooltip.create(runningTimeText));
                return;
            }
            final MutableComponent stateText = createStepCrafterTranslation("gui", "step_crafting_monitor.state."
                + state.toString().toLowerCase(Locale.ROOT));
            this.setTooltip(Tooltip.create(stateText.append("\n").append(runningTimeText.withStyle(ChatFormatting.GRAY))));
        } else {
            this.setTooltip(null);
        }
    }

    private String getRunningTimeText() {
        final int totalSecs = (int) (System.currentTimeMillis() - this.task.startTime()) / 1000;
        final int hours = totalSecs / 3600;
        final int minutes = (totalSecs % 3600) / 60;
        final int seconds = totalSecs % 60;
        final String runningTime;
        if (hours > 0) {
            runningTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            runningTime = String.format("%02d:%02d", minutes, seconds);
        }
        return runningTime;
    }

    @Override
    public void onPress() {
        this.onPress.accept(this.task.id());
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }

    interface StateProvider {
        double getPercentageCompleted(TaskId taskId);

        @Nullable
        StepTaskState getState(TaskId taskId);
    }
}
