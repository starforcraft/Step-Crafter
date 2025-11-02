package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;
import com.ultramega.stepcrafter.common.support.ResourceMinMaxAmount;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record PatternResourceSlotUpdatePacket(int slotIndex, Optional<ResourceMinMaxAmount> resourceAmount) implements CustomPacketPayload {
    public static final Type<PatternResourceSlotUpdatePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("pattern_resource_slot_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceSlotUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PatternResourceSlotUpdatePacket::slotIndex,
        ByteBufCodecs.optional(ResourceMinMaxAmount.STREAM_CODEC), PatternResourceSlotUpdatePacket::resourceAmount,
        PatternResourceSlotUpdatePacket::new
    );

    public static void handle(final PatternResourceSlotUpdatePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractPatternResourceContainerMenu containerMenu) {
            containerMenu.handlePatternResourceSlotUpdate(packet.slotIndex, packet.resourceAmount.orElse(null));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
