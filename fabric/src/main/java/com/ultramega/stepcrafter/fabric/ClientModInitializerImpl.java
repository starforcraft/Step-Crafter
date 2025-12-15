package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.AbstractClientModInitializer;
import com.ultramega.stepcrafter.common.packet.s2c.PatternResourceSlotUpdatePacket;
import com.ultramega.stepcrafter.common.packet.s2c.SetMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepManagerActivePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepNameUpdatePacket;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor;
import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        this.registerPacketHandlers();
        registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(final MenuType<? extends M> type,
                                                                                                     final ScreenConstructor<M, U> factory) {
                MenuScreens.register(type, factory::create);
            }
        });
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
            PatternResourceSlotUpdatePacket.PACKET_TYPE,
            wrapHandler(PatternResourceSlotUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            SetMaintainableResourcesPacket.PACKET_TYPE,
            wrapHandler(SetMaintainableResourcesPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StepNameUpdatePacket.PACKET_TYPE,
            wrapHandler(StepNameUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StepManagerActivePacket.PACKET_TYPE,
            wrapHandler(StepManagerActivePacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<T> wrapHandler(final PacketHandler<T> handler) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }
}
