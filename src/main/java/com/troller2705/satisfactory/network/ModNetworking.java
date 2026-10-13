package com.troller2705.satisfactory.network;

import com.troller2705.satisfactory.Satisfactory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Satisfactory.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        // Registers our Payload to be sent from the Client to the Server
        registrar.playToServer(
                SubmitMilestonePayload.TYPE,
                SubmitMilestonePayload.STREAM_CODEC,
                SubmitMilestonePayload::handle
        );

        registrar.playToServer(
                SetActiveMilestonePayload.TYPE,
                SetActiveMilestonePayload.STREAM_CODEC,
                SetActiveMilestonePayload::handle
        );
    }
}