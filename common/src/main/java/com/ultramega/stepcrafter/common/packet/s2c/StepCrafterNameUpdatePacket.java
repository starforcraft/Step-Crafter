package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCrafterNameUpdatePacket(Component name) implements CustomPacketPayload {
    public static final Type<StepCrafterNameUpdatePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafter_name_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCrafterNameUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, StepCrafterNameUpdatePacket::name,
            StepCrafterNameUpdatePacket::new
        );

    public static void handle(final StepCrafterNameUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof StepCrafterContainerMenu containerMenu) {
            containerMenu.nameChanged(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
