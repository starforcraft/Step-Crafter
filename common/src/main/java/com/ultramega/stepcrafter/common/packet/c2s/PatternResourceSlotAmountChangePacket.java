package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record PatternResourceSlotAmountChangePacket(int slotIndex, long minAmount, long maxAmount, long batchSize) implements CustomPacketPayload {
    public static final Type<PatternResourceSlotAmountChangePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("pattern_resource_slot_amount_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceSlotAmountChangePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PatternResourceSlotAmountChangePacket::slotIndex,
        ByteBufCodecs.VAR_LONG, PatternResourceSlotAmountChangePacket::minAmount,
        ByteBufCodecs.VAR_LONG, PatternResourceSlotAmountChangePacket::maxAmount,
        ByteBufCodecs.VAR_LONG, PatternResourceSlotAmountChangePacket::batchSize,
        PatternResourceSlotAmountChangePacket::new
    );

    public static void handle(final PatternResourceSlotAmountChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractPatternResourceContainerMenu containerMenu) {
            containerMenu.handlePatternResourceSlotAmountChange(packet.slotIndex, packet.minAmount, packet.maxAmount, packet.batchSize);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
