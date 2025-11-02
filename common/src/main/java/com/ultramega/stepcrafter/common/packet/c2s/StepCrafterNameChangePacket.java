package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.stepcrafter.StepCrafterContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCrafterNameChangePacket(String name) implements CustomPacketPayload {
    public static final Type<StepCrafterNameChangePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("stepcrafter_name_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCrafterNameChangePacket> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.STRING_UTF8, StepCrafterNameChangePacket::name,
            StepCrafterNameChangePacket::new
        );

    public static void handle(final StepCrafterNameChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof StepCrafterContainerMenu containerMenu) {
            containerMenu.changeName(packet.name);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
