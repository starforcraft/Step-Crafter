package com.ultramega.stepcrafter.common.mixin;

import com.ultramega.stepcrafter.common.network.stepcrafter.StepCraftingNetworkComponent;
import com.ultramega.stepcrafter.common.packet.c2s.RequestMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.s2c.SetMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskProvider;
import com.ultramega.stepcrafter.common.support.MaintainingResource;
import com.ultramega.stepcrafter.common.support.NetworkGetter;
import com.ultramega.stepcrafter.common.support.RequestableMaintainingResources;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.util.Objects.requireNonNull;

@Mixin(AbstractGridContainerMenu.class)
public abstract class MixinAbstractGridContainerMenu implements MaintainingResource, RequestableMaintainingResources, StepTaskProvider {
    @Shadow(remap = false)
    @Final
    protected Inventory playerInventory;
    @Shadow(remap = false)
    @Nullable
    private Grid grid;

    @Shadow(remap = false)
    @Final
    private ResourceRepository<GridResource> repository;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;"
        + "Lcom/refinedmods/refinedstorage/common/grid/GridData;)V", at = @At("TAIL"))
    private void clientInit(final MenuType<? extends AbstractGridContainerMenu> menuType,
                            final int syncId,
                            final Inventory playerInv,
                            final GridData gridData,
                            final CallbackInfo ci) {
        Platform.INSTANCE.sendPacketToServer(new RequestMaintainableResourcesPacket());
    }

    @Override
    @Unique
    public void stepcrafter$sendMaintainingResourcesToClient() {
        if (this.grid instanceof NetworkGetter networkGetter && this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            final Network network = networkGetter.stepcrafter$getNetwork();
            if (network != null) {
                Platform.INSTANCE.sendPacketToClient(serverPlayer, new SetMaintainableResourcesPacket(this.stepcrafter$getMaintainableResources(network)));
            }
        }
    }

    @Override
    public void stepcrafter$setMaintainingResource(final Set<ResourceMinMaxAmount> resources) {
        if (this.repository instanceof MaintainingResource maintainingResource) {
            maintainingResource.stepcrafter$setMaintainingResource(resources);
        }
    }

    @Override
    public List<ResourceMinMaxAmount> stepcrafter$getMaintainingResources(final ResourceKey resource) {
        throw new UnsupportedOperationException("isMaintainingResource shouldn't be called here");
    }

    @Unique
    public List<ResourceMinMaxAmount> stepcrafter$getMaintainableResources(final Network network) {
        return new ArrayList<>(requireNonNull(network)
            .getComponent(StepCraftingNetworkComponent.class)
            .getOutputs());
    }

    @Override
    @Unique
    public Optional<TaskId> stepcrafter$startStepCraftingTask(final ResourceKey resource,
                                                              final long amount,
                                                              final Actor actor,
                                                              final boolean notify,
                                                              final CancellationToken cancellationToken) {
        if (this.grid instanceof NetworkGetter networkGetter) {
            final Network network = networkGetter.stepcrafter$getNetwork();
            if (network != null) {
                network.getComponent(StepCraftingNetworkComponent.class).stepcrafter$startStepCraftingTask(resource, amount, actor, notify, cancellationToken);
            }
        }

        return Optional.empty();
    }
}
