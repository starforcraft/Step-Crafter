package com.ultramega.stepcrafter.common.stepcrafter.preview;

import com.ultramega.stepcrafter.common.mixin.AutocraftingRequestInvoker;
import com.ultramega.stepcrafter.common.packet.c2s.StepCraftingRequestPacket;
import com.ultramega.stepcrafter.common.registry.Menus;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingRequest;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledResourceSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlotType;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class StepCraftingPreviewContainerMenu extends AbstractResourceContainerMenu {
    private final AutocraftingRequest request;
    @Nullable
    private StepCraftingPreviewListener listener;

    StepCraftingPreviewContainerMenu(final AutocraftingRequest request) {
        this(null, 0, request);
    }

    public StepCraftingPreviewContainerMenu(final int syncId, final PlatformResourceKey resource) {
        this(Menus.INSTANCE.getStepCraftingPreview(), syncId, AutocraftingRequest.of(new ResourceAmount(resource, resource.getResourceType().normalizeAmount(1.0D))));
    }

    public StepCraftingPreviewContainerMenu(@Nullable final MenuType<?> type,
                                            final int syncId,
                                            final AutocraftingRequest request) {
        super(type, syncId);
        this.request = request;
        final ResourceContainer resourceContainer = ResourceContainerImpl.createForFilter(1);
        resourceContainer.set(0, new ResourceAmount(((AutocraftingRequestInvoker) request).stepcrafter$getResource(), 1));
        this.addSlot(new DisabledResourceSlot(
            resourceContainer,
            0,
            Component.empty(),
            157,
            48,
            ResourceSlotType.FILTER
        ));
    }

    void setListener(final StepCraftingPreviewListener listener) {
        this.listener = listener;
    }

    public AutocraftingRequest getRequest() {
        return this.request;
    }

    void sendRequest(final double amount, final boolean notify) {
        if (!(((AutocraftingRequestInvoker) this.request).stepcrafter$getResource() instanceof PlatformResourceKey resourceKey)) {
            return;
        }
        final long normalizedAmount = resourceKey.getResourceType().normalizeAmount(amount);
        Platform.INSTANCE.sendPacketToServer(
            new StepCraftingRequestPacket(((AutocraftingRequestInvoker) this.request).stepcrafter$getId(), resourceKey, normalizedAmount, notify));
    }

    boolean isNotify() {
        return Platform.INSTANCE.getConfig().isAutocraftingNotification();
    }

    void setNotify(final boolean notify) {
        Platform.INSTANCE.getConfig().setAutocraftingNotification(notify);
    }

    double getMinAmount() {
        if (((AutocraftingRequestInvoker) this.request).stepcrafter$getResource() instanceof PlatformResourceKey platformResource) {
            return platformResource.getResourceType().getDisplayAmount(1);
        }
        return 1D;
    }

    void sendCancelRequest() {
        C2SPackets.sendAutocraftingPreviewCancelRequest();
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }
}
