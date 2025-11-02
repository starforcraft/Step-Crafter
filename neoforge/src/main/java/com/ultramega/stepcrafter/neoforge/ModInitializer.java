package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.AbstractModInitializer;
import com.ultramega.stepcrafter.common.Platform;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceFilterSlotChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotAmountChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.PatternResourceSlotChangePacket;
import com.ultramega.stepcrafter.common.packet.c2s.RequestMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.c2s.StepCrafterNameChangePacket;
import com.ultramega.stepcrafter.common.packet.s2c.PatternResourceSlotUpdatePacket;
import com.ultramega.stepcrafter.common.packet.s2c.SetMaintainableResourcesPacket;
import com.ultramega.stepcrafter.common.packet.s2c.StepCrafterNameUpdatePacket;
import com.ultramega.stepcrafter.common.registry.BlockEntities;
import com.ultramega.stepcrafter.common.registry.CreativeModeTabItems;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

@Mod(MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private final DeferredRegister<Block> blockRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK, MOD_ID);
    private final DeferredRegister<Item> itemRegistry = DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypeRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);
    private final DeferredRegister<MenuType<?>> menuTypeRegistry = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public ModInitializer(final IEventBus eventBus, final ModContainer modContainer) {
        final ConfigImpl config = new ConfigImpl();
        modContainer.registerConfig(ModConfig.Type.COMMON, config.getSpec());
        Platform.setConfigProvider(() -> config);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.addListener(ClientModInitializer::onClientSetup);
            eventBus.addListener(ClientModInitializer::onRegisterMenuScreens);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        eventBus.addListener(this::onCommonSetup);
        this.registerContent(eventBus);
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(this::registerPackets);
        eventBus.addListener(this::registerCreativeModeTabListener);
    }

    private void registerContent(final IEventBus eventBus) {
        this.registerBlocks(eventBus);
        this.registerItems(eventBus);
        this.registerBlockEntities(eventBus);
        this.registerMenus(eventBus);
    }

    private void registerBlocks(final IEventBus eventBus) {
        super.registerBlocks(new ForgeRegistryCallback<>(this.blockRegistry));
        this.blockRegistry.register(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(this.itemRegistry);
        super.registerItems(callback);
        this.itemRegistry.register(eventBus);
    }

    private void registerBlockEntities(final IEventBus eventBus) {
        super.registerBlockEntities(
            new ForgeRegistryCallback<>(this.blockEntityTypeRegistry),
            new BlockEntityTypeFactory() {
                @SuppressWarnings("DataFlowIssue") // data type can be null
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntityProvider<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            }
        );
        this.blockEntityTypeRegistry.register(eventBus);
    }

    private void registerMenus(final IEventBus eventBus) {
        super.registerMenus(new ForgeRegistryCallback<>(this.menuTypeRegistry), new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec) {
                return IMenuTypeExtension.create((syncId, inventory, buf) -> {
                    final D data = streamCodec.decode(buf);
                    return supplier.create(syncId, inventory, data);
                });
            }
        });
        this.menuTypeRegistry.register(eventBus);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        this.registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getStepCrafter());
        this.registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getStepRequester());
    }

    private void registerNetworkNodeContainerProvider(final RegisterCapabilitiesEvent event,
                                                      final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type) {
        event.registerBlockEntity(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            type,
            (be, side) -> be.getContainerProvider()
        );
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToServer(
            PatternResourceSlotChangePacket.PACKET_TYPE,
            PatternResourceSlotChangePacket.STREAM_CODEC,
            wrapHandler(PatternResourceSlotChangePacket::handle)
        );
        registrar.playToServer(
            PatternResourceSlotAmountChangePacket.PACKET_TYPE,
            PatternResourceSlotAmountChangePacket.STREAM_CODEC,
            wrapHandler(PatternResourceSlotAmountChangePacket::handle)
        );
        registrar.playToServer(
            PatternResourceFilterSlotChangePacket.PACKET_TYPE,
            PatternResourceFilterSlotChangePacket.STREAM_CODEC,
            wrapHandler(PatternResourceFilterSlotChangePacket::handle)
        );
        registrar.playToServer(
            RequestMaintainableResourcesPacket.PACKET_TYPE,
            RequestMaintainableResourcesPacket.STREAM_CODEC,
            wrapHandler(RequestMaintainableResourcesPacket::handle)
        );
        registrar.playToServer(
            StepCrafterNameChangePacket.PACKET_TYPE,
            StepCrafterNameChangePacket.STREAM_CODEC,
            wrapHandler(StepCrafterNameChangePacket::handle)
        );

        registrar.playToClient(
            PatternResourceSlotUpdatePacket.PACKET_TYPE,
            PatternResourceSlotUpdatePacket.STREAM_CODEC,
            wrapHandler(PatternResourceSlotUpdatePacket::handle)
        );
        registrar.playToClient(
            SetMaintainableResourcesPacket.PACKET_TYPE,
            SetMaintainableResourcesPacket.STREAM_CODEC,
            wrapHandler(SetMaintainableResourcesPacket::handle)
        );
        registrar.playToClient(
            StepCrafterNameUpdatePacket.PACKET_TYPE,
            StepCrafterNameUpdatePacket.STREAM_CODEC,
            wrapHandler(StepCrafterNameUpdatePacket::handle)
        );
    }

    private void registerCreativeModeTabListener(final BuildCreativeModeTabContentsEvent e) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getCreativeModeTabId()
        );
        /*final ResourceKey<CreativeModeTab> coloredCreativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getColoredCreativeModeTabId()
        );*/

        if (e.getTabKey().equals(creativeModeTab)) {
            CreativeModeTabItems.append(e::accept);
        }
        /*else if (e.getTabKey().equals(coloredCreativeModeTab)) {
            CreativeModeTabItems.appendColoredVariants(e::accept);
        }*/
    }

    private void onCommonSetup(final FMLCommonSetupEvent e) {
        super.initializePlatformApi();
        this.registerUpgradeMappings();
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(final PacketHandler<T> handler) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return this.registry.register(id.getPath(), value);
        }
    }
}
