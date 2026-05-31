package com.ultramega.stepcrafter.common.support.patternresource;

import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;
import com.ultramega.stepcrafter.common.support.ResourceStatus;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class PatternResourceContainerImpl extends PatternInventory {
    @Nullable
    private final ResourceMinMaxAmount[] slots;
    @Nullable
    private final ResourceFactory primaryResourceFactory;
    @Nullable
    private final Set<ResourceFactory> alternativeResourceFactories;
    private final boolean isFilter;
    private final Supplier<Integer> slotUpgradesCount;

    @Nullable
    private Consumer<Integer> changedListener;

    public PatternResourceContainerImpl(final int size,
                                        final Supplier<@Nullable Level> levelSupplier,
                                        @Nullable final ResourceFactory primaryResourceFactory,
                                        @Nullable final Set<ResourceFactory> alternativeResourceFactories,
                                        final boolean isFilter,
                                        final Supplier<Integer> slotUpgradesCount) {
        super(size, levelSupplier);
        this.slots = new ResourceMinMaxAmount[size];
        this.primaryResourceFactory = primaryResourceFactory;
        this.alternativeResourceFactories = alternativeResourceFactories;
        this.isFilter = isFilter;
        this.slotUpgradesCount = slotUpgradesCount;
    }

    public void setChangedListener(@Nullable final Consumer<Integer> changedListener) {
        this.changedListener = changedListener;
    }

    public void change(final int index, final ItemStack stack, final boolean tryAlternatives) {
        if (tryAlternatives && this.alternativeResourceFactories != null) {
            for (final ResourceFactory resourceFactory : this.alternativeResourceFactories) {
                final Optional<ResourceAmount> result = resourceFactory.create(stack);
                if (result.isPresent()) {
                    this.setNewResource(index, result.get().resource());
                    return;
                }
            }
        }
        if (this.primaryResourceFactory != null) {
            this.primaryResourceFactory.create(stack).ifPresentOrElse(
                resource -> this.setNewResource(index, resource.resource()),
                () -> this.remove(index)
            );
        }
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        if (this.isFilter) {
            return;
        }

        if (this.slots[slot] == null && !stack.isEmpty()) {
            this.setNewResource(slot, ItemResource.ofItemStack(stack));
        } else if (this.slots[slot] != null && stack.isEmpty()) {
            this.remove(slot);
        }
    }

    public void remove(final int index) {
        this.slots[index] = null;
    }

    public void setNewResource(final int index, final ResourceKey resourceKey) {
        this.set(index, new ResourceMinMaxAmount(resourceKey, 0L, 0L, 1L, ResourceStatus.FINISHED));
    }

    public void set(final int index, final ResourceMinMaxAmount resourceAmount) {
        this.slots[index] = resourceAmount;
    }

    public void setMinAmount(final int index, final long amount) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return;
        }

        this.slots[index] = slot.toBuilder().minAmount(amount).build();
    }

    public long getMinAmount(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return 0L;
        }
        return slot.minAmount();
    }

    public void setMaxAmount(final int index, final long amount) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return;
        }

        this.slots[index] = slot.toBuilder().maxAmount(amount).build();
    }

    public long getMaxAmount(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return 0L;
        }
        return slot.maxAmount();
    }

    public void setBatchSize(final int index, final long amount) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return;
        }

        this.slots[index] = slot.toBuilder().batchSize(amount).build();
    }

    public long getBatchSize(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return 0L;
        }
        return slot.batchSize();
    }

    public ResourceStatus getStatus(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return ResourceStatus.FINISHED;
        }
        return slot.status();
    }

    @Nullable
    public ResourceMinMaxAmount get(final int index) {
        return this.slots[index];
    }

    public @Nullable PlatformResourceKey getResource(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return null;
        }
        return (PlatformResourceKey) slot.resource();
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        if (this.isFilter) {
            return true;
        }

        // TODO: allow processing patterns again?
        // Disallow inputting processing patterns
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        return super.canPlaceItem(slot, stack) && state != null && state.type() != PatternType.PROCESSING;
    }

    public int size() {
        return this.getContainerSize();
    }

    public boolean isActive(final int slot) {
        return slot < 9 * (this.slotUpgradesCount.get() + 1);
    }

    public boolean isEmpty(final int index) {
        return this.get(index) == null;
    }

    protected final void changed(final int index) {
        if (this.changedListener != null) {
            this.changedListener.accept(index);
        }
    }

    public void load(final PatternResourceContainerContents contents) {
        final List<Optional<ResourceMinMaxAmount>> contentsSlots = contents.slots();
        for (int i = 0; i < this.size() && i < contentsSlots.size(); ++i) {
            final Optional<ResourceMinMaxAmount> slotContents = contentsSlots.get(i);
            this.slots[i] = slotContents.orElse(null);
        }
    }

    @Nullable
    public ResourceFactory getPrimaryResourceFactory() {
        return this.primaryResourceFactory;
    }

    @Nullable
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return this.alternativeResourceFactories;
    }
}
