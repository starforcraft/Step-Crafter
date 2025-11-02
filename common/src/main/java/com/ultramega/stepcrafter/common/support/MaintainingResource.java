package com.ultramega.stepcrafter.common.support;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Set;

public interface MaintainingResource {
    void stepcrafter$setMaintainingResource(Set<ResourceMinMaxAmount> resources);

    List<ResourceMinMaxAmount> stepcrafter$getMaintainingResources(ResourceKey resource);
}
