package com.ultramega.stepcrafter.common.support.patternresource;

import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotAmountChangePacket;
import com.ultramega.stepcrafter.common.packet.s2c.PatternResourceSlotUpdatePacket;
import com.ultramega.stepcrafter.common.support.DisabledPatternResourceSlot;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PatternResourceSlot extends ValidatedSlot {
    private final PatternResourceContainerImpl container;
    private final Component helpText;
    private final Level level;
    private final boolean isFilter;

    @Nullable
    private ResourceMinMaxAmount cachedResource;

    public PatternResourceSlot(final PatternResourceContainerImpl container,
                               final int index,
                               final Component helpText,
                               final int x,
                               final int y,
                               final Level level,
                               final boolean isFilter) {
        super(container, index, x, y, stack -> container.canPlaceItem(index, stack));
        this.container = container;
        this.helpText = helpText;
        this.level = level;
        this.isFilter = isFilter;
    }

    public PatternResourceSlot forAmountScreen(final int newX, final int newY) {
        return new DisabledPatternResourceSlot(this.container, this.getContainerSlot(), this.helpText, newX, newY, this.level, this.isFilter);
    }

    public boolean isEmpty() {
        return this.container.isEmpty(this.getContainerSlot());
    }

    public void change(final ItemStack stack, final boolean tryAlternatives) {
        this.container.change(this.getContainerSlot(), stack, tryAlternatives);
    }

    public void change(@Nullable final ResourceMinMaxAmount resourceAmount) {
        if (resourceAmount == null) {
            this.container.remove(this.getContainerSlot());
        } else {
            this.container.set(this.getContainerSlot(), resourceAmount);
        }
    }

    public void setFilter(final PlatformResourceKey resource) {
        if (!this.isFilter) {
            return;
        }
        this.container.setNewResource(this.getContainerSlot(), resource);
    }

    public boolean changeIfEmpty(final ItemStack stack) {
        if (!this.isEmpty()) {
            return false;
        }
        this.container.setNewResource(this.getContainerSlot(), ItemResource.ofItemStack(stack));
        return true;
    }

    public void changeAmount(final long minAmount, final long maxAmount, final long batchSize) {
        this.container.setMinAmount(this.getContainerSlot(), minAmount);
        this.container.setMaxAmount(this.getContainerSlot(), maxAmount);
        this.container.setBatchSize(this.getContainerSlot(), batchSize);
        this.container.changed(this.getContainerSlot());
    }

    public void changeAmountOnClient(final double minAmount, final double maxAmount, final double batchSize) {
        final PlatformResourceKey resource = this.getResource();
        if (resource == null) {
            return;
        }

        final long normalizedMinAmount = resource.getResourceType().normalizeAmount(minAmount);
        final long normalizedMaxAmount = resource.getResourceType().normalizeAmount(maxAmount);
        final long normalizedBatchSize = resource.getResourceType().normalizeAmount(batchSize);
        Platform.INSTANCE.sendPacketToServer(new PatternResourceSlotAmountChangePacket(this.index, normalizedMinAmount, normalizedMaxAmount, normalizedBatchSize));
    }

    @Nullable
    public PlatformResourceKey getResource() {
        return this.container.getResource(this.getContainerSlot());
    }

    public long getMinAmount() {
        return this.container.getMinAmount(this.getContainerSlot());
    }

    public long getMaxAmount() {
        return this.container.getMaxAmount(this.getContainerSlot());
    }

    public long getBatchSize() {
        return this.container.getBatchSize(this.getContainerSlot());
    }

    public boolean isCrafting() {
        return this.container.isCrafting(this.getContainerSlot());
    }

    public boolean contains(final ItemStack stack) {
        return ItemStack.matches(stack, this.getItem());
    }

    public void broadcastChanges(final Player player) {
        final ResourceMinMaxAmount currentResourceAmount = this.container.get(this.getContainerSlot());
        if (!Objects.equals(currentResourceAmount, this.cachedResource)) {
            this.cachedResource = currentResourceAmount;
            this.broadcastChange((ServerPlayer) player, currentResourceAmount);
        }
    }

    private void broadcastChange(final ServerPlayer player, @Nullable final ResourceMinMaxAmount contents) {
        Platform.INSTANCE.sendPacketToClient(player, new PatternResourceSlotUpdatePacket(this.index, Optional.ofNullable(contents)));
    }

    public boolean isFilter() {
        return this.isFilter;
    }

    @Override
    public boolean isActive() {
        return this.container.isActive(this.getContainerSlot());
    }

    @Override
    public boolean mayPickup(final Player player) {
        return super.mayPickup(player) && !this.isFilter;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return super.mayPlace(stack) && !this.isFilter;
    }

    @Nullable
    public ResourceFactory getPrimaryResourceFactory() {
        return this.container.getPrimaryResourceFactory();
    }

    @Nullable
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return this.container.getAlternativeResourceFactories();
    }

    public Component getHelpText() {
        return this.helpText;
    }
}
