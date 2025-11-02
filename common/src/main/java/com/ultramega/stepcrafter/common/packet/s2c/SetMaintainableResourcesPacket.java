package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.support.MaintainingResource;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.HashSet;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record SetMaintainableResourcesPacket(List<ResourceMinMaxAmount> maintainableResources) implements CustomPacketPayload {
    public static final Type<SetMaintainableResourcesPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("set_maintainable_resources"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMaintainableResourcesPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceMinMaxAmount.STREAM_CODEC.apply(ByteBufCodecs.list()), SetMaintainableResourcesPacket::maintainableResources,
        SetMaintainableResourcesPacket::new
    );

    public static void handle(final SetMaintainableResourcesPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof MaintainingResource maintainingResource) {
            maintainingResource.stepcrafter$setMaintainingResource(new HashSet<>(packet.maintainableResources()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
