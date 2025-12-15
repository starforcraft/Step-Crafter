package com.ultramega.stepcrafter.common.stepmanager;

import com.ultramega.stepcrafter.common.packet.s2c.StepManagerActivePacket;
import com.ultramega.stepcrafter.common.stepcraftermanager.StepCrafterManagerContainerMenu;
import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceContainerImpl;
import com.ultramega.stepcrafter.common.support.patternresource.PatternResourceSlot;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerViewType;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractStepManagerContainerMenu extends AbstractPatternResourceContainerMenu implements ScreenSizeListener, StepManagerWatcher {
    private final Inventory playerInventory;
    private final List<ViewGroup> groups;
    private final List<StepManagerSlot> stepSlots = new ArrayList<>();

    @Nullable
    private StepManagerListener listener;
    @Nullable
    private AbstractStepManagerBlockEntity stepManager;
    private String query = "";
    private boolean active;

    public AbstractStepManagerContainerMenu(final MenuType<? extends AbstractStepManagerContainerMenu> menu,
                                            final int syncId,
                                            final Inventory playerInventory,
                                            final StepManagerData data) {
        super(menu, syncId, playerInventory.player);
        this.playerInventory = playerInventory;
        this.registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.groups = data.groups().stream().map(g -> ViewGroup.from(playerInventory.player.level(), g, this.isStepCrafterManager())).toList();
        this.active = data.active();
        this.resized(0, 0, 0);
    }

    protected AbstractStepManagerContainerMenu(final MenuType<? extends AbstractStepManagerContainerMenu> menu,
                                               final int syncId,
                                               final Inventory playerInventory,
                                               final AbstractStepManagerBlockEntity stepManager,
                                               final List<AbstractStepManagerBlockEntity.Group> groups) {
        super(menu, syncId, playerInventory.player);
        this.playerInventory = playerInventory;
        this.stepManager = stepManager;
        this.stepManager.addWatcher(this);
        this.registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            stepManager::getRedstoneMode,
            stepManager::setRedstoneMode
        ));
        this.groups = Collections.emptyList();
        this.addServerSideSlots(groups);
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (this.stepManager != null) {
            this.stepManager.removeWatcher(this);
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        if (this.stepManager == null) {
            return true;
        }
        return Container.stillValidBlockEntity(this.stepManager, player);
    }

    void setListener(final StepManagerListener listener) {
        this.listener = listener;
    }

    void setQuery(final String query) {
        this.query = query;
        this.notifyListener();
    }

    protected void notifyListener() {
        if (this.listener != null) {
            this.listener.slotsChanged();
        }
    }

    public boolean isStepCrafterManager() {
        return this instanceof StepCrafterManagerContainerMenu;
    }

    private void addServerSideSlots(final List<AbstractStepManagerBlockEntity.Group> serverGroups) {
        for (final AbstractStepManagerBlockEntity.Group group : serverGroups) {
            this.addServerSideSlots(group);
        }
        this.addPlayerInventory(this.playerInventory, 0, 0);
    }

    private void addServerSideSlots(final AbstractStepManagerBlockEntity.Group group) {
        for (final AbstractStepManagerBlockEntity.SubGroup subGroup : group.subGroups()) {
            final PatternResourceContainerImpl container = subGroup.container();
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (!container.isActive(i)) {
                    continue;
                }
                this.addSlot(new PatternResourceSlot(container, i, Component.empty(), 0, 0, this.playerInventory.player.level(), !this.isStepCrafterManager()));
            }
        }
    }

    public boolean containsPattern(final ItemStack stack) {
        for (final Slot slot : this.stepSlots) {
            if (slot.getItem() == stack) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        this.initializeGroups(playerInventoryY, topYStart, topYEnd);
    }

    private void initializeGroups(final int playerInventoryY, final int topYStart, final int topYEnd) {
        this.resetSlots();
        this.stepSlots.clear();
        final int rowX = 7 + 1;
        final int startY = topYStart - 18;
        int rowY = topYStart;
        for (final ViewGroup group : this.groups) {
            rowY += this.initializeGroup(group, rowX, rowY, startY, topYEnd);
        }
        this.addPlayerInventory(this.playerInventory, 8, playerInventoryY);
    }

    private int initializeGroup(final ViewGroup group,
                                final int rowX,
                                final int rowY,
                                final int startY,
                                final int topYEnd) {
        int slotsWithinGroup = 0;
        for (final SubViewGroup subGroup : group.subViewGroups) {
            int slotsWithinSubGroup = 0;
            final boolean visible = this.active && this.isVisible(subGroup);
            for (int i = 0; i < subGroup.backingInventory.getContainerSize(); i++) {
                if (!subGroup.backingInventory.isActive(i)) {
                    continue;
                }
                final int slotX = rowX + ((slotsWithinGroup % 9) * 18);
                final int slotY = rowY + 18 + ((slotsWithinGroup / 9) * 18);
                final boolean slotVisible = visible && this.isSlotVisible(this.playerInventory.player.level(), group, i);
                final StepManagerSlot slot = new StepManagerSlot(
                    subGroup.backingInventory,
                    i,
                    Component.empty(),
                    slotX,
                    slotY,
                    IntIntPair.of(startY, topYEnd),
                    this.playerInventory.player.level(),
                    !this.isStepCrafterManager(),
                    slotVisible
                );
                this.addSlot(slot);
                if (slotVisible) {
                    this.stepSlots.add(slot);
                    ++slotsWithinGroup;
                    ++slotsWithinSubGroup;
                }
            }
            subGroup.visibleSlots = slotsWithinSubGroup;
        }
        group.visibleSlots = slotsWithinGroup;
        if (slotsWithinGroup == 0) {
            return 0;
        }
        return (group.getVisibleRows() + 1) * 18;
    }

    private boolean isVisible(final SubViewGroup subGroup) {
        return switch (this.getViewType()) {
            case VISIBLE -> subGroup.visibleToTheStepManager;
            case NOT_FULL -> !subGroup.full;
            case ALL -> true;
        };
    }

    private boolean isSlotVisible(final Level level, final ViewGroup group, final int index) {
        final String normalizedQuery = this.query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        return this.getSearchMode().isSlotVisible(group, level, normalizedQuery, index);
    }

    List<ViewGroup> getGroups() {
        return this.groups;
    }

    List<StepManagerSlot> getStepSlots() {
        return this.stepSlots;
    }

    public abstract StepManagerSearchMode getSearchMode();

    public abstract void setSearchMode(StepManagerSearchMode searchMode);

    public abstract AutocrafterManagerViewType getViewType();

    public abstract void setViewType(AutocrafterManagerViewType toggle);

    public void setActive(final boolean active) {
        this.active = active;
        this.notifyListener();
    }

    boolean isActive() {
        return this.active;
    }

    @Override
    public void activeChanged(final boolean newActive) {
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            com.refinedmods.refinedstorage.common.Platform.INSTANCE.sendPacketToClient(serverPlayerEntity, new StepManagerActivePacket(newActive));
        }
    }

    @Override
    public int getAmountSlotUpgrades() {
        throw new UnsupportedOperationException("getAmountSlotUpgrades() is not supported on Step Crafter Manager.");
    }

    static class SubViewGroup {
        private final boolean visibleToTheStepManager;
        private final PatternResourceContainerImpl backingInventory;
        private final boolean full;
        private int visibleSlots;

        private SubViewGroup(final boolean visibleToTheStepManager,
                             final PatternResourceContainerImpl backingInventory,
                             final boolean full) {
            this.visibleToTheStepManager = visibleToTheStepManager;
            this.backingInventory = backingInventory;
            this.full = full;
        }

        private static SubViewGroup from(final StepManagerData.SubGroup subGroup, final Level level, final boolean isStepCrafterManager) {
            final PatternResourceContainerImpl backingInventory =
                new PatternResourceContainerImpl(subGroup.slotCount(), () -> level, null, null, !isStepCrafterManager, subGroup::slotUpgradesCount);
            return new SubViewGroup(
                subGroup.visibleToTheStepManager(),
                backingInventory,
                subGroup.full()
            );
        }

        int getVisibleSlots() {
            return this.visibleSlots;
        }

        boolean hasPatternInput(final Level level, final String normalizedQuery, final int index) {
            final ItemStack patternStack = this.backingInventory.getItem(index);
            return RefinedStorageApi.INSTANCE.getPattern(patternStack, level).map(
                pattern -> hasIngredient(pattern.layout().ingredients(), normalizedQuery)
            ).orElse(false);
        }

        boolean hasPatternOutput(final Level level, final String normalizedQuery, final int index) {
            final ItemStack patternStack = this.backingInventory.getItem(index);
            return RefinedStorageApi.INSTANCE.getPattern(patternStack, level).map(
                pattern -> hasResource(pattern.layout().outputs(), normalizedQuery)
            ).orElse(false);
        }

        private static boolean hasIngredient(final List<Ingredient> ingredients, final String normalizedQuery) {
            return ingredients.stream().flatMap(i -> i.inputs().stream()).anyMatch(key ->
                hasResource(normalizedQuery, key));
        }

        private static boolean hasResource(final List<ResourceAmount> resources, final String normalizedQuery) {
            return resources.stream().map(ResourceAmount::resource).anyMatch(key ->
                hasResource(normalizedQuery, key));
        }

        private static boolean hasResource(final String normalizedQuery, final ResourceKey key) {
            return RefinedStorageClientApi.INSTANCE.getResourceRendering(key.getClass())
                .getDisplayName(key)
                .getString()
                .toLowerCase(Locale.ROOT)
                .trim()
                .contains(normalizedQuery);
        }
    }

    static class ViewGroup {
        private final String name;
        private final List<SubViewGroup> subViewGroups;
        private final List<SubViewGroup> subViewGroupsView;
        private int visibleSlots;

        private ViewGroup(final String name, final List<SubViewGroup> subViewGroups) {
            this.name = name;
            this.subViewGroups = subViewGroups;
            this.subViewGroupsView = Collections.unmodifiableList(subViewGroups);
        }

        private static ViewGroup from(final Level level, final StepManagerData.Group group, final boolean isStepCrafterManager) {
            return new ViewGroup(
                group.name(),
                group.subGroups().stream().map(subGroup -> SubViewGroup.from(subGroup, level, isStepCrafterManager)).toList()
            );
        }

        String getName() {
            return this.name;
        }

        List<SubViewGroup> getSubViewGroups() {
            return this.subViewGroupsView;
        }

        boolean isVisible() {
            return this.visibleSlots > 0;
        }

        int getVisibleRows() {
            return Math.ceilDiv(this.visibleSlots, 9);
        }

        boolean nameContains(final String normalizedQuery) {
            return this.name.toLowerCase(Locale.ROOT).trim().contains(normalizedQuery);
        }

        boolean hasPatternInput(final Level level, final String normalizedQuery, final int index) {
            return this.subViewGroups.stream().anyMatch(
                subGroup -> subGroup.hasPatternInput(level, normalizedQuery, index)
            );
        }

        boolean hasPatternOutput(final Level level, final String normalizedQuery, final int index) {
            return this.subViewGroups.stream().anyMatch(
                subGroup -> subGroup.hasPatternOutput(level, normalizedQuery, index)
            );
        }
    }
}
