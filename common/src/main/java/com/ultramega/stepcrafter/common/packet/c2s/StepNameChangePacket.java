package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.AbstractEditableNameContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepNameChangePacket(String name) implements CustomPacketPayload {
    public static final Type<StepNameChangePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_name_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepNameChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8, StepNameChangePacket::name,
            StepNameChangePacket::new
        );

    public static void handle(final StepNameChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractEditableNameContainerMenu containerMenu) {
            containerMenu.changeName(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
