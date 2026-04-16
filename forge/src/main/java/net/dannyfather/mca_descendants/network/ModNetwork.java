package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.MCADescendants;
import net.dannyfather.mca_descendants.network.c2s.CallToPlayerMessage;
import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;
import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.dannyfather.mca_descendants.MCADescendants.MODID;

public class ModNetwork {
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(MODID, "main"),
                    () -> "1.0",
                    "1.0"::equals,
                    "1.0"::equals
            );

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(getDescendantsRequest.class, id++)
                .encoder(getDescendantsRequest::encode)
                .decoder(getDescendantsRequest::new)
                .consumerMainThread(getDescendantsRequest::handle)
                .add();
        CHANNEL.messageBuilder(getDescendantResponse.class, id++)
                .encoder(getDescendantResponse::encode)
                .decoder(getDescendantResponse::new)
                .consumerMainThread(getDescendantResponse::handle)
                .add();
        CHANNEL.messageBuilder(CallToPlayerMessage.class, id++)
                .encoder(CallToPlayerMessage::encode)
                .decoder(CallToPlayerMessage::new)
                .consumerMainThread(CallToPlayerMessage::handle)
                .add();
        CHANNEL.messageBuilder(OpenGuiRequest.class, id++)
                .encoder(OpenGuiRequest::encode)
                .decoder(OpenGuiRequest::new)
                .consumerMainThread(OpenGuiRequest::handle)
                .add();
    }

}