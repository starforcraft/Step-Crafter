package com.ultramega.stepcrafter.common.support.patternresource;

import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.core.NullableType;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.autocrafting.PatternInventory;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternResourceContainerImpl extends PatternInventory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternResourceContainerImpl.class);

    private final ResourceMinMaxAmount[] slots;
    @Nullable
    private final ResourceFactory primaryResourceFactory;
    @Nullable
    private final Set<ResourceFactory> alternativeResourceFactories;
    private final boolean isFilter;
    private final Supplier<Integer> amountOfSlotUpgrades;

    @Nullable
    private Consumer<Integer> changedListener;

    public PatternResourceContainerImpl(final int size,
                                        final Supplier<@NullableType Level> levelSupplier,
                                        @Nullable final ResourceFactory primaryResourceFactory,
                                        @Nullable final Set<ResourceFactory> alternativeResourceFactories,
                                        final boolean isFilter,
                                        final Supplier<Integer> amountOfSlotUpgrades) {
        super(size, levelSupplier);
        this.slots = new ResourceMinMaxAmount[size];
        this.primaryResourceFactory = primaryResourceFactory;
        this.alternativeResourceFactories = alternativeResourceFactories;
        this.isFilter = isFilter;
        this.amountOfSlotUpgrades = amountOfSlotUpgrades;
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
        this.set(index, new ResourceMinMaxAmount(resourceKey, 0L, 0L, 1L, false));
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
        return this.slots[index].minAmount();
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
        return this.slots[index].maxAmount();
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
        return this.slots[index].batchSize();
    }

    public boolean isCrafting(final int index) {
        final ResourceMinMaxAmount slot = this.slots[index];
        if (slot == null) {
            return false;
        }
        return this.slots[index].isCrafting();
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

        // Disallow inputting processing patterns
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        return super.canPlaceItem(slot, stack) && state != null && state.type() != PatternType.PROCESSING;
    }

    public int size() {
        return this.getContainerSize();
    }

    public boolean isActive(final int slot) {
        return slot < 9 * (this.amountOfSlotUpgrades.get() + 1);
    }

    public boolean isEmpty(final int index) {
        return this.get(index) == null;
    }

    protected final void changed(final int index) {
        if (this.changedListener != null) {
            this.changedListener.accept(index);
        }
    }

    @Override
    public ListTag createTag(final Provider provider) {
        final CompoundTag tag1 = ContainerUtil.write(this, provider);

        final CompoundTag tag2 = new CompoundTag();
        for (int i = 0; i < this.size(); ++i) {
            final ResourceMinMaxAmount slot = this.slots[i];
            if (slot == null) {
                continue;
            }
            this.addToTag(tag2, i, slot, provider);
        }

        final ListTag listTag = new ListTag();
        listTag.add(tag1);
        listTag.add(tag2);
        return listTag;
    }

    private void addToTag(final CompoundTag tag,
                          final int index,
                          final ResourceMinMaxAmount slot,
                          final HolderLookup.Provider provider) {
        final Tag serialized = ResourceMinMaxAmount.CODEC.encode(
            slot,
            provider.createSerializationContext(NbtOps.INSTANCE),
            new CompoundTag()
        ).getOrThrow();
        tag.put("s" + index, serialized);
    }

    @Override
    public void fromTag(final ListTag tag, final Provider provider) {
        ContainerUtil.read(tag.getCompound(0), this, provider);

        final CompoundTag tag2 = tag.getCompound(1);
        for (int i = 0; i < this.size(); ++i) {
            final String key = "s" + i;
            if (!tag2.contains(key)) {
                this.slots[i] = null;
                continue;
            }
            final CompoundTag item = tag2.getCompound(key);
            this.fromTag(i, item, provider);
        }
    }

    private void fromTag(final int index, final CompoundTag tag, final HolderLookup.Provider provider) {
        ResourceMinMaxAmount.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
            .resultOrPartial(error ->
                LOGGER.error("Failed to load resource container slot {} {}: {}", index, tag, error))
            .ifPresent(resourceAmount -> this.set(index, resourceAmount));
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
