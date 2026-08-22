package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record PatternResourceDefaultAmountsPacket(double minAmount, double maxAmount, double batchSize) implements CustomPacketPayload {
    public static final Type<PatternResourceDefaultAmountsPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("pattern_resource_default_amounts"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceDefaultAmountsPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, PatternResourceDefaultAmountsPacket::minAmount,
        ByteBufCodecs.DOUBLE, PatternResourceDefaultAmountsPacket::maxAmount,
        ByteBufCodecs.DOUBLE, PatternResourceDefaultAmountsPacket::batchSize,
        PatternResourceDefaultAmountsPacket::new
    );

    public static void handle(final PatternResourceDefaultAmountsPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractPatternResourceContainerMenu containerMenu) {
            containerMenu.setDefaultResourceAmounts(packet.minAmount, packet.maxAmount, packet.batchSize);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
