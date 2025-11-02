package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record PatternResourceSlotChangePacket(int slotIndex, boolean tryAlternatives) implements CustomPacketPayload {
    public static final Type<PatternResourceSlotChangePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("pattern_resource_slot_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceSlotChangePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PatternResourceSlotChangePacket::slotIndex,
        ByteBufCodecs.BOOL, PatternResourceSlotChangePacket::tryAlternatives,
        PatternResourceSlotChangePacket::new
    );

    public static void handle(final PatternResourceSlotChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractPatternResourceContainerMenu containerMenu) {
            containerMenu.handlePatternResourceSlotChange(packet.slotIndex, packet.tryAlternatives);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
