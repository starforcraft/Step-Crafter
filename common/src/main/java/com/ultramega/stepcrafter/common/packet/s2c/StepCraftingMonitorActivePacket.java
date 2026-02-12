package com.ultramega.stepcrafter.common.packet.s2c;

import com.ultramega.stepcrafter.common.stepcrafter.stepcraftingmonitor.AbstractStepCraftingMonitorContainerMenu;

import com.refinedmods.refinedstorage.common.support.packet.PacketContext;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCraftingMonitorActivePacket(boolean active) implements CustomPacketPayload {
    public static final Type<StepCraftingMonitorActivePacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafting_monitor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingMonitorActivePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, StepCraftingMonitorActivePacket::active,
        StepCraftingMonitorActivePacket::new
    );

    public static void handle(final StepCraftingMonitorActivePacket packet, final PacketContext ctx) {
        final AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu instanceof AbstractStepCraftingMonitorContainerMenu containerMenu) {
            containerMenu.activeChanged(packet.active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
