package com.ultramega.stepcrafter.common.packet.c2s;

import com.ultramega.stepcrafter.common.stepcrafter.task.StepTaskProvider;

import com.refinedmods.refinedstorage.api.network.impl.autocrafting.TimeoutableCancellationToken;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.createStepCrafterIdentifier;

public record StepCraftingRequestPacket(UUID id, PlatformResourceKey resource, long amount, boolean notifyPlayer) implements CustomPacketPayload {
    public static final Type<StepCraftingRequestPacket> PACKET_TYPE = new Type<>(createStepCrafterIdentifier("step_crafting_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StepCraftingRequestPacket> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, StepCraftingRequestPacket::id,
        ResourceCodecs.STREAM_CODEC, StepCraftingRequestPacket::resource,
        ByteBufCodecs.VAR_LONG, StepCraftingRequestPacket::amount,
        ByteBufCodecs.BOOL, StepCraftingRequestPacket::notifyPlayer,
        StepCraftingRequestPacket::new
    );

    public static void handle(final StepCraftingRequestPacket packet, final PacketContext ctx) {
        final Player player = ctx.getPlayer();
        if (player.containerMenu instanceof StepTaskProvider provider) {
            final PlayerActor playerActor = new PlayerActor(player);
            final var taskId = provider.stepcrafter$startStepCraftingTask(packet.resource, packet.amount, playerActor, packet.notifyPlayer,
                new TimeoutableCancellationToken());
            S2CPackets.sendAutocraftingResponse((ServerPlayer) player, packet.id, taskId.isPresent());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
