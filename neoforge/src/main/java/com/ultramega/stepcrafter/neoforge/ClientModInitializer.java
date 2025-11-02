package com.ultramega.stepcrafter.neoforge;

import com.ultramega.stepcrafter.common.AbstractClientModInitializer;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor;
import com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientModInitializer extends AbstractClientModInitializer {
    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(final RegisterMenuScreensEvent e) {
        registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(final MenuType<? extends M> type,
                                                                                                     final ScreenConstructor<M, U> factory) {
                e.register(type, factory::create);
            }
        });
    }
}
