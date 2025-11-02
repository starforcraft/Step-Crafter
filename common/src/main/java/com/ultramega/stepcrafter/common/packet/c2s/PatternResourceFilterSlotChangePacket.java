package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.support.AbstractPatternResourceContainerMenu;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record PatternResourceFilterSlotChangePacket(int slotIndex, PlatformResourceKey resource) implements CustomPacketPayload {
    public static final Type<PatternResourceFilterSlotChangePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("pattern_resource_filter_slot_change"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PatternResourceFilterSlotChangePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PatternResourceFilterSlotChangePacket::slotIndex,
        ResourceCodecs.STREAM_CODEC, PatternResourceFilterSlotChangePacket::resource,
        PatternResourceFilterSlotChangePacket::new
    );

    public static void handle(final PatternResourceFilterSlotChangePacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof AbstractPatternResourceContainerMenu containerMenu) {
            containerMenu.handlePatternResourceFilterSlotUpdate(packet.slotIndex, packet.resource);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
