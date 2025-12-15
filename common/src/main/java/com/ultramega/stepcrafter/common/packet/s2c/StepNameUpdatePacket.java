package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.support.AbstractEditableNameContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepNameUpdatePacket(Component name) implements CustomPacketPayload {
    public static final Type<StepNameUpdatePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_name_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepNameUpdatePacket> STREAM_CODEC =
        StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, StepNameUpdatePacket::name,
            StepNameUpdatePacket::new
        );

    public static void handle(final StepNameUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractEditableNameContainerMenu containerMenu) {
            containerMenu.nameChanged(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
