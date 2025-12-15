package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.stepmanager.AbstractStepManagerContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepManagerActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<StepManagerActivePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_manager_active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepManagerActivePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, StepManagerActivePacket::active,
        StepManagerActivePacket::new
    );

    public static void handle(final StepManagerActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof AbstractStepManagerContainerMenu containerMenu) {
            containerMenu.setActive(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
