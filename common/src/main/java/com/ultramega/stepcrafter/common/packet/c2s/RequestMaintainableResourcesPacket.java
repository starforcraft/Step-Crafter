package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.RequestableMaintainingResources;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record RequestMaintainableResourcesPacket() implements CustomPacketPayload {
    public static final Type<RequestMaintainableResourcesPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("request_maintainable_resources_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestMaintainableResourcesPacket> STREAM_CODEC =
        StreamCodec.unit(new RequestMaintainableResourcesPacket());

    public static void handle(final RequestMaintainableResourcesPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof RequestableMaintainingResources containerMenu) {
            containerMenu.stepcrafter$sendMaintainingResourcesToClient();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
