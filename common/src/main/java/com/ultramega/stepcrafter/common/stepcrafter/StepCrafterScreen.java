package com.ultramega.stepcrafter.common.stepcrafter;

import com.ultramega.stepcrafter.common.support.AbstractAdvancedBaseScreen;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class StepCrafterScreen extends AbstractAdvancedBaseScreen<StepCrafterContainerMenu>
    implements StepCrafterContainerMenu.Listener, PatternOutputRenderingScreen {
    private static final Component EDIT = createTranslation("gui", "autocrafter.edit_name");

    private static final ResourceLocation NAME_BACKGROUND = createIdentifier("widget/autocrafter_name");
    private static final List<String> CRAFTER_NAME_HISTORY = new ArrayList<>();

    @Nullable
    private EditBox nameField;
    @Nullable
    private Button editButton;
    private boolean editName;

    public StepCrafterScreen(final StepCrafterContainerMenu menu,
                             final Inventory playerInventory,
                             final Component title) {
        super(menu, playerInventory, new TextMarquee(title, getTitleMaxWidth()));
    }

    @Override
    protected void init() {
        super.init();
        this.getMenu().setListener(this);

        this.addSideButton(new RedstoneModeSideButtonWidget(this.getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));

        // TODO: add a side button about redstone pulse (either request all items or only based on the strength)
        //  also show "not enough ingredients", "crafting" or "checkmark" icon in a corner

        if (this.nameField == null) {
            this.nameField = new SearchFieldWidget(
                this.font,
                this.leftPos + 8 + 1,
                this.topPos + 6 + 1,
                159 - 6,
                new History(CRAFTER_NAME_HISTORY)
            );
            this.nameField.setValue(this.title.getString());
            this.nameField.setBordered(false);
            this.nameField.setCanLoseFocus(false);
            this.addWidget(this.nameField);
        }

        if (this.editButton == null) {
            this.editButton = this.addRenderableWidget(Button.builder(EDIT, button -> this.setEditName(true))
                .pos(this.getEditButtonX(), this.topPos + this.titleLabelY - 3)
                .size(getEditButtonWidth(), 14)
                .build());

            this.setEditName(false);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (this.nameField != null && this.editName) {
            this.nameField.render(graphics, mouseX, mouseY, partialTicks);
        }
        this.renderSlotMinMax(graphics, false);
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        super.renderBg(graphics, delta, mouseX, mouseY);
        if (this.editName) {
            graphics.blitSprite(NAME_BACKGROUND, this.leftPos + 7, this.topPos + 5, 162, 12);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        if (this.editName) {
            this.renderPlayerInventoryTitle(graphics);
            return;
        }
        super.renderLabels(graphics, mouseX, mouseY);
    }

    private void setEditName(final boolean editName) {
        this.editName = editName;
        if (this.nameField != null) {
            this.nameField.visible = editName;
            this.nameField.setFocused(editName);
            this.nameField.setCanLoseFocus(!editName);
            if (editName) {
                this.setFocused(this.nameField);
            } else {
                this.setFocused(null);
            }
        }
        if (this.editButton != null) {
            this.editButton.visible = !editName;
        }
    }

    @Override
    public boolean charTyped(final char unknown1, final int unknown2) {
        return (this.nameField != null && this.editName && this.nameField.charTyped(unknown1, unknown2))
            || super.charTyped(unknown1, unknown2);
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers) {
        if (this.nameField != null && this.editName) {
            if (this.nameField.keyPressed(key, scanCode, modifiers)) {
                return true;
            }
            if (this.nameField.isFocused() && this.saveOrCancel(key)) {
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (this.hoveredSlot instanceof PatternResourceSlot resourceSlot && resourceSlot.isActive()) {
            if (this.tryOpenResourceAmountScreen(resourceSlot, false, true)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private boolean saveOrCancel(final int key) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            this.getMenu().changeName(requireNonNull(this.nameField).getValue());
            this.setEditName(false);
            return true;
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.setEditName(false);
            requireNonNull(this.nameField).setValue(this.titleMarquee.getText().getString());
            return true;
        }
        return false;
    }

    @Override
    public void nameChanged(final Component name) {
        this.titleMarquee.setText(name);
        if (this.nameField != null) {
            this.nameField.setValue(name.getString());
        }
        if (this.editButton != null) {
            this.editButton.setX(this.getEditButtonX());
        }
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return this.getMenu().containsPattern(stack);
    }

    private int getEditButtonX() {
        return this.leftPos + this.titleLabelX + this.titleMarquee.getEffectiveWidth(this.font) + 2;
    }

    private static int getTitleMaxWidth() {
        final int editButtonWidth = getEditButtonWidth();
        return TITLE_MAX_WIDTH - editButtonWidth - 10;
    }

    private static int getEditButtonWidth() {
        return Minecraft.getInstance().font.width(EDIT) + 8;
    }
}
