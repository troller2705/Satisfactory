package com.troller2705.satisfactory.client;

import com.troller2705.satisfactory.Satisfactory;
import com.troller2705.satisfactory.gui.HubTerminalScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Removed the deprecated 'bus' parameter. NeoForge auto-detects it now!
@EventBusSubscriber(modid = Satisfactory.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Satisfactory.HUB_TERMINAL_MENU.get(), HubTerminalScreen::new);
    }
}