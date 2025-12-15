package com.ultramega.stepcrafter.fabric;

import com.ultramega.stepcrafter.common.AbstractModInitializer;
import com.ultramega.stepcrafter.common.PlatformProxy;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceFilterSlotChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotAmountChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.RequestMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.c2s.StepNameChangePacket;
import com.ultramega.stepcrafter.common.packet.s2c.PatternResourceSlotUpdatePacket;
import com.ultramega.stepcrafter.common.packet.s2c.SetMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepManagerActivePacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepNameUpdatePacket;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.CreativeModeTabItems;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApi;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import java.util.Arrays;
import java.util.HashSet;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModInitializerImpl extends AbstractModInitializer implements RefinedStoragePlugin, ModInitializer {
    @Override
    public void onApiAvailable(final RefinedStorageApi refinedStorageApi) {
        PlatformProxy.loadPlatform(new PlatformImpl());
        this.registerContent();
        this.registerCapabilities();
        this.registerPackets();
        this.registerPacketHandlers();
        this.registerCreativeModeTabListener(refinedStorageApi);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
    }

    private void registerContent() {
        super.initializePlatformApi();
        super.registerBlocks(new DirectRegistryCallback<>(BuiltInRegistries.BLOCK));
        final DirectRegistryCallback<Item> itemRegistryCallback = new DirectRegistryCallback<>(BuiltInRegistries.ITEM);
        super.registerItems(itemRegistryCallback);
        this.registerUpgradeMappings();
        super.registerBlockEntities(
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE),
            new BlockEntityTypeFactory() {
                @SuppressWarnings("DataFlowIssue") // data type can be null
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntityProvider<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            }
        );
        super.registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec) {
                return new ExtendedScreenHandlerType<>(supplier::create, streamCodec);
            }
        });
    }

    private void registerCapabilities() {
        this.registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getStepCrafter());
        this.registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getStepRequester());
        this.registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getStepCrafterManager());
    }

    private void registerNetworkNodeContainerProvider(final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type) {
        RefinedStorageFabricApi.INSTANCE.getNetworkNodeContainerProviderLookup().registerForBlockEntity(
            (be, dir) -> be.getContainerProvider(),
            type
        );
    }

    private void registerPackets() {
        PayloadTypeRegistry.playC2S().register(PatternResourceSlotChangePacket.PACKET_TYPE, PatternResourceSlotChangePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PatternResourceSlotAmountChangePacket.PACKET_TYPE, PatternResourceSlotAmountChangePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PatternResourceFilterSlotChangePacket.PACKET_TYPE, PatternResourceFilterSlotChangePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestMaintainableResourcesPacket.PACKET_TYPE, RequestMaintainableResourcesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(StepNameChangePacket.PACKET_TYPE, StepNameChangePacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(PatternResourceSlotUpdatePacket.PACKET_TYPE, PatternResourceSlotUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SetMaintainableResourcesPacket.PACKET_TYPE, SetMaintainableResourcesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StepNameUpdatePacket.PACKET_TYPE, StepNameUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StepManagerActivePacket.PACKET_TYPE, StepManagerActivePacket.STREAM_CODEC);
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(
            PatternResourceSlotChangePacket.PACKET_TYPE,
            wrapHandler(PatternResourceSlotChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternResourceSlotAmountChangePacket.PACKET_TYPE,
            wrapHandler(PatternResourceSlotAmountChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternResourceFilterSlotChangePacket.PACKET_TYPE,
            wrapHandler(PatternResourceFilterSlotChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            RequestMaintainableResourcesPacket.PACKET_TYPE,
            wrapHandler(RequestMaintainableResourcesPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            StepNameChangePacket.PACKET_TYPE,
            wrapHandler(StepNameChangePacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<T> wrapHandler(
        final PacketHandler<T> handler
    ) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    private void registerCreativeModeTabListener(final RefinedStorageApi refinedStorageApi) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getCreativeModeTabId()
        );
        ItemGroupEvents.modifyEntriesEvent(creativeModeTab).register(
            entries -> CreativeModeTabItems.appendBlocks(entries::accept)
        );
        final ResourceKey<CreativeModeTab> coloredCreativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getColoredCreativeModeTabId()
        );
        ItemGroupEvents.modifyEntriesEvent(coloredCreativeModeTab).register(
            entries -> CreativeModeTabItems.appendColoredVariants(entries::accept)
        );
    }
}
