package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.support.MaintainingResource;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ResourceRepositoryImpl.class)
public abstract class MixinResourceRepositoryImpl<T> implements MaintainingResource {
    @Shadow(remap = false)
    @Final
    private Set<ResourceKey> stickyResources;

    @Unique
    private Set<ResourceMinMaxAmount> stepcrafter$maintainingResources = new HashSet<>();

    @ModifyArg(
        method = "sort",
        at = @At(
            value = "INVOKE",
            target = "Lcom/refinedmods/refinedstorage/api/resource/repository/ViewList;createSorted(Lcom/refinedmods/refinedstorage/api/resource/list/ResourceList;"
                + "Ljava/util/Set;Ljava/util/Comparator;Lcom/refinedmods/refinedstorage/api/resource/repository/ResourceRepositoryMapper;Ljava/util/function/Predicate;)"
                + "Lcom/refinedmods/refinedstorage/api/resource/repository/ViewList;"
        ),
        index = 1, // target stickyResources
        remap = false
    )
    private Set<ResourceKey> modifyStickyResources(final Set<ResourceKey> original) {
        final Set<ResourceKey> merged = new HashSet<>(original);
        merged.addAll(this.stepcrafter$maintainingResources.stream().map(ResourceMinMaxAmount::resource).toList());
        return merged;
    }

    @Redirect(method = "updateExisting", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"), remap = false)
    private boolean checkForMaintainingResources(final Set<?> instance, final Object resource) {
        if (instance == this.stickyResources && resource instanceof ResourceKey) {
            final boolean original = this.stickyResources.contains(resource);
            final boolean newCondition = this.stepcrafter$maintainingResources.stream().noneMatch(r -> r.resource().equals(resource));

            // Replace "!sticky.contains(resource)" with: "!sticky.contains(resource) && !maintaining.contains(resource)"
            return original || !newCondition;
        }

        return instance.contains(resource);
    }

    @Override
    public void stepcrafter$setMaintainingResource(final Set<ResourceMinMaxAmount> resources) {
        this.stepcrafter$maintainingResources = resources;
        this.sort();
    }

    @Override
    public List<ResourceMinMaxAmount> stepcrafter$getMaintainingResources(final ResourceKey resource) {
        return this.stepcrafter$maintainingResources.stream().filter(r -> r.resource().equals(resource)).toList();
    }

    @Shadow(remap = false)
    public abstract void sort();
}
