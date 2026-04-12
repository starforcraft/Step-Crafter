package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.AbstractClientModInitializer;
import com.ultramega.stepcrafter.common.packet.s2c.PatternResourceSlotUpdatePacket;
import com.ultramega.stepcrafter.common.packet.s2c.SetMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorActivePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskAddedPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskRemovedPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCraftingMonitorTaskStatusChangedPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepManagerActivePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepNameUpdatePacket;
import com.ultramega.stepcrafter.common.registry.Blocks;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor;
import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        this.setRenderLayers();
        this.registerPacketHandlers();
        registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(final MenuType<? extends M> type,
                                                                                                     final ScreenConstructor<M, U> factory) {
                MenuScreens.register(type, factory::create);
            }
        });
    }

    private void setRenderLayers() {
        this.setCutout(Blocks.INSTANCE.getStepCrafterManager());
        this.setCutout(Blocks.INSTANCE.getStepCraftingMonitor());
    }

    private void setCutout(final BlockColorMap<?, ?> blockMap) {
        blockMap.values().forEach(this::setCutout);
    }

    private void setCutout(final Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
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
        ClientPlayNetworking.registerGlobalReceiver(
            StepCraftingMonitorTaskStatusChangedPacket.PACKET_TYPE,
            wrapHandler(StepCraftingMonitorTaskStatusChangedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StepCraftingMonitorTaskRemovedPacket.PACKET_TYPE,
            wrapHandler(StepCraftingMonitorTaskRemovedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StepCraftingMonitorTaskAddedPacket.PACKET_TYPE,
            wrapHandler(StepCraftingMonitorTaskAddedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            StepCraftingMonitorActivePacket.PACKET_TYPE,
            wrapHandler(StepCraftingMonitorActivePacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<T> wrapHandler(final PacketHandler<T> handler) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }
}
